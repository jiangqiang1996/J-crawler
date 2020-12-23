package xin.jiangqiang.selenium;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.openqa.selenium.WebDriver;
import xin.jiangqiang.config.Config;
import xin.jiangqiang.entities.Crawler;
import xin.jiangqiang.entities.Page;
import xin.jiangqiang.manage.Recorder;
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

    public synchronized static SeleniumHelper getInstance(Config config, Recorder record) {
        if (singleton == null) {
            singleton = new SeleniumHelperDefaultImpl(config, record);
        }
        return singleton;
    }

    private SeleniumHelperDefaultImpl(Config config, Recorder recorder) {
        super(config, recorder);
    }

    public Page request(WebHandler webHandler, Crawler crawler) {
        WebDriver driver = webHandler.getWebDriver();
        try {
            if (StringUtil.isEmpty(crawler.getUrl())) {
                return null;
            }
            log.info("url: " + crawler.getUrl());
            driver.get(crawler.getUrl());

            // 点击后要等待网页加载一段时间，然后才是最新的网页源码
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(config.getDelaytime()));
            String html = DriverUtil.getHtml(driver);
            //todo 最好能够获取到响应码和contentType
            return Page.getPage(crawler, null, "", html);
        } catch (Exception e) {
            log.info(e.getMessage());
            return null;
        }
    }

}
