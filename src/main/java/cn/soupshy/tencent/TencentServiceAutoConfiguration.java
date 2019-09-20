package cn.soupshy.tencent;

import cn.soupshy.tencent.http.HttpClientConnectionManager;
import cn.soupshy.tencent.http.HttpClientUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



/**
 * @author 石络
 */
@Configuration
public class TencentServiceAutoConfiguration {

    /**
     * 指定实例化接口的类
     * @return
     * @ConditionalOnMissingBean 当spring容器中不存在TencentService的实例时，默认调用这个。
     * 如果你实现了TencentService接口，并添加到了Spring容器，则会使用你的TencentService实例。
     */
    @Bean
    @ConditionalOnMissingBean
    public TencentService tencentService() {
        return new TencentServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public HttpClientUtil httpClientUtil() {
        return new HttpClientUtil();
    }

    @Bean
    @ConditionalOnMissingBean
    public TencentConfiguration tencentConfiguration() {
        return new TencentConfiguration();
    }

    @Bean
    @ConditionalOnMissingBean
    public HttpClientConnectionManager httpClientConnectionManager() {
        return new HttpClientConnectionManager();
    }

}
