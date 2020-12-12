package xin.jiangqiang;

import lombok.extern.slf4j.Slf4j;
import xin.jiangqiang.annotation.*;
import xin.jiangqiang.config.Config;
import xin.jiangqiang.crawler.RAMCrawler;
import xin.jiangqiang.entities.Crawler;
import xin.jiangqiang.entities.Next;
import xin.jiangqiang.entities.Page;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@App
public class Test extends RAMCrawler {

    @Before
    public void before() {
    }

    @Match("type1")
    public void match1(Page page, Next next) {
        log.info("url:" + page.getUrl() + " type:" + page.getType());
    }

    @Match("type2")
    public void match2(Page page, Next next) {
        log.info("url:" + page.getUrl() + " type:" + page.getType());
    }

    @Deal
    public void deal(Page page, Next next) {
//        log.info(page);
//        log.info(next);
//        next.addSeed("https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp/4.0.0").setType("type2");
//        next.addSeed("https://blog.csdn.net/wangmx1993328/article/details/81662001").setType("type1");
//        next.addSeed("https://blog.csdn.net/ds986619036/article/details/89310472");
        log.info("url:" + page.getUrl() + " type:" + page.getType());
    }

    @After
    public void after() {
        log.info("after执行了");
    }

    public static void main(String[] args) throws IOException {
        Test test = new Test();
        Config config = new Config();
        config.setPackageName("xin.jiangqiang");
        Crawler crawler = new Crawler();
        crawler.addSeed("https://www.baidu.com").setType("type2");
        crawler.addSeed("https://mvnrepository.com").setType("type1");
        config.addRegEx("https://.*");
//        crawler.addSeed("https://blog.jiangqiang.xin");
//        crawler.addSeed("https://www.4399.com");
//        crawler.addSeed("https://github.com/jiangqiang2020/J-crawler");
        test.setConfig(config);
        test.setCrawler(crawler);
        test.start();
    }
}
