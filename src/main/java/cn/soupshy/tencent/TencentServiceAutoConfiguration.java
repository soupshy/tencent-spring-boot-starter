package cn.soupshy.tencent;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
/**自动装配这个properties类，读取yaml自定义内容**/
@EnableConfigurationProperties(TencentConfig.class)
/**service类，@ConditionalOnClass某个 Class 位于类路径上，才会实例化一个Bean。也就是说，当classpath下发现该类的情况下进行实例化。**/
@ConditionalOnClass(TencentService.class)
/**当配置文件中 tencentyun 的值为 true 时，实例化此类。可以不填**/
@ConditionalOnProperty(prefix = "tencentyun", value = "true", matchIfMissing = true)
/**
 * @author 石络
 */
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
}
