package xin.jiangqiang;

import lombok.extern.slf4j.Slf4j;
import xin.jiangqiang.annotation.*;
import xin.jiangqiang.app.TradApplication;
import xin.jiangqiang.entities.Crawler;
import xin.jiangqiang.entities.Next;
import xin.jiangqiang.entities.Page;
import xin.jiangqiang.filter.NextFilter;
import xin.jiangqiang.management.Record;
import xin.jiangqiang.management.RecordImpl;
import xin.jiangqiang.net.RequestMethod;


import java.io.IOException;
import java.util.*;

@Slf4j
//@App
public class Test extends TradApplication {

    @Before
    public void before() {

    }

    @Match(value = "type1", code = "404")
    public void match1(Page page, Next next) {
//        log.info("url:" + page.getUrl() + " type:" + page.getType());
    }

    @Match(type = "type2")
    public void match2(Page page, Next next) {
//        log.info("url:" + page.getUrl() + " type:" + page.getType());
    }

    @Deal
    public void deal(Page page, Next next) {
//        log.info(page);
//        log.info(next);
//        next.addSeed("https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp/4.0.0").setType("type2");
//        next.addSeed("https://blog.csdn.net/wangmx1993328/article/details/81662001").setType("type1");
//        next.addSeed("https://blog.csdn.net/ds986619036/article/details/89310472");
//        log.info("url:" + page.getUrl() + " type:" + page.getType());

//        System.out.println(page.getHtml());
        System.out.println("____________");
        System.out.println(next);
//        System.out.println(page.getHtml());

//        System.out.println(page);
    }

    @After
    public void after() {
        log.info("after执行了");
    }

    public static void main(String[] args) throws IOException {
        Test test = new Test();

        test.getConfig().setPackageName("xin.jiangqiang");

        Crawler crawler = new Crawler();
        Map<String, String> lines = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        Map<String, String> bodys = new HashMap<>();
        Map<String, String> configs = new HashMap<>();

//        headers.put("SUIXIN", "SUIXIN");
//        bodys.put("username", "蒋樯");
//        bodys.put("password", "19961226qwe");
//        headers.put("Content-Type", "application/json");
////        headers.put("Content-Type", "application/x-www-form-urlencoded");
//        crawler.addSeed("https://blog.jiangqiang.xin/api/admin/login").setLines(lines).setHeaders(headers)
//                .setBodys(bodys).setMethod(RequestMethod.POST);

        configs.put("IP", "14.18.49.22");
        configs.put("port", "233");
        configs.put("username", "123");
        configs.put("password", "314");
        //http://www.ip3366.net/?stype=1&page=1
        crawler.addSeed("https://blog.jiangqiang.xin").setLines(lines).setHeaders(headers).setConfigs(configs)
                .setBodys(bodys).setMethod(RequestMethod.GET);

//        crawler.addSeed("http://blog.jiangqiang.xin").setLines(lines).setHeaders(headers)
//                .setBodys(bodys).setMethod(RequestMethod.POST);
//        crawler.addSeed("https://blog.jiangqiang.xin").setType("type2");
//        crawler.addSeed("https://mvnrepository.com").setType("type1");
//        config.addRegEx("https://.*");
//        config.addDefaultRegEx("https://.*");
//        crawler.addSeed("https://blog.jiangqiang.xin");
//        crawler.addSeed("https://www.4399.com");
//        crawler.addSeed("https://github.com/jiangqiang2020/J-crawler");
        test.getConfig().setSavePath("tmp.obj");
        test.getConfig().setIsContinue(false);
        test.getConfig().setDepth(2);

        test.setCrawler(crawler);
        test.setFilter(new NextFilter());
        test.setRecord(new RecordImpl());
        new Thread(test::start).start();
        Record record = new RecordImpl();
//        while (true) {
//            try {
//                Thread.sleep(1000);
//                Set<String> succ = (Set<String>) record.getSucc();
//                Set<String> err = (Set<String>) record.getErr();
////                System.out.println(succ);
////                System.err.println(err);
//                System.out.println("爬取成功的URL：   " + succ.size());
//                System.out.println("爬取失败的URL：   " + err.size());
//                if (test.getIsEnd()) {
//                    break;
//                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    }
}
