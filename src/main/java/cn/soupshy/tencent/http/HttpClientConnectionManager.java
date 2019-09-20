package cn.soupshy.tencent.http;

import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@Component
public class HttpClientConnectionManager {
    private static final Logger log = LoggerFactory.getLogger(HttpClientConnectionManager.class);

    // 最大链接数
    private static final int MAX_TOTAL = 200;
    // 单个路由最大连接数
    private static final int DEFAULT_MAX_PER_ROUTE = 100;
    // 默认执行重试的次数
    private static final int DEFAULT_RETRY_TIMES = 2;
    // 默认请求数据获取超时时间（毫秒）
    private static final int DEFAULT_SOCKET_READ_TIME = 65000;
    private static PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = null;

    @PostConstruct
    public void init() {
        LayeredConnectionSocketFactory connectionSocketFactory = null;
        try {
            connectionSocketFactory = new SSLConnectionSocketFactory(SSLContext.getDefault());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
                .register("https", connectionSocketFactory)
                .register("http", new PlainConnectionSocketFactory())
                .build();
        poolingHttpClientConnectionManager =new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        poolingHttpClientConnectionManager.setMaxTotal(MAX_TOTAL);
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(DEFAULT_MAX_PER_ROUTE);

    }

    public CloseableHttpClient getHttpClient(int retryTimes, int socketTimeout) {
        RequestConfig.Builder builder = RequestConfig.custom();
        // 设置连接超时时间，单位毫秒
        builder.setConnectTimeout(5000);
        // 设置从connect Manager获取Connection 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的。
        builder.setConnectionRequestTimeout(1000);
        if (socketTimeout >= 0) {
            // 请求获取数据的超时时间，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
            builder.setSocketTimeout(socketTimeout);
        }else {
            builder.setSocketTimeout(DEFAULT_SOCKET_READ_TIME);
        }
        RequestConfig defaultRequestConfig = builder.setCookieSpec(CookieSpecs.STANDARD_STRICT).setExpectContinueEnabled(false).setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST)).setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC)).build();
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        if (retryTimes > 0) {
            setRetryHandler(httpClientBuilder, retryTimes);
        } else {
            setRetryHandler(httpClientBuilder, DEFAULT_RETRY_TIMES);
        }
        CloseableHttpClient httpClient = httpClientBuilder
                .setConnectionManager(poolingHttpClientConnectionManager)
                .setDefaultRequestConfig(defaultRequestConfig)
                .build();
        return httpClient;
    }
    private void setRetryHandler(HttpClientBuilder httpClientBuilder, final int retryTimes) {
        HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {
            @Override
            public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                if (executionCount > retryTimes) {
                    log.error("已经重试"+executionCount+"次，请检查网络问题");
                    return false;
                }
                return true;
            }
        };
        httpClientBuilder.setRetryHandler(myRetryHandler);
    }



}
