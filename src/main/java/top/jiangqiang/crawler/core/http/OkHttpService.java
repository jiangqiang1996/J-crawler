package top.jiangqiang.crawler.core.http;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.LoggerFactory;
import top.jiangqiang.crawler.core.config.CrawlerGlobalConfig;
import top.jiangqiang.crawler.core.entities.Crawler;
import top.jiangqiang.crawler.core.util.HttpUtil;
import top.jiangqiang.crawler.core.util.JSONUtil;

import java.util.Map;

/**
 * @author jiangqiang
 * @date 2022-09-29
 */
@Slf4j
public class OkHttpService {
    /**
     * 使用全局配置构建的客户端
     */
    private final OkHttpClient client;
    private final Interceptor[] interceptors;
    private final CrawlerGlobalConfig globalConfig;

    public OkHttpService(CrawlerGlobalConfig globalConfig, Interceptor... interceptors) {
        initLogLevel(globalConfig);
        //拦截器;
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder().addNetworkInterceptor(new HttpLoggingInterceptor(new HttpLogger()).setLevel(globalConfig.getLogLevel()));
        Boolean useProxy = globalConfig.getUseProxy();
        Map<String, String> proxyConfig = globalConfig.getProxyConfig();
        if (ArrayUtil.isNotEmpty(interceptors)) {
            for (Interceptor interceptor : interceptors) {
                okHttpClientBuilder.addInterceptor(interceptor);
            }
        }
        if (useProxy == null || !useProxy || CollUtil.isEmpty(proxyConfig)) {
            //不使用代理
            this.client = okHttpClientBuilder.build();
        } else {
            this.client = HttpUtil.useProxy(proxyConfig, okHttpClientBuilder);
        }
        this.interceptors = interceptors;
        this.globalConfig = globalConfig;
    }

    /**
     * 设置报文的日志级别
     *
     * @param globalConfig
     */
    void initLogLevel(CrawlerGlobalConfig globalConfig) {
        if (globalConfig.getLogLevel() == null) {
            String packageName = OkHttpService.class.getPackageName();
            Level level = ((Logger) LoggerFactory.getLogger(packageName)).getLevel();
            while (level == null) {
                if (packageName.contains(".")) {
                    packageName = StrUtil.subBefore(packageName, ".", true);
                } else {
                    packageName = null;
                }
                if (StrUtil.isBlank(packageName)) {
                    level = ((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).getLevel();
                    break;
                } else {
                    level = ((Logger) LoggerFactory.getLogger(packageName)).getLevel();
                }
            }
            switch (level.levelStr) {
                case "OFF", "ERROR" -> globalConfig.setLogLevel(HttpLoggingInterceptor.Level.NONE);
                case "INFO" -> globalConfig.setLogLevel(HttpLoggingInterceptor.Level.HEADERS);
                case "DEBUG", "TRACE", "ALL" -> globalConfig.setLogLevel(HttpLoggingInterceptor.Level.BODY);
                default -> globalConfig.setLogLevel(HttpLoggingInterceptor.Level.BASIC);
            }
        }
    }

    public Call request(Crawler crawler) {
        if (StrUtil.isBlank(crawler.getUrl())) {
            return null;
        }
        try {
            //拦截器;
            OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder().addNetworkInterceptor(new HttpLoggingInterceptor(new HttpLogger()).setLevel(globalConfig.getLogLevel()));
            if (ArrayUtil.isNotEmpty(interceptors)) {
                for (Interceptor interceptor : interceptors) {
                    okHttpClientBuilder.addInterceptor(interceptor);
                }
            }
            Boolean useProxy = globalConfig.getUseProxy();
            Map<String, String> proxyConfig = crawler.getProxyConfig();
            OkHttpClient tmpClient;
            if (useProxy == null || !useProxy || CollUtil.isEmpty(proxyConfig)) {
                tmpClient = client;
            } else {
                tmpClient = HttpUtil.useProxy(proxyConfig, okHttpClientBuilder);
            }
            Map<String, String> lines = crawler.getLines();
            Map<String, String> headers = crawler.getHeaders();
            Map<String, String> body = crawler.getBody();
            if (CollUtil.isEmpty(lines)) {
                lines = globalConfig.getLines();
            }
            if (CollUtil.isEmpty(headers)) {
                headers = globalConfig.getHeaders();
            }
            if (CollUtil.isEmpty(body)) {
                body = globalConfig.getBody();
            }
            Request request = HttpUtil.processRequestParam(crawler.getUrl(), lines, headers, JSONUtil.toJsonStr(body));
            return tmpClient.newCall(request);
        } catch (Exception e) {
            log.debug(e.getMessage());
            return null;
        }
    }
}
