package top.jiangqiang.sample;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.jiangqiang.core.interceptor.DefaultHttpInterceptor;

/**
 * @author Jiangqiang
 * @version 1.0
 * @description TODO
 * @date 2022/9/30 12:59
 */
@Configuration
public class Config {
    @Bean
    public DefaultHttpInterceptor a() {
        return new DefaultHttpInterceptor();
    }
}
