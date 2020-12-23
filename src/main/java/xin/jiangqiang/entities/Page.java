package xin.jiangqiang.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import xin.jiangqiang.config.Config;
import xin.jiangqiang.util.StringUtil;

import java.nio.charset.Charset;

/**
 * 请求结束后的爬虫,此对象用于提取数据
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class Page extends Crawler {
    private Integer responseCode = null;
    private byte[] content = new byte[0];
    private String html = "";
    private Document document = null;
    private String contentType = "";

    /**
     * okhttp请求之后通过crawler创建page
     * page和crawler的其余属性一致
     *
     * @param crawler      需要继承属性的crawler
     * @param responseCode 响应码
     * @param contentType  响应mimeType类型
     * @param content      页面的字节数组
     * @param charset      编码
     * @return 返回一个page
     */
    public static Page getPage(Crawler crawler, Integer responseCode, String contentType, byte[] content, Charset charset) {
        Page page = new Page();
        page.initDataFromCrawler(crawler);
        page.setResponseCode(responseCode);
        page.setContentType(contentType);
        if (content == null) {
            content = new byte[0];
        }
        page.setContent(content);
        if (page.getContentType().contains("application/json")) {
            //内容为json
            log.debug(page.getContentType());
        } else if (contentType.contains("image")) {
            log.debug(page.getContentType());
        } else if ("".equals(page.getContentType())) {
            //经过测试,对https://beian.miit.gov.cn/发送请求时的contentType为空
            //使用postman对上述网站发GET请求获取到的是js字符串
            log.info("URL:" + page.getUrl() + "的contenType为空");
        } else if (page.getContentType().contains("text/html")) {
            String html = new String(content, charset);
            page.setHtml(html);
            if (StringUtil.isNotEmpty(html)) {
                page.document = Jsoup.parse(html, page.getUrl());
            }
        }
        return page;
    }

    /**
     * selenium使用
     *
     * @param crawler      需要继承属性的crawler
     * @param responseCode 响应码
     * @param contentType  响应mimeType类型
     * @param html         html
     * @return 返回一个page
     */
    public static Page getPage(Crawler crawler, Integer responseCode, String contentType, String html) {
        Page page = new Page();
        page.initDataFromCrawler(crawler);
        page.setResponseCode(responseCode);
        page.setContentType(contentType);
        if (page.getContentType().contains("application/json")) {
            //内容为json
            log.debug(page.getContentType());
        } else if (contentType.contains("image")) {
            log.debug(page.getContentType());
        } else if ("".equals(page.getContentType())) {
            if (StringUtil.isNotEmpty(page.getHtml())) {
                page.document = Jsoup.parse(page.getHtml(), page.getUrl());
            }
        } else if (page.getContentType().contains("text/html")) {
            if (StringUtil.isNotEmpty(page.getHtml())) {
                page.document = Jsoup.parse(page.getHtml(), page.getUrl());
            }
        }
        return page;
    }
}