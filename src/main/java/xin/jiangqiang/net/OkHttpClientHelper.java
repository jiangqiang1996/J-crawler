package xin.jiangqiang.net;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import xin.jiangqiang.config.Config;
import xin.jiangqiang.entities.Crawler;
import xin.jiangqiang.entities.Page;
import xin.jiangqiang.management.Record;
import xin.jiangqiang.util.StringUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
public class OkHttpClientHelper {
    Config config;//暂时没用上
    Record record;

    public OkHttpClientHelper(Config config, Record record) {
        this.config = config;
        this.record = record;
    }

    public Page request(Crawler crawler) {
        try {
            if (StringUtil.isEmpty(crawler.getUrl())) {
                return null;
            }
            //判断是否成功爬取过该URL
            if (!record.hasUrl(crawler.getUrl())) {
                OkHttpClient client = processOkHttpClient(crawler.getConfigs());
//                OkHttpClient client = new OkHttpClient();
                log.info("url: " + crawler.getUrl());
                Request request = getRequest(crawler);
                if (request == null) {
                    return null;
                }
                //同步请求，可以优化为异步请求
                //todo
                Response response = client.newCall(request).execute();
                Integer code = response.code();
                Protocol protocol = response.protocol();
                String message = response.message();
                Headers headers = response.headers();
                ResponseBody body = response.body();
                byte[] content = Objects.requireNonNull(response.body()).bytes();
                //content可能为图片等资源时,不应该转字符串,只不过不影响后续操作
                String html = new String(content, new Config().getCharset());
                Page page = new Page(code, protocol, message, headers, body, response.request(), content, html);
                page.initDataFromCrawler(crawler);
                return page;
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            //爬取错误 记录,一旦出错会返回null
            record.addErr(crawler.getUrl());
        }
        return null;
    }

    private Request getRequest(Crawler crawler) throws JsonProcessingException {
        //请求行
        Map<String, String> lines = crawler.getLines();
        //请求头
        Map<String, String> headers = crawler.getHeaders();

        //请求体，请求参数
        Map<String, String> bodys = crawler.getBodys();
        return processRequest(crawler.getUrl(), lines, headers, bodys);
    }


    public OkHttpClient processOkHttpClient(@NonNull Map<String, String> configs) {
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder().addInterceptor(new CommonInterCeptor());

        String IP = configs.get("IP");
        String portString = configs.get("port");
        String protocol = configs.get("protocol");
        String username = configs.get("username");
        String password = configs.get("password");
        //IP和端口号不为空
        if (StringUtil.isNotEmpty(IP) && StringUtil.isNotEmpty(portString)) {
            Proxy.Type type = Proxy.Type.HTTP;//默认HTTP
            if (StringUtil.isNotEmpty(protocol)) {
                //根据protocol确定type
                type = selectType(protocol);
            }
            int port = Integer.parseInt(portString);
            okHttpClientBuilder.proxy(new Proxy(type, new InetSocketAddress(IP, port)));
            //有验证身份信息
            if (StringUtil.isNotEmpty(username) && StringUtil.isNotEmpty(password)) {
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

    public Request processRequest(String url, Map<String, String> lines, Map<String, String> headers, Map<String, String> bodys) throws JsonProcessingException {

        Request.Builder builder = new Request.Builder();

        //请求行中获取请求方式
        String method = lines.get("method");

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
                if (StringUtil.isEmpty(contentType)) {
                    return null;
                } else if (contentType.contains("application/json")) {//JSON提交
                    MediaType mediaType = MediaType.Companion.parse("application/json;charset=utf-8");
                    RequestBody requestBody = RequestBody.Companion.create(new ObjectMapper().writeValueAsString(bodys), mediaType);
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
}
