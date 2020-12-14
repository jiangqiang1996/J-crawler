package xin.jiangqiang.net;

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
                OkHttpClient client = new OkHttpClient();
                log.info("url: " + crawler.getUrl());
                Request request = buildRequest(crawler);
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

    public Request buildRequest(Crawler crawler) {
        if ("GET".equals(crawler.getMethod()) || "HEAD".equals(crawler.getMethod())) {
            return new Request.Builder().method(crawler.getMethod(), null).url(crawler.getUrl()).build();
        } else {
            FormBody.Builder formBodyBuilder = new FormBody.Builder();
            Map<String, String> paramMap = crawler.getBodys();
            Set<String> keySet = paramMap.keySet();
            for (String key : keySet) {
                String value = paramMap.get(key);
                formBodyBuilder.add(key, value);
            }
            FormBody formBody = new FormBody.Builder().build();
            return new Request.Builder().method(crawler.getMethod(), formBody).url(crawler.getUrl()).build();
        }
    }
}
