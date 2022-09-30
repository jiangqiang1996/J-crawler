package top.jiangqiang.core.common;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class DocumentUtil {
    public static List<String> getAllUrl(String html, String baseUri, Boolean strong) {
        if (StrUtil.isBlank(html)) {
            return new ArrayList<>();
        }
        Set<String> urls = new HashSet<>();
        if (baseUri == null) {
            baseUri = "";
        }
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
        if (BooleanUtil.isTrue(strong)) {
            String regEx = """
                    (=)("|')(\\./|/|//|http://|https://)([\\S]){1,}('|")
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
        str = str.trim();
        if (str.length() < 5) {
            return null;
        }
        //去掉引号和最前面的等号
        str = str.substring(2, str.length() - 2);
        if (str.startsWith("//")) {
            String before = StrUtil.subBefore(baseUri, "//", false);
            return before + str;
        } else if (str.startsWith("http://") || str.startsWith("https://")) {
            return str;
        } else if (str.startsWith("./")) {
            String regEx = """
                    (http://|https://)(.*){1,}(/)
                     """;
            String group0 = ReUtil.getGroup0(regEx.trim(), baseUri);
            return group0 + str.substring(2);
        } else if (str.startsWith("/")) {
            String regEx = """
                          (?=^.{3,255}$)(http(s)?:\\/\\/)?(www\\.)?[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+(:\\d+)*(\\/\\w+\\.\\w+)*
                    """;
            String group0 = ReUtil.getGroup0(regEx.trim(), baseUri);
            return group0 + str;
        }
        return null;
    }

}
