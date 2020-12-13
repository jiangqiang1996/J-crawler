package xin.jiangqiang.net;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import xin.jiangqiang.config.Config;
import xin.jiangqiang.entities.Crawler;
import xin.jiangqiang.entities.Page;
import xin.jiangqiang.management.Record;
import xin.jiangqiang.util.StringUtil;

import java.io.IOException;
import java.util.Objects;

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
                Request request = new Request.Builder().url(crawler.getUrl()).build();
                Response response = client.newCall(request).execute();
                Integer code = response.code();
                Protocol protocol = response.protocol();
                String message = response.message();
                Headers headers = response.headers();
                ResponseBody body = response.body();
                byte[] content = Objects.requireNonNull(response.body()).bytes();
                String html = new String(content, new Config().getCharset());
//                record.addSucc(crawler.getUrl());
                return new Page(code, protocol, message, headers, body, response.request(), content, html);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            record.addErr(crawler.getUrl());
        }
        return null;
    }

}
