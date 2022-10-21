package top.jiangqiang.crawler.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import top.jiangqiang.crawler.core.base.BaseException;
import top.jiangqiang.crawler.core.http.HttpLogger;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author jiangqiang
 * @date 2022-10-17
 */
@Slf4j
public class HttpUtil {
    /**
     * 日志级别，如果没有传入日志级别，则使用此值，如果此字段没有值，则默认HEADERS
     */
    public static HttpLoggingInterceptor.Level level = null;

    /**
     * post请求，提交文件参数
     *
     * @param url
     * @param file
     * @return
     */
    public static Response post(String url, File file) {
        Map<String, String> lines = new HashMap<>();
        lines.put("method", "POST");
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/octet-stream");
        Call call = buildCall(url, lines, headers, file.getAbsolutePath(), null, null);
        try {
            return call.execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * post请求，提交json或xml
     *
     * @param url
     * @param body
     * @return
     */
    public static Response post(String url, String body) {
        Map<String, String> lines = new HashMap<>();
        lines.put("method", "POST");
        Map<String, String> headers = new HashMap<>();
        String contentType = getContentType(body);
        headers.put("Content-Type", contentType);
        Call call = buildCall(url, lines, headers, body, null, null);
        try {
            return call.execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从请求参数的body中判断请求的Content-Type类型，支持的类型有：
     *
     * <pre>
     * 1. application/json
     * 2. application/xml
     * </pre>
     *
     * @param body 请求参数体
     * @return Content-Type类型，如果无法判断返回null
     */
    public static String getContentType(String body) {
        String contentType = null;
        if (StrUtil.isNotBlank(body)) {
            char firstChar = body.charAt(0);
            switch (firstChar) {
                case '{', '[' ->
                    // JSON请求体
                        contentType = "application/json";
                case '<' ->
                    // XML请求体
                        contentType = "application/xml";
                default -> {
                }
            }
        }
        return contentType;
    }

    public static Response post(String url, Map<String, String> body) {
        return request(url, "POST", body);
    }

    public static Response put(String url, Map<String, String> body) {
        return request(url, "PUT", body);
    }

    /**
     * get请求，没有参数
     *
     * @param url
     * @return
     */
    public static Response get(String url) {
        return get(url, null);
    }

    /**
     * get请求，附带参数
     *
     * @param url
     * @param params
     * @return
     */
    public static Response get(String url, Map<String, String> params) {
        return request(url, "GET", params);
    }

    /**
     * delete请求
     *
     * @param url
     * @param params
     * @return
     */
    public static Response delete(String url, Map<String, String> params) {
        return request(url, "DELETE", params);
    }

    /**
     * @param url    请求地址
     * @param method 请求方法
     * @param params 请求参数
     * @return
     */
    public static Response request(String url, String method, Map<String, String> params) {
        try {
            Map<String, String> lines = new HashMap<>();
            lines.put("method", method);
            return buildCall(url, lines, null, params, null, null).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param url         请求地址
     * @param lines       请求行
     * @param headers     请求头
     * @param body        请求体或参数
     * @param proxyConfig 代理设置
     * @param level       日志级别，默认只打印header
     * @return
     */
    public static Call buildCall(String url, Map<String, String> lines, Map<String, String> headers,
                                 Map<String, String> body, Map<String, String> proxyConfig, HttpLoggingInterceptor.Level level) {
        return buildCall(url, lines, headers, JSONUtil.toJsonStr(body), proxyConfig, level);
    }

    /**
     * @param url         请求地址
     * @param lines       请求行
     * @param headers     请求头
     * @param bodyStr     请求参数，可以是json字符串或xml字符串或文件路径等等
     * @param proxyConfig 代理设置
     * @param level       日志级别，默认只打印header
     * @return
     */
    public static Call buildCall(String url, Map<String, String> lines, Map<String, String> headers,
                                 String bodyStr, Map<String, String> proxyConfig, HttpLoggingInterceptor.Level level) {
        if (level == null) {
            level = Objects.requireNonNullElse(HttpUtil.level, HttpLoggingInterceptor.Level.HEADERS);
        }
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder().addNetworkInterceptor(new HttpLoggingInterceptor(new HttpLogger()).setLevel(level));
        OkHttpClient okHttpClient;
        if (CollUtil.isNotEmpty(proxyConfig)) {
            okHttpClient = useProxy(proxyConfig, okHttpClientBuilder);
        } else {
            okHttpClient = okHttpClientBuilder.build();
        }
        Request request = HttpUtil.processRequestParam(url, lines, headers, bodyStr);
        return okHttpClient.newCall(request);
    }

    /**
     * 构造请求参数
     *
     * @param url
     * @param lines
     * @param headers
     * @param bodyStr json字符串或xml字符串或文件路径等等
     * @return
     */
    public static Request processRequestParam(String url, Map<String, String> lines, Map<String, String> headers, String bodyStr) {
        Request.Builder builder = new Request.Builder();
        if (StrUtil.isBlank(url)) {
            throw new BaseException("请求链接不能为空");
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
            case "PATCH":
                if (StrUtil.isNotBlank(bodyStr)) {
                    String contentType;
                    if (CollUtil.isEmpty(headers) || StrUtil.isBlank(headers.get("Content-Type"))) {
                        contentType = "application/x-www-form-urlencoded";
                    } else {
                        contentType = headers.get("Content-Type");
                    }
                    if (contentType.startsWith("application/json")) {
                        MediaType mediaType = MediaType.parse("application/json;charset=utf-8");
                        RequestBody requestBody = RequestBody.Companion.create(bodyStr, mediaType);
                        builder.method(method, requestBody);
                    } else if (contentType.startsWith("application/xml")) {
                        MediaType mediaType = MediaType.parse("application/xml;charset=utf-8");
                        RequestBody requestBody = RequestBody.Companion.create(bodyStr, mediaType);
                        builder.method(method, requestBody);
                    } else if ("application/x-www-form-urlencoded".equals(contentType)) {
                        FormBody.Builder formBodyBuilder = new FormBody.Builder();
                        Map<String, String> body = strToMap(bodyStr);
                        //添加请求参数
                        Set<String> keySet = body.keySet();
                        for (String key : keySet) {
                            String value = body.get(key);
                            formBodyBuilder.add(key, value);
                        }
                        builder.method(method, formBodyBuilder.build());
                    } else if ("application/octet-stream".equals(contentType)) {
                        MediaType mediaType = MediaType.parse("application/octet-stream;charset=utf-8");
                        RequestBody requestBody = RequestBody.Companion.create(new File(bodyStr), mediaType);
                        builder.method(method, requestBody);
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
            case "DELETE":
            default:
                if (StrUtil.isNotBlank(bodyStr)) {
                    Map<String, String> body = strToMap(bodyStr);
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

    private static Map<String, String> strToMap(String jsonStr) {
        try {
            return JSONUtil.parse(jsonStr, new HashMap<>());
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * 使用代理配置
     *
     * @param configs
     * @param okHttpClientBuilder
     * @return
     */
    public static OkHttpClient useProxy(Map<String, String> configs, OkHttpClient.Builder okHttpClientBuilder) {
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
                    return response.request().newBuilder().header("Proxy-Authorization", credential).build();
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

}
