package xin.jiangqiang.selenium;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.openqa.selenium.WebDriver;
import xin.jiangqiang.config.Config;
import xin.jiangqiang.entities.Crawler;
import xin.jiangqiang.entities.Page;
import xin.jiangqiang.management.Record;
import xin.jiangqiang.selenium.webdriver.WebHandler;
import xin.jiangqiang.util.DriverUtil;
import xin.jiangqiang.util.StringUtil;

import java.time.Duration;

/**
 * @author jiangqiang
 * @date 2020/12/18 23:10
 */
@Slf4j
public class SeleniumHelperDefaultImpl extends AbstractSeleniumHelper {
    private static SeleniumHelper singleton = null;

    public synchronized static SeleniumHelper getInstance(Config config, Record record) {
        if (singleton == null) {
            singleton = new SeleniumHelperDefaultImpl(config, record);
        }
        return singleton;
    }

    private SeleniumHelperDefaultImpl(Config config, Record record) {
        super(config, record);
    }

    public Page request(WebHandler webHandler, Crawler crawler) {
        WebDriver driver = webHandler.getWebDriver();
        try {
            if (StringUtil.isEmpty(crawler.getUrl())) {
                return null;
            }
            //判断是否成功爬取过该URL
            if (!record.hasUrl(crawler.getUrl())) {
                log.info("url: " + crawler.getUrl());
                driver.get(crawler.getUrl());
                //获取cookie,因为只有一个driver实例，因此没有必要
//                for (org.openqa.selenium.Cookie cookie : crawler.getCookies()) {
//                    driver.manage().addCookie(cookie);
//                }
                // 点击后要等待网页加载一段时间，然后才是最新的网页源码
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(config.getDelaytime()));
                Page page = new Page();
                String html = DriverUtil.getHtml(driver);
                page.setHtml(html);
                page.setUrl(driver.getCurrentUrl());
                if (StringUtil.isNotEmpty(html)) {
                    page.setDocument(Jsoup.parse(html, driver.getCurrentUrl()));
                }
                page.initDataFromCrawler(crawler);
                return page;
            }
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return null;
    }

}
