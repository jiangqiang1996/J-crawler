package top.jiangqiang.core.entities;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.nio.charset.Charset;

/**
 * @Author: JiangQiang
 * @Date: 2022年05月14日 10:10
 * 请求结束后的爬虫,此对象用于提取数据
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class Page extends Crawler {
    //html或json
    private String content = "";
    private Document document = null;

    private Integer responseCode = null;
    private byte[] bodyBytes = new byte[0];
    private String contentType = "";

    /**
     * okhttp请求之后通过crawler创建page
     * page和crawler的其余属性一致
     *
     * @param crawler      需要继承属性的crawler
     * @param responseCode 响应码
     * @param contentType  响应mimeType类型
     * @param bodyBytes    页面的字节数组
     * @param charset      编码
     * @return 返回一个page
     */
    public static Page getPage(Crawler crawler, Integer responseCode, String contentType, byte[] bodyBytes, Charset charset) {
        Page page = getPage(crawler, responseCode, contentType);
        if (bodyBytes == null) {
            bodyBytes = new byte[0];
        }
        page.setBodyBytes(bodyBytes);
        if (page.getContentType().contains("application/json")) {
            String html = new String(bodyBytes, charset);
            page.setContent(html);
            //内容为json
            log.debug(page.getContentType());
        } else if (contentType.contains("image")) {
            log.debug(page.getContentType());
        } else if ("".equals(page.getContentType())) {
            //经过测试,对https://beian.miit.gov.cn/发送请求时的contentType为空
            //使用postman对上述网站发GET请求获取到的是js字符串
            log.debug("URL:" + page.getUrl() + "的contenType为空");
        } else if (page.getContentType().contains("text/html")) {
            String html = new String(bodyBytes, charset);
            page.setContent(html);
            if (StrUtil.isNotEmpty(html)) {
                page.document = Jsoup.parse(html, page.getUrl());
            }
        }
        return page;
    }

    public static Page getPage(Crawler crawler, Integer responseCode, String contentType) {
        //深克隆
        crawler = ObjectUtil.cloneByStream(crawler);
        Page page = new Page();
        BeanUtil.copyProperties(crawler, page);
        page.setResponseCode(responseCode);
        page.setContentType(contentType);
        return page;
    }
}
