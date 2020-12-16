package xin.jiangqiang.test;

import lombok.extern.slf4j.Slf4j;
import xin.jiangqiang.annotation.After;
import xin.jiangqiang.annotation.Before;
import xin.jiangqiang.annotation.Deal;
import xin.jiangqiang.annotation.Match;
import xin.jiangqiang.app.Application;
import xin.jiangqiang.entities.Next;
import xin.jiangqiang.entities.Page;
import xin.jiangqiang.net.RequestMethod;
import xin.jiangqiang.util.FileUtil;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jiangqiang
 * @date 2020/12/16 9:23
 */
@Slf4j
public class SaveApplicationTest extends Application {
    @Before
    public void before() {
        log.info("before执行了");
    }

    @Match("home")
    public void matchHome(Page page, Next next) {
        log.info("home执行了");
    }

    @Match(code = "405")
    public void matchCode405(Page page, Next next) {
        log.info("405执行了");
//        System.out.println(page.getHtml());
        //页面标题为Method Not Allowed.因为我使用了POST请求页面，该页面只能GET请求
        //document对象里面提取数据，URL，具体怎么用百度jsoup教程
        String titile = page.getDocument().select("h1").text();
        System.out.println(titile);
    }

    @Match(code = "404")
    public void matchCode404(Page page, Next next) {
        log.info("404执行了");
    }

    @Match("article")
    public void matchAtricle(Page page, Next next) {
        log.info("article执行了");
    }

    @Deal
    public void deal(Page page, Next next) {
        log.info("deal执行了");
    }

    @After
    public void after() {
        log.info("after执行了");
    }

    public static void main(String[] args) {
        SaveApplicationTest saveApplicationTest = new SaveApplicationTest();
        Map<String, String> lines = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        Map<String, String> bodys = new HashMap<>();
        Map<String, String> configs = new HashMap<>();
//        saveApplicationTest.getCrawler().addSeed("https://blog.jiangqiang.xin").setType("home").setLines(lines).setHeaders(headers)
//                .setBodys(bodys).setConfigs(configs).setMethod(RequestMethod.GET);
        saveApplicationTest.getConfig().addRegEx("https://.*");//满足正则表达式的所有URL将会自动提取到deal生命周期的Next对象
        saveApplicationTest.getConfig().setIsUseDefault(true);//启用默认内置正则表达式过滤，默认会过滤css，js的url
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");

        //设置保存路径，保存的是没有爬取完成的种子，方便下次爬取，而不是保存的爬取数据，路径不为空并且种子还没爬取完时就提前结束才会保存
        saveApplicationTest.getConfig().setSavePath("D:\\tmp\\" + simpleDateFormat.format(new Date()) + "\\" + "1.txt");
        //是否使用上次保存的种子继续爬取
        saveApplicationTest.getConfig().setIsContinue(true);
        saveApplicationTest.start();

    }
}
