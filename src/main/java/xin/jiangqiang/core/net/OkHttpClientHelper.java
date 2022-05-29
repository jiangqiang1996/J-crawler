package xin.jiangqiang.core.net;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import xin.jiangqiang.core.entities.Crawler;
import xin.jiangqiang.core.interfaces.HttpConfig;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
public class OkHttpClientHelper {

    public Call request(Crawler crawler) {
        if (StrUtil.isEmpty(crawler.getUrl())) {
            return null;
        }
        OkHttpClient client;
        if (((HttpConfig<?>) crawler).getUseProxy()) {
            client = processOkHttpClient(((HttpConfig<?>) crawler).getHttpConfig());
        } else {
            client = new OkHttpClient.Builder().addInterceptor(new CommonInterCeptor()).build();//拦截器;
        }
        log.debug("url: " + crawler.getUrl());
        Request request = processRequest(crawler.getUrl(), ((HttpConfig<?>) crawler).getLines(), ((HttpConfig<?>) crawler).getHeaders(), ((HttpConfig<?>) crawler).getBodys());
        if (request == null) {
            return null;
        }
        return client.newCall(request);
    }

    /**
     * 获取请求对象
     *
     * @param url
     * @param lines
     * @param headers
     * @param bodys
     * @return
     */
    public Request processRequest(String url, Map<String, String> lines, Map<String, String> headers, Map<String, String> bodys) {

        Request.Builder builder = new Request.Builder();
        //请求行中获取请求方式
        String method = lines.get("method");
        if (StrUtil.isEmpty(method)) {
            method = "GET";
        }
        //添加请求头
        Set<Map.Entry<String, String>> headerEntrySet = headers.entrySet();
        for (Map.Entry<String, String> entry : headerEntrySet) {
            builder.header(entry.getKey(), entry.getValue());
        }

        //判断请求类型,根据不同类型设置参数
        if ("GET".equals(method) || "HEAD".equals(method)) {//GET和HEAD请求
            HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
            //GET或HEAD请求添加请求参数
            Set<String> keySet = bodys.keySet();
            for (String key : keySet) {
                String value = bodys.get(key);
                urlBuilder.addQueryParameter(key, value);
            }
            builder.method(method, null).url(urlBuilder.build());
        } else {//POST,PUT等请求
            if (bodys.keySet().size() != 0) {//有参数
                String contentType = headers.get("Content-Type");
                if (StrUtil.isEmpty(contentType)) {
                    log.info("此方式请求时，Content-Type不能为空");
                    return null;
                } else if (contentType.contains("application/json")) {//JSON提交
                    MediaType mediaType = MediaType.parse("application/json;charset=utf-8");
                    RequestBody requestBody = RequestBody.Companion.create(JSONUtil.toJsonPrettyStr(bodys), mediaType);
                    builder.method(method, requestBody);
                } else if (contentType.equals("application/x-www-form-urlencoded")) {//表单提交
                    FormBody.Builder formBodyBuilder = new FormBody.Builder();
                    //添加请求参数
                    Set<String> keySet = bodys.keySet();
                    for (String key : keySet) {
                        String value = bodys.get(key);
                        formBodyBuilder.add(key, value);
                    }
                    FormBody formBody = new FormBody.Builder().build();
                    builder.method(method, formBody);
                }
            } else {
                builder.method(method, RequestBody.Companion.create(new byte[0]));//没有参数
            }
            builder.url(url);
        }
        return builder.build();
    }

    /**
     * 创建http请求客户端对象
     *
     * @param configs
     * @return
     */
    public OkHttpClient processOkHttpClient(@NonNull Map<String, String> configs) {
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder().addInterceptor(new CommonInterCeptor());//拦截器;
        String IP = configs.get("IP");
        String portString = configs.get("port");
        String protocol = configs.get("protocol");
        String username = configs.get("username");
        String password = configs.get("password");
        //IP和端口号不为空
        if (StrUtil.isNotEmpty(IP) && StrUtil.isNotEmpty(portString)) {
            Proxy.Type type = Proxy.Type.HTTP;//默认HTTP
            if (StrUtil.isNotEmpty(protocol)) {
                //根据protocol确定type
                type = selectType(protocol);
            }
            int port = Integer.parseInt(portString);
            okHttpClientBuilder.proxy(new Proxy(type, new InetSocketAddress(IP, port)));
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