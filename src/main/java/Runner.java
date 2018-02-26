import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Runner {

    // it's supposed to be some cloud/external queue service
    private static final int queueSize = 1_000_000;
    private static final int consumerPoolSize = Runtime.getRuntime().availableProcessors();
    private static final BlockingQueue<String> queue = new ArrayBlockingQueue<>(queueSize);
    private static final ExecutorService consumerPool = Executors.newFixedThreadPool(consumerPoolSize);

    public static void main(String[] args) {
        // starting producer
        Producer producer = new Producer(queue);
        producer.start();
        // starting consumers
        for (int i = 0; i < consumerPoolSize; i++) {
            consumerPool.execute(new Consumer(queue));
        }
    }
}
