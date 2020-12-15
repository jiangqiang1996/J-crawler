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

import java.util.HashMap;
import java.util.Map;

/**
 * 最基本的爬虫实例
 * 本文讲解基本处理实例
 * Page对象封装了请求响应结果，他本身也是爬虫的子类
 * 可以直接取出html内容，以及字节流保存为文件等等
 *
 * @author jiangqiang
 * @date 2020/12/15 17:19
 */
@Slf4j
public class DealApplicationTest extends Application {
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
        DealApplicationTest dealApplicationTest = new DealApplicationTest();
        Map<String, String> lines = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        Map<String, String> bodys = new HashMap<>();
        Map<String, String> configs = new HashMap<>();
        dealApplicationTest.getCrawler().addSeed("https://blog.jiangqiang.xin").setType("home").setLines(lines).setHeaders(headers)
                .setBodys(bodys).setConfigs(configs).setMethod(RequestMethod.POST);
        dealApplicationTest.start();
    }
}
