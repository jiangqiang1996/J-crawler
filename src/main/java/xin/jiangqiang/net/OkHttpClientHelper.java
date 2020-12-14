package xin.jiangqiang.net;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import xin.jiangqiang.config.Config;
import xin.jiangqiang.entities.Crawler;
import xin.jiangqiang.entities.Page;
import xin.jiangqiang.management.Record;
import xin.jiangqiang.util.StringUtil;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
public class OkHttpClientHelper {
    Config config;
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
                OkHttpClient client = new OkHttpClient.Builder()
                        .addInterceptor(new CommonInterCeptor())
                        .build();
//                OkHttpClient client = new OkHttpClient();
                log.info("url: " + crawler.getUrl());
                Request request = buildRequest(crawler);
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

    public Request buildRequest(Crawler crawler) throws JsonProcessingException {
        Request.Builder builder = new Request.Builder();
        //请求行，暂时没用，只用到了请求类型
        //crawler.getLines();
        //请求头
        Map<String, String> headers = crawler.getHeaders();

        //添加请求头
        Set<Map.Entry<String, String>> headerEntrySet = headers.entrySet();
        for (Map.Entry<String, String> entry : headerEntrySet) {
            builder.header(entry.getKey(), entry.getValue());
        }
        //请求体，请求参数
        Map<String, String> bodys = crawler.getBodys();

        if ("GET".equals(crawler.getMethod()) || "HEAD".equals(crawler.getMethod())) {
            HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(crawler.getUrl())).newBuilder();
            //GET或HEAD请求添加请求参数
            Set<String> keySet = bodys.keySet();
            for (String key : keySet) {
                String value = bodys.get(key);
                urlBuilder.addQueryParameter(key, value);
            }
            builder.method(crawler.getMethod(), null).url(urlBuilder.build());
        } else {//POST,PUT等请求
            if (bodys.keySet().size() != 0) {//有参数
                String contentType = headers.get("Content-Type");
                if (StringUtil.isEmpty(contentType)) {
                    return null;
                } else if (contentType.contains("application/json")) {//JSON提交
                    MediaType mediaType=MediaType.Companion.parse("application/json;charset=utf-8");
                    RequestBody requestBody=RequestBody.Companion.create(new ObjectMapper().writeValueAsString(bodys),mediaType);
                    builder.method(crawler.getMethod(), requestBody);
                } else if (contentType.equals("application/x-www-form-urlencoded")) {//表单提交
                    FormBody.Builder formBodyBuilder = new FormBody.Builder();
                    //添加请求参数
                    Set<String> keySet = bodys.keySet();
                    for (String key : keySet) {
                        String value = bodys.get(key);
                        formBodyBuilder.add(key, value);
                    }
                    FormBody formBody = new FormBody.Builder().build();
                    builder.method(crawler.getMethod(), formBody);
                }
            } else {
                builder.method(crawler.getMethod(), RequestBody.Companion.create(new byte[0]));//没有参数
            }
            builder.url(crawler.getUrl());
        }
        return builder.build();
    }
}
