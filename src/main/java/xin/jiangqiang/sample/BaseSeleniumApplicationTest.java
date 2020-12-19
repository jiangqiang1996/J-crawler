package xin.jiangqiang.sample;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jiangqiang
 * @date 2020/12/18 23:08
 */
@Slf4j
public class BaseSeleniumApplicationTest extends SeleniumApplication {

    /**
     * 模拟登录获取cookie,将cookie放入种子中
     *
     * @param webDriverManage
     * @param crawler
     */
    protected void init(WebDriverManage webDriverManage, Crawler crawler) {

        log.info("init执行了");
        WebDr dr = webDriverManage.getWebDr();
        WebDriver driver = dr.getWebDriver();
        System.out.println(dr.isRunning());


        driver.get("https://passport.csdn.net/login?code=public");
        driver.findElement(By.xpath("//*[@id=\"app\"]/div/div/div[1]/div[2]/div[5]/ul/li[2]/a")).click();
        driver.findElement(By.xpath("//*[@id=\"all\"]")).sendKeys("帐号");
        driver.findElement(By.xpath("//*[@id=\"password-number\"]")).sendKeys("密码");


        driver.findElement(By.xpath("//*[@id=\"app\"]/div/div/div[1]/div[2]/div[5]/div/div[6]/div/button")).click();

        CookieUtil.saveCookie(driver, "csdn.cookie.txt");

        String uri = driver.getCurrentUrl(); //获取登录后的新窗口的url
        System.out.println(uri);

        System.out.println("++++++++++++");
        dr.setRunning(false);//允许给其他线程池使用

    }

    @Before
    public void before() {

    }

    @Deal
    public void deal(Page page, Next next) {
        log.info("deal执行了");
        System.out.println(page.getHtml());
    }

    @After
    public void after() {
        log.info("after执行了");
    }

    public static void main(String[] args) {
        BaseSeleniumApplicationTest test = new BaseSeleniumApplicationTest();
//        test.setWebDriverBuilder(new WebDriverManagement(test.getConfig()));
        Map<String, String> lines = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        Map<String, String> bodys = new HashMap<>();
        Map<String, String> configs = new HashMap<>();
        test.getConfig().setDriverPath("D:\\tmp\\driver\\chromedriver_win32\\chromedriver.exe");
        test.getConfig().addRegEx("https://.*");//满足正则表达式的所有URL将会自动提取到deal生命周期的Next对象
        test.getConfig().setThreads(1);//使用selenium时不能太大
        test.getCrawler().addSeed("https://passport.csdn.net").setLines(lines).setHeaders(headers)
                .setBodys(bodys).setConfigs(configs).setMethod(RequestMethod.GET);
        test.getConfig().setIsHeadLess(false);
        test.start();
    }
}
