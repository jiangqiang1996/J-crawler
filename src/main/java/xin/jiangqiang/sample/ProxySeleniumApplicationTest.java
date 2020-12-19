package xin.jiangqiang.sample;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import xin.jiangqiang.annotation.After;
import xin.jiangqiang.annotation.Before;
import xin.jiangqiang.annotation.Deal;
import xin.jiangqiang.app.SeleniumApplication;
import xin.jiangqiang.entities.Crawler;
import xin.jiangqiang.entities.Next;
import xin.jiangqiang.entities.Page;
import xin.jiangqiang.net.RequestMethod;
import xin.jiangqiang.selenium.webdriver.WebDr;
import xin.jiangqiang.selenium.webdriver.WebDriverManage;
import xin.jiangqiang.util.CookieUtil;

import java.util.HashMap;
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
     * @param webDriverManage
     * @param crawler
     */
    protected void init(WebDriverManage webDriverManage, Crawler crawler) {
        WebDr webDr = webDriverManage.getWebDr();
        WebDriver driver = webDr.getWebDriver();
        driver.get("https://www.pixiv.net/");
        driver.findElement(By.xpath("//*[@id=\"wrapper\"]/div[3]/div[2]/a[2]")).click();
        driver.findElement(By.xpath("//*[@id=\"LoginComponent\"]/form/div[1]/div[1]/input")).sendKeys("********");
        driver.findElement(By.xpath("//*[@id=\"LoginComponent\"]/form/div[1]/div[2]/input")).sendKeys("******");
        driver.findElement(By.xpath("//*[@id=\"LoginComponent\"]/form/button")).click();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(driver.manage().getCookies());
        crawler.setCookies(driver.manage().getCookies());
        Set<Cookie> cookieSet = driver.manage().getCookies();

        for (WebDr tmpWebDr : webDriverManage.getWebDrs()) {
            if (webDr != tmpWebDr) {
                tmpWebDr.getWebDriver().get("https://www.pixiv.net/");
                for (Cookie cookie : cookieSet) {
                    tmpWebDr.getWebDriver().manage().addCookie(cookie);
                }
                tmpWebDr.getWebDriver().navigate().refresh();
            }
        }
        webDr.setRunning(false);
    }

    @Before
    public void before() {

    }

    @Deal
    public void deal(Page page, Next next) {
        log.info("deal执行了");
        System.out.println(page.getHtml());
        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @After
    public void after() {
        log.info("after执行了");
    }

    public static void main(String[] args) {
        ProxySeleniumApplicationTest test = new ProxySeleniumApplicationTest();
//        test.setWebDriverBuilder(new WebDriverManagement(test.getConfig()));
        Map<String, String> configs = new HashMap<>();
        test.getConfig().setDriverPath("D:\\tmp\\driver\\chromedriver_win32\\chromedriver.exe");
        test.getConfig().addRegEx("https://.*");//满足正则表达式的所有URL将会自动提取到deal生命周期的Next对象
        test.getConfig().setThreads(3);//使用selenium时不能太大
        test.getCrawler().addSeed("https://www.pixiv.net/");
        test.getConfig().setIsHeadLess(false);
        configs.put("IP", "127.0.0.1");
        configs.put("port", "8001");
        test.getCrawler().setConfigs(configs);
        test.start();
    }
}
