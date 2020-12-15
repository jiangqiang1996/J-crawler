package xin.jiangqiang.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import xin.jiangqiang.util.StringUtil;

import java.util.Objects;

/**
 * 请求结束后的爬虫,此对象用于提取数据
 */
@Slf4j
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class Page extends Crawler {
    private Integer responseCode;
    private Protocol protocol;
    private String message;
    private Headers responseHeaders;
    private ResponseBody responseBody;
    private Request request;
    private byte[] content;
    private String html;
    private Document document;

    public Page(Integer responseCode, Protocol protocol, String message, Headers headers, ResponseBody body, Request request, byte[] content, String html) {
        this.responseCode = responseCode;
        this.protocol = protocol;
        this.message = message;
        this.responseHeaders = headers;
        this.responseBody = body;
        this.request = request;
        if (content == null) {
            content = new byte[0];
        }
        this.content = content;
        this.html = html;
        String contentType = "";
        MediaType mediaType = body.contentType();
        if (mediaType != null) {
            contentType = mediaType.toString();
        }
        if (contentType.contains("application/json")) {
            //内容为json
            log.debug(contentType);
        } else if (StringUtil.isNotEmpty(html) && contentType.contains("text/html")) {
            this.document = Jsoup.parse(html);
        } else if (contentType.contains("image")) {
            log.debug(contentType);
        } else if ("".equals(contentType)) {
            //经过测试,对https://beian.miit.gov.cn/发送请求时的contentType为空
            //使用postman对上述网站发GET请求获取到的是js字符串
            log.info("URL:" + request.url().url().toString() + "的contenType为空");
        }
        this.setUrl(request.url().url().toString());
    }

}