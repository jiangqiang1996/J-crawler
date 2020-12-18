package xin.jiangqiang.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DocumentUtil {
    public static List<String> getAllUrl(String html, String baseUri) {
        if (!StringUtil.isNotEmpty(html)) {
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
        return new ArrayList<>(urls);
    }

}
