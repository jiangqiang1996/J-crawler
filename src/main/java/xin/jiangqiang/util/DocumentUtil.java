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
    public static List<String> getAllUrl(String html) {
        if (!StringUtil.isNotEmpty(html)) {
            return new ArrayList<>();
        }
        Set<String> urls = new HashSet<>();
        Document doc = Jsoup.parse(html);
        Elements links = doc.select("a[href]");
        Elements media = doc.select("[src]");
        Elements imports = doc.select("link[href]");
        for (Element src : media) {
            urls.add(src.attr("abs:src"));
        }
        for (Element link : imports) {
            urls.add(link.attr("abs:href"));
        }
        for (Element link : links) {
            urls.add(link.attr("abs:href"));
        }
        return new ArrayList<>(urls);
    }

}
