package xin.jiangqiang.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class Crawler {
    //    protected List<String> seeds = new ArrayList<>();
    //深度
    protected Integer depth = 1;
    protected String url;
    //根据type执行不同逻辑
    private String type = "";
    List<Crawler> crawlers = new ArrayList<>();

//    public Crawler addSeed(String url) {
//        seeds.add(url);
//        return this;
//    }
    public Crawler addSeed(String url) {
        Crawler crawler = new Crawler(url);
        crawlers.add(crawler);
        return crawler;
    }

    public Crawler(String url) {
        this.url = url;
    }

    public Crawler(String url, String type) {
        this.url = url;
        this.type = type;
    }
}
