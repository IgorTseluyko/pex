import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;

/**
 * Implementation of Producer/Consumer pattern
 */
public class Producer {

    private BlockingQueue<String> queue;

    public Producer(BlockingQueue<String> queue ) {
        this.queue = queue;
    }

    private static final Logger logger = LoggerFactory.getLogger(Producer.class);

    /**
     * Starts producer
     * Reads file by default bufferSize(8192) and put lines into queue
     */
    protected void start() {
        try (BufferedReader br = Files.newBufferedReader(Paths.get("src/main/resources/urls.txt"))) {
            br.lines().forEach(id -> {
                try {
                    queue.put(id);
                } catch (Exception e) {
                    logger.error("Exception: {}", e);
                }
            });
        } catch (Exception e) {
            logger.error("Exception: {}", e);
        }
    }

}
