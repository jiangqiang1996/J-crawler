package top.jiangqiang.crawler.core.http;

import ch.qos.logback.classic.Logger;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.LoggerFactory;
import top.jiangqiang.crawler.core.base.BaseException;
import top.jiangqiang.crawler.core.config.CrawlerGlobalConfig;
import top.jiangqiang.crawler.core.entities.Crawler;
import top.jiangqiang.crawler.core.util.JSONUtil;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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

    /**
     * 设置报文的日志级别
     *
     * @param globalConfig
     */
    void initLogLevel(CrawlerGlobalConfig globalConfig) {
        if (globalConfig.getLogLevel() == null) {
            Logger logger = (Logger) LoggerFactory.getLogger("top.jiangqiang.crawler.http");
            if (logger.getLevel() == null) {
                logger = (Logger) LoggerFactory.getLogger("top.jiangqiang.crawler");
            }
            if (logger.getLevel() == null) {
                logger = (Logger) LoggerFactory.getLogger("top.jiangqiang");
            }
            if (logger.getLevel() == null) {
                logger = (Logger) LoggerFactory.getLogger("top");
            }
            if (logger.getLevel() == null) {
                logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            }
            if (logger.getLevel() == null) {
                globalConfig.setLogLevel(HttpLoggingInterceptor.Level.NONE);
                return;
            }
            String levelStr = logger.getLevel().levelStr;
            switch (levelStr) {
                case "OFF", "ERROR" -> globalConfig.setLogLevel(HttpLoggingInterceptor.Level.NONE);
                case "INFO" -> globalConfig.setLogLevel(HttpLoggingInterceptor.Level.HEADERS);
                case "DEBUG", "TRACE", "ALL" -> globalConfig.setLogLevel(HttpLoggingInterceptor.Level.BODY);
                default -> globalConfig.setLogLevel(HttpLoggingInterceptor.Level.BASIC);
            }
        }
    }

    public OkHttpService(CrawlerGlobalConfig globalConfig, Interceptor... interceptors) {
        initLogLevel(globalConfig);
        //拦截器;
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                .addNetworkInterceptor(
                        new HttpLoggingInterceptor(new HttpLogger())
                                .setLevel(globalConfig.getLogLevel())
                );
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
            this.client = processOkHttpClient(proxyConfig, okHttpClientBuilder);
        }
        this.interceptors = interceptors;
        this.globalConfig = globalConfig;
    }

    public Call request(Crawler crawler) {
        if (StrUtil.isBlank(crawler.getUrl())) {
            return null;
        }
        try {
            //拦截器;
            OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                    .addNetworkInterceptor(new HttpLoggingInterceptor(new HttpLogger()).setLevel(globalConfig.getLogLevel()));
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
                tmpClient = processOkHttpClient(proxyConfig, okHttpClientBuilder);
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
            Request request = processRequestParam(crawler.getUrl(), lines, headers, body);
            return tmpClient.newCall(request);
        } catch (Exception e) {
            log.debug(e.getMessage());
            return null;
        }
    }

    /**
     * 构造请求参数
     *
     * @param url
     * @param lines
     * @param headers
     * @param body
     * @return
     */
    public Request processRequestParam(String url, Map<String, String> lines, Map<String, String> headers, Map<String, String> body) {
        Request.Builder builder = new Request.Builder();
        if (StrUtil.isBlank(url)) {
            throw new BaseException("请求方式不能为空");
        }
        url = url.trim();
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
        String method;
        //请求行中获取请求方式
        if (CollUtil.isEmpty(lines)) {
            method = "GET";
        } else {
            method = lines.get("method");
            if (StrUtil.isBlank(method)) {
                method = "GET";
            } else {
                method = method.toUpperCase();
            }
        }
        if (CollUtil.isNotEmpty(headers)) {
            //添加请求头
            Set<Map.Entry<String, String>> headerEntrySet = headers.entrySet();
            for (Map.Entry<String, String> entry : headerEntrySet) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        switch (method) {
            case "POST":
            case "PUT":
                if (CollUtil.isNotEmpty(body)) {
                    String contentType = headers.get("Content-Type");
                    if (StrUtil.isBlank(contentType)) {
                        contentType = "application/x-www-form-urlencoded";
                    }
                    if (contentType.startsWith("application/json")) {
                        MediaType mediaType = MediaType.parse("application/json;charset=utf-8");
                        RequestBody requestBody = RequestBody.Companion.create(JSONUtil.toJsonStr(body), mediaType);
                        builder.method(method, requestBody);
                    } else if ("application/x-www-form-urlencoded".equals(contentType)) {
                        FormBody.Builder formBodyBuilder = new FormBody.Builder();
                        //添加请求参数
                        Set<String> keySet = body.keySet();
                        for (String key : keySet) {
                            String value = body.get(key);
                            formBodyBuilder.add(key, value);
                        }
                        builder.method(method, formBodyBuilder.build());
                    } else {
                        log.debug("不支持的contentType：{}", contentType);
                        throw new BaseException("不支持的contentType：" + contentType);
                    }
                } else {
                    //没有参数
                    builder.method(method, RequestBody.Companion.create(new byte[0]));
                }
                break;
            case "GET":
            case "HEAD":
            default:
                if (CollUtil.isNotEmpty(body)) {
                    //GET或HEAD请求添加请求参数
                    Set<String> keySet = body.keySet();
                    for (String key : keySet) {
                        String value = body.get(key);
                        urlBuilder.addQueryParameter(key, value);
                    }
                }
                builder.method(method, null);
        }
        return builder.url(urlBuilder.build()).build();
    }

    /**
     * 使用代理配置
     *
     * @param configs
     * @param okHttpClientBuilder
     * @return
     */
    public OkHttpClient processOkHttpClient(Map<String, String> configs, OkHttpClient.Builder okHttpClientBuilder) {
        if (CollUtil.isEmpty(configs)) {
            return okHttpClientBuilder.build();
        }
        String ip = configs.get("IP");
        String portString = configs.get("port");
        String protocol = configs.get("protocol");
        String username = configs.get("username");
        String password = configs.get("password");
        //IP和端口号不为空
        if (StrUtil.isNotBlank(ip) && StrUtil.isNotBlank(portString)) {
            Proxy.Type type = Proxy.Type.HTTP;//默认HTTP
            if (StrUtil.isNotBlank(protocol)) {
                //根据protocol确定type
                type = selectType(protocol);
            }
            int port = Integer.parseInt(portString);
            okHttpClientBuilder.proxy(new Proxy(type, new InetSocketAddress(ip, port)));
            //有验证身份信息
            if (StrUtil.isNotBlank(username) && StrUtil.isNotBlank(password)) {
                okHttpClientBuilder.proxyAuthenticator((route, response) -> {
                    //设置代理服务器账号密码
                    String credential = Credentials.basic(username, password);
                    return response.request().newBuilder()
                            .header("Proxy-Authorization", credential)
                            .build();
                });
            }
        }
        return okHttpClientBuilder.build();
    }

    /**
     * 转换为代理方式
     *
     * @param protocol
     * @return
     */
    private Proxy.Type selectType(String protocol) {
        if ("HTTP".equals(protocol) || "".equals(protocol)) {
            return Proxy.Type.HTTP;
        } else if ("DIRECT".equals(protocol)) {
            return Proxy.Type.DIRECT;
        } else if ("SOCKS".equals(protocol)) {
            return Proxy.Type.SOCKS;
        }
        return Proxy.Type.HTTP;
    }

    @Slf4j
    public static class HttpLogger implements HttpLoggingInterceptor.Logger {
        @Override
        public void log(String s) {
            if (s.length() > 300) {
                log.debug("\n" + s);
            } else {
                log.debug(s);
            }
        }
    }
}
