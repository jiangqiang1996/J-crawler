package xin.jiangqiang.sample;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import xin.jiangqiang.annotation.After;
import xin.jiangqiang.annotation.Before;
import xin.jiangqiang.annotation.Deal;
import xin.jiangqiang.annotation.Match;
import xin.jiangqiang.app.SeleniumApplication;
import xin.jiangqiang.entities.Crawler;
import xin.jiangqiang.entities.Next;
import xin.jiangqiang.entities.Page;
import xin.jiangqiang.selenium.webdriver.WebHandler;
import xin.jiangqiang.selenium.webdriver.WebHandlerManager;
import xin.jiangqiang.util.CookieUtil;
import xin.jiangqiang.util.DocumentUtil;
import xin.jiangqiang.util.FileUtil;
import xin.jiangqiang.util.HttpUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author jiangqiang
 * @date 2020/12/18 23:08
 */
@Slf4j
public class ProxySeleniumApplicationTest extends SeleniumApplication {

    /**
     * 模拟登录获取cookie,将cookie放入种子中
     *
     * @param webHandlerManager
     * @param crawler
     */
    protected void init(WebHandlerManager webHandlerManager, Crawler crawler) {
        WebHandler webHandler = webHandlerManager.getWebHandler();
        WebDriver driver = webHandler.getWebDriver();

//        driver.get("https://www.pixiv.net/");//此处报错多是网络问题，自己检查代理
//        driver.findElement(By.xpath("//*[@id=\"wrapper\"]/div[3]/div[2]/a[2]")).click();
//        driver.findElement(By.xpath("//*[@id=\"LoginComponent\"]/form/div[1]/div[1]/input"))
//                .sendKeys("jq125dasdasd");//自己换成自己的帐号
//        driver.findElement(By.xpath("//*[@id=\"LoginComponent\"]/form/div[1]/div[2]/input"))
//                .sendKeys("1996asda");//密码
//        driver.findElement(By.xpath("//*[@id=\"LoginComponent\"]/form/button")).click();
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        CookieUtil.saveCookie(driver, "cookie.txt");

        //直接从文件读取cookie，上面部分代码可以模拟登录 保存cookie到文件，之后启动程序可以注释上面部分，使用此行代码即可
        CookieUtil.getCookie(driver, "cookie.txt", "https://www.pixiv.net/");

//        String url = driver.getCurrentUrl();
//        webHandlerManager.getWebHandler().getWebDriver().get(url);
//        webHandlerManager.getWebHandler().getWebDriver().get(url);

    }

    @Before
    public void before() {

    }

    /**
     * 处理列表页面的URL，提取封面图片
     * 提取详情页的URL
     *
     * @param page
     * @param next
     */
    @Match("list")
    public void matchList(Page page, Next next) {
        //抓取rp5asc-10 leQnFG类的img标签的URL
        Elements imgs = page.getDocument().select("img.rp5asc-10.leQnFG[src]");
        for (Element img : imgs) {
            String url = img.attr("abs:src");//这里是封面的图片链接，可以选择不保存
            System.out.println(url);


            Map<String, String> proxyConfigs = new HashMap<>();
            //根据自己情况修改
            proxyConfigs.put("IP", "127.0.0.1");
            proxyConfigs.put("port", "8001");

            Map<String, String> headers = new HashMap<>();
            headers.put("referer", page.getUrl());
            headers.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.25 Safari/537.36 Core/1.70.3861.400 QQBrowser/10.7.4313.400");
            FileUtil.saveFileFromURL("D:\\tmp\\20201219", url, proxyConfigs, null, headers, null);

        }
        //抓取a标签的URL
        Elements hrefs = page.getDocument().select("a.rp5asc-16.kdmVAX.sc-AxjAm.MksUu[href]");
        for (Element href : hrefs) {
            String url = href.attr("abs:href");
            next.addSeed(url).setType("content");
        }
    }

    @Match("content")
    public void matchContent(Page page, Next next) {
        //抓取img标签的URL
        Elements imgs = page.getDocument().select("img.sc-1qpw8k9-1.fvHoJ[src]");
        for (Element img : imgs) {
            String url = img.attr("abs:src");
            System.out.println(url);

            Map<String, String> proxyConfigs = new HashMap<>();
            //根据自己情况修改
            proxyConfigs.put("IP", "127.0.0.1");
            proxyConfigs.put("port", "8001");

            Map<String, String> headers = new HashMap<>();
            headers.put("referer", page.getUrl());
            headers.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.25 Safari/537.36 Core/1.70.3861.400 QQBrowser/10.7.4313.400");
            FileUtil.saveFileFromURL("D:\\tmp\\20201219\\content", url, proxyConfigs, null, headers, null);//这里是内容图片
        }
    }

    @Deal
    public void deal(Page page, Next next) {
//        System.out.println(page.getHtml());


//        List<String> urls = DocumentUtil.getAllUrl(page.getHtml(), page.getUrl());//手动抽取所有URL，实际上直接在main方法中使用正则表达式更好
//        for (String url : urls) {
//            log.info(url);
//            //判断类型，不建议使用，因为会重复发请求，降低效率
//            String contentType = HttpUtil.getContentType(url, "127.0.0.1", 8001);
//            if (contentType.startsWith("image")) {
//                FileUtil.saveFileFromURL("D:/tmp/20201219", url);
//            } else {
//                next.addSeeds(urls, "content");
//            }
//        }
    }

    @After
    public void after() {
        log.info("after执行了");
    }

    public static void main(String[] args) throws IOException {
        ProxySeleniumApplicationTest test = new ProxySeleniumApplicationTest();
//        test.setWebDriverBuilder(new WebDriverManagement(test.getConfig()));
        Map<String, String> configs = new HashMap<>();
        test.getConfig().setDriverPath("D:\\tmp\\driver\\chromedriver_win32\\chromedriver.exe");
//        test.getConfig().addRegEx("https://.*");//满足正则表达式的所有URL将会自动提取到deal生命周期的Next对象
        test.getConfig().setThreads(3);//使用selenium时不能太大
        test.getCrawler().addSeed("https://www.pixiv.net/tags/%E3%82%AA%E3%83%AA%E3%82%AD%E3%83%A3%E3%83%A9/illustrations")
                .setType("list");
        test.getConfig().setIsHeadLess(false);
        configs.put("IP", "127.0.0.1");//配置代理
        configs.put("port", "8001");
        test.getCrawler().setConfigs(configs);
        test.start();
    }
}
