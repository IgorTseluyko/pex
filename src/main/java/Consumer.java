import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.BlockingQueue;

public class Consumer implements Runnable {

    private static final Logger fileLogger = LoggerFactory.getLogger("file-logger");
    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    private BlockingQueue<String> queue;

    public Consumer(BlockingQueue<String> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        // waiting for entries from queue
        while (true) {
            String url = null;

            try {
                url = queue.take();
            } catch (Exception e) {
                logger.error("Exception: {}", e);
            }

            if (url != null) {
                try (InputStream is = HttpManager.getStream(url)) {
                    logger.info("Reading from url: {}", url);
                    try (ImageInputStream iis = ImageIO.createImageInputStream(is)) {
                        Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);
                        Map<Integer, Integer> colorsMap = new HashMap<>();

                        // buffered image streaming (for large images)
                        while (iter.hasNext()) {
                            ImageReader imageReader = iter.next();
                            imageReader.setInput(iis);
                            BufferedImage image = imageReader.read(0);
                            Map<Integer, Integer> partition = getColorsMap(image);
                            colorsMap.putAll(partition);
                        }
                        String colorHex = getMostCommonColor(colorsMap);
                        fileLogger.info(url + ", " + colorHex);
                    }
                } catch (Exception e) {
                    logger.error("Exception: {}", e);
                }
            }
        }
    }


    private Map<Integer, Integer> getColorsMap(BufferedImage image) {
        int height = image.getHeight();
        int width = image.getWidth();
        int[][] rbgArray = getRgb(image);

        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                // this is very costly method,
                // could be improved by self-implementation (depends on requirements)
                // int rgb = image.getRGB(j, i);
                int rgb = rbgArray[i][j];
                int counter = 0;
                if (map.containsKey(rgb)) {
                    counter = map.get(rgb);
                }
                counter++;
                map.put(rgb, counter);
            }
        }
        return map;
    }

    private String getMostCommonColor(Map<Integer, Integer> map) {
        if (map == null || map.isEmpty()) return "Exception";
        int mostCommonColor = map.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
        int[] rgb = getRGBArr(mostCommonColor);
        return Integer.toHexString(rgb[0]) + ", " + Integer.toHexString(rgb[1]) + ", " + Integer.toHexString(rgb[2]);
    }

    private int[] getRGBArr(int pixel) {
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;
        return new int[]{red, green, blue};
    }

    /**
     * faster alternative of image.getRGB(j, i);
     * @param image
     * @return
     */
    private int[][] getRgb(BufferedImage image) {
        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();
        final boolean hasAlphaChannel = image.getAlphaRaster() != null;

        int[][] result = new int[height][width];
        if (hasAlphaChannel) {
            final int pixelLength = 4;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
                argb += ((int) pixels[pixel + 1] & 0xff); // blue
                argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
                result[row][col] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        } else {
            final int pixelLength = 3;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += -16777216; // 255 alpha
                argb += ((int) pixels[pixel] & 0xff); // blue
                argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
                result[row][col] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        }
        return result;
    }
}
