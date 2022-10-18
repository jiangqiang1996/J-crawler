package top.jiangqiang.crawler.core.util;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
public class DocumentUtil {
    public static List<String> getAllUrl(String html, String baseUri, Boolean strong) {
        if (StrUtil.isBlank(html)) {
            return new ArrayList<>();
        }
        Set<String> urls = new HashSet<>();
        if (baseUri == null) {
            baseUri = "";
        }
        try {
            Document doc = Jsoup.parse(html, baseUri);
            Elements links = doc.select("a[href]");
            Elements media = doc.select("[src]");
            Elements imports = doc.select("link[href]");
            for (Element src : media) {
                String url = src.attr("abs:src");
                if (!baseUri.contains(url)) {
                    urls.add(url);
                }
            }
            for (Element link : imports) {
                String url = link.attr("abs:href");
                if (!baseUri.contains(url)) {
                    urls.add(url);
                }
            }
            for (Element link : links) {
                String url = link.attr("abs:href");
                if (!baseUri.contains(url)) {
                    urls.add(url);
                }
            }
        } catch (Exception e) {
            log.error(ExceptionUtil.stacktraceToString(e));
        }
        if (BooleanUtil.isTrue(strong)) {
            String regEx = """
                    ('|")(\\./|/|//|http://|https://)[\\w\\./-]+('|")
                    """;
            List<String> allGroups = ReUtil.findAllGroup0(Pattern.compile(regEx.trim(), Pattern.DOTALL), html);
            for (String str : allGroups) {
                String url = generateUrl(baseUri, str);
                if (StrUtil.isNotBlank(url)) {
                    urls.add(url);
                }
            }
        }
        return new ArrayList<>(urls);
    }

    public static String generateUrl(String baseUri, String str) {
        baseUri = baseUri.trim();
        str = str.substring(1, str.length() - 1);
        if (str.startsWith("http://") || str.startsWith("https://")) {
            return str;
        } else if (str.startsWith("//")) {
            //双斜线开头，需要拼接协议
            if (StrUtil.startWith(baseUri, "https://")) {
                return "https:" + str;
            } else {
                return "http:" + str;
            }
        } else {
            if (StrUtil.isBlank(baseUri)) {
                return null;
            }
            if (str.startsWith("./")) {
                if (baseUri.endsWith("/")) {
                    baseUri = baseUri.substring(0, baseUri.length() - 1);
                }
                String before = StrUtil.subBefore(baseUri, "/", true);
                if (StrUtil.isNotBlank(before)) {
                    return before + "/" + str.substring(2);
                }
            } else if (str.startsWith("/")) {
                String regEx = """
                        (http://|https://|)[\\w\\.-]+
                         """;
                //基础部分，协议+域名部分
                String group0 = ReUtil.getGroup0(regEx.trim(), baseUri);
                if (StrUtil.isNotBlank(group0)) {
                    return group0 + str;
                }
            }
        }
        return null;
    }

}
