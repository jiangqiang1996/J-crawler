package top.jiangqiang.http;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import top.jiangqiang.base.BaseException;
import top.jiangqiang.interceptor.HttpInterceptorr;
import top.jiangqiang.util.JSONUtil;

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
public class HttpUtil {
    public Request processRequest(String url, Map<String, String> lines, Map<String, String> headers, Map<String, String> body) {
        Request.Builder builder = new Request.Builder();
        if (StrUtil.isBlank(url)) {
            throw new BaseException("请求方式不能为空");
        }
        url = url.trim();
        String method;
        //请求行中获取请求方式
        if (CollUtil.isEmpty(lines) || lines.get("method") == null) {
            method = "GET";
        } else {
            method = lines.get("method");
        }
        if (CollUtil.isNotEmpty(headers)) {
            //添加请求头
            Set<Map.Entry<String, String>> headerEntrySet = headers.entrySet();
            for (Map.Entry<String, String> entry : headerEntrySet) {
                builder.header(entry.getKey(), entry.getValue());
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
                HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
                if (CollUtil.isNotEmpty(body)) {
                    //GET或HEAD请求添加请求参数
                    Set<String> keySet = body.keySet();
                    for (String key : keySet) {
                        String value = body.get(key);
                        urlBuilder.addQueryParameter(key, value);
                    }
                }
                builder.method(method, null).url(urlBuilder.build());
        }
        return builder.build();
    }

    /**
     * 创建http请求客户端对象
     *
     * @param configs
     * @return
     */
    public OkHttpClient processOkHttpClient(Map<String, String> configs) {
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder().addInterceptor(new HttpInterceptorr());//拦截器;
        String ip = configs.get("IP");
        String portString = configs.get("port");
        String protocol = configs.get("protocol");
        String username = configs.get("username");
        String password = configs.get("password");
        //IP和端口号不为空
        if (StrUtil.isNotEmpty(ip) && StrUtil.isNotEmpty(portString)) {
            Proxy.Type type = Proxy.Type.HTTP;//默认HTTP
            if (StrUtil.isNotEmpty(protocol)) {
                //根据protocol确定type
                type = selectType(protocol);
            }
            int port = Integer.parseInt(portString);
            okHttpClientBuilder.proxy(new Proxy(type, new InetSocketAddress(ip, port)));
            //有验证身份信息
            if (StrUtil.isNotEmpty(username) && StrUtil.isNotEmpty(password)) {
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
//    public Call request(Crawler crawler) {
//        if (StrUtil.isEmpty(crawler.getUrl())) {
//            return null;
//        }
//        OkHttpClient client;
//        if (((HttpConfig<?>) crawler).getUseProxy()) {
//            client = processOkHttpClient(((HttpConfig<?>) crawler).getHttpConfig());
//        } else {
//            client = new OkHttpClient.Builder().addInterceptor(new CommonInterCeptor()).build();//拦截器;
//        }
//        log.debug("url: " + crawler.getUrl());
//        Request request = processRequest(crawler.getUrl(), ((HttpConfig<?>) crawler).getLines(), ((HttpConfig<?>) crawler).getHeaders(), ((HttpConfig<?>) crawler).getBodys());
//        if (request == null) {
//            return null;
//        }
//        return client.newCall(request);
//    }
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
}
