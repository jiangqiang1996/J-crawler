package xin.jiangqiang.sample;

import lombok.extern.slf4j.Slf4j;
import xin.jiangqiang.annotation.After;
import xin.jiangqiang.annotation.Before;
import xin.jiangqiang.annotation.Deal;
import xin.jiangqiang.annotation.Match;
import xin.jiangqiang.app.TradApplication;
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
public class AutoBaseTradApplicationTest extends TradApplication {
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
    }

    @Match(code = "404")
    public void matchCode404(Page page, Next next) {
        log.info("404执行了");
    }

    @Match("article")
    public void matchAtricle(Page page, Next next) {
        log.info("article执行了");
        //已经自动提取URL到next里面
        System.out.println(next.getCrawlers());
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
        AutoBaseTradApplicationTest autoBaseApplicationTest = new AutoBaseTradApplicationTest();
        Map<String, String> lines = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        Map<String, String> bodys = new HashMap<>();
        Map<String, String> configs = new HashMap<>();
        autoBaseApplicationTest.getConfig().addRegEx("https://.*");//满足正则表达式的所有URL将会自动提取到deal生命周期的Next对象
        autoBaseApplicationTest.getConfig().setIsUseDefault(true);//启用默认内置正则表达式过滤，默认会过滤css，js的url
        autoBaseApplicationTest.getConfig().setDepth(4);//设置爬取深度，默认就是4，第一个种子URL为1，从种子里面获取的URL为2，从深度为2的URL中获取的URL为3，超过4就不会爬取了
        autoBaseApplicationTest.getCrawler().addSeed("https://blog.jiangqiang.xin").setType("home").setLines(lines).setHeaders(headers)
                .setBodys(bodys).setConfigs(configs).setMethod(RequestMethod.GET);
        autoBaseApplicationTest.start();
    }
}
