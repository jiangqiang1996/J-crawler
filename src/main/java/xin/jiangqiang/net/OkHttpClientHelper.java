package xin.jiangqiang.net;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import xin.jiangqiang.config.Config;
import xin.jiangqiang.entities.Crawler;
import xin.jiangqiang.entities.Page;

import java.io.IOException;
import java.util.Objects;

@Slf4j
public class OkHttpClientHelper {
    Config config;

    public OkHttpClientHelper(Config config) {
        this.config = config;
    }

    public Page request(Crawler crawler) throws IOException {
        OkHttpClient client = new OkHttpClient();
        log.info("url: " + crawler.getUrl());
        Request request = new Request.Builder().url(crawler.getUrl()).build();
        Response response = client.newCall(request).execute();
        Integer code = response.code();
        Protocol protocol = response.protocol();
        String message = response.message();
        Headers headers = response.headers();
        ResponseBody body = response.body();
        byte[] content = Objects.requireNonNull(response.body()).bytes();
        String html = new String(content, new Config().getCharset());
//        page.setResponseCode(code).setProtocol(protocol).setMessage(message).setHeaders(headers).setBody(body).setRequest(response.request()).setContent(content).setHtml(html);
//        return page;
        return new Page(code, protocol, message, headers, body, response.request(), content, html);
    }

}
