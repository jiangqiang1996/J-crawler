package top.jiangqiang.core.http;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import top.jiangqiang.core.base.BaseException;
import top.jiangqiang.core.config.CrawlerGlobalConfig;
import top.jiangqiang.core.entities.Crawler;
import top.jiangqiang.core.interceptor.DefaultHttpInterceptor;
import top.jiangqiang.core.util.JSONUtil;
import top.jiangqiang.core.util.SpringUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author jiangqiang
 * @date 2022-09-29
 */
@Slf4j
public class OkHttpUtil {
    public static class HttpLogger implements HttpLoggingInterceptor.Logger {
        @Override
        public void log(String s) {
            if (s.length() > 300) {
                log.debug("\n" + s);
            } else {
                log.info(s);
            }
        }
    }

    public static Call request(Crawler crawler, CrawlerGlobalConfig globalConfig) {
        if (StrUtil.isBlank(crawler.getUrl())) {
            return null;
        }
        try {
            AtomicReference<Interceptor> interceptor = new AtomicReference<>();
            interceptor.set(SpringUtil.getOneBeanDefault(Interceptor.class, new DefaultHttpInterceptor()));
            //拦截器;
            OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder().addInterceptor(interceptor.get())
                    .addNetworkInterceptor(new HttpLoggingInterceptor(new HttpLogger()).setLevel(globalConfig.getLogLevel()));

            Boolean useProxy = globalConfig.getUseProxy();
            OkHttpClient client;
            if (useProxy == null || !useProxy) {
                //不使用代理
                client = okHttpClientBuilder.build();
            } else {
                Map<String, String> proxyConfig = crawler.getProxyConfig();
                if (CollUtil.isEmpty(proxyConfig)) {
                    proxyConfig = globalConfig.getProxyConfig();
                }
                client = processOkHttpClient(proxyConfig, okHttpClientBuilder);
            }

            log.debug("url: " + crawler.getUrl());
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
            return client.newCall(request);
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
    public static Request processRequestParam(String url, Map<String, String> lines, Map<String, String> headers, Map<String, String> body) {
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
    public static OkHttpClient processOkHttpClient(Map<String, String> configs, OkHttpClient.Builder okHttpClientBuilder) {
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
    private static Proxy.Type selectType(String protocol) {
        if ("HTTP".equals(protocol) || "".equals(protocol)) {
            return Proxy.Type.HTTP;
        } else if ("DIRECT".equals(protocol)) {
            return Proxy.Type.DIRECT;
        } else if ("SOCKS".equals(protocol)) {
            return Proxy.Type.SOCKS;
        }
        return Proxy.Type.HTTP;
    }

    public static Response send(String url, String method) {
        Request request = new Request.Builder().method(method, null).url(URLUtil.url(url)).build();
        try {
            return new OkHttpClient.Builder().build().newCall(request).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
