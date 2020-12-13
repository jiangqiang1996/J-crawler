package xin.jiangqiang.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import xin.jiangqiang.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class Crawler {
    //    protected List<String> seeds = new ArrayList<>();
    //深度
    protected Integer depth = 1;
    protected String url;//当前的URL
    //根据type执行不同逻辑
    private String type = "";
    List<Crawler> crawlers = new ArrayList<>();//当前URL中提取出来的子爬虫

    //    public Crawler addSeed(String url) {
//        seeds.add(url);
//        return this;
//    }
    public Crawler addSeed(String url) {
        if (StringUtil.isNotEmpty(url)) {
            Crawler crawler = new Crawler(url);
            crawlers.add(crawler);
            return crawler;
        }
        return null;
    }

    public void addSeeds(List<String> urls, String type) {
        for (String url : urls) {
            if (StringUtil.isNotEmpty(url)) {
                addSeed(url).setType(type);
            }
        }
    }

    public Crawler(String url) {
        this.url = url;
    }

    public Crawler(String url, String type) {
        this.url = url;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Crawler crawler = (Crawler) o;
        return Objects.equals(url, crawler.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }
}
