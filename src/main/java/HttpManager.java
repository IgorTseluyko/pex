import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.io.IOException;
import java.io.InputStream;

public class HttpManager {

    private static CloseableHttpClient client;

    static {
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();

        SocketConfig socketConfig = SocketConfig
                .custom()
                .setSoKeepAlive(true)
                .setTcpNoDelay(true)
                .build();
        connManager.setDefaultSocketConfig(socketConfig);
        connManager.setMaxTotal(Runtime.getRuntime().availableProcessors());

        client = HttpClients
                .custom()
                .setConnectionManager(connManager)
                .build();
    }


    /**
     * We assume that all urls are correct and no extra verification is required
     *
     * @param url
     * @return
     * @throws IOException
     */
    protected static InputStream getStream(String url) throws IOException {
        return client.execute(new HttpGet(url)).getEntity().getContent();
    }

}
