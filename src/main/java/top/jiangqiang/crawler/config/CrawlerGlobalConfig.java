package top.jiangqiang.crawler.config;

import lombok.Data;
import okhttp3.logging.HttpLoggingInterceptor;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Supplier;

@Data
public class CrawlerGlobalConfig {
    /**
     * 返回一个时间，单位毫秒，用于指定每次爬虫任务请求的时间间隔，
     * 建议返回一个随机数，不容易被察觉为机器请求，防止被封禁。
     * delayTime为null或Supplier.get()返回null或小于等于0的数字则不需要时间间隔
     */
    private Supplier<Long> delayTime = () -> null;
    /**
     * 请求和响应报文的日志打印级别
     */
    private HttpLoggingInterceptor.Level logLevel = HttpLoggingInterceptor.Level.BASIC;

    /**
     * 自定义线程池，可以替代okhttp底层的线程池
     */
    private ThreadPoolExecutor threadPoolExecutor;
    /**
     * 设置代理池的密码
     */
    private ProxyAuthenticatorConfig proxyAuthenticatorConfig;
}
