package xin.jiangqiang.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.beanutils.BeanUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import xin.jiangqiang.util.StringUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * 请求结束后的爬虫,此对象用于提取数据
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class Page extends Crawler {
    private Integer responseCode;
    private byte[] content;
    private String html;
    private Document document;

    public Page(Crawler crawler, Integer responseCode, ResponseBody body, Request request, byte[] content, String html) {
        try {
            BeanUtils.copyProperties(this, crawler);//继承公共参数
        } catch (Exception e) {
            e.printStackTrace();
            log.warn("继承公共参数失败，可能会对程序逻辑有影响，请检查，错误信息：" + e.getMessage());
        }
        this.setUrl(request.url().url().toString());
        this.responseCode = responseCode;
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
            this.document = Jsoup.parse(html, getUrl());
        } else if (contentType.contains("image")) {
            log.debug(contentType);
        } else if ("".equals(contentType)) {
            //经过测试,对https://beian.miit.gov.cn/发送请求时的contentType为空
            //使用postman对上述网站发GET请求获取到的是js字符串
            log.info("URL:" + request.url().url().toString() + "的contenType为空");
        }

    }

}