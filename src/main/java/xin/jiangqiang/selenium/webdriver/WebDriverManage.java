package xin.jiangqiang.selenium.webdriver;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import xin.jiangqiang.config.Config;
import xin.jiangqiang.entities.Crawler;
import xin.jiangqiang.selenium.SeleniumHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jiangqiang
 * @date 2020/12/19 10:53
 */
@Slf4j
public class WebDriverManage {
    private final List<WebDr> webDrs;

    public WebDriverManage(Config config, SeleniumHelper seleniumHelper, Crawler crawler) {
        webDrs = new ArrayList<>(config.getThreads() * 2);
        while (webDrs.size() != config.getThreads()) {
            try {
                WebDriver webDriver = seleniumHelper.getDriver(crawler);
                webDrs.add(new WebDr(webDriver));
            } catch (Exception e) {
                log.info(e.getMessage());
            }
        }
    }

    public List<WebDr> getWebDrs() {
        return webDrs;
    }

    /**
     * @return 返回一个可以使用的WebDr
     */
    public synchronized WebDr getWebDr() {
        while (true) {
            for (WebDr webDr : webDrs) {
                if (!webDr.isRunning()) {
                    //正在运行中
                    webDr.setRunning(true);
                    return webDr;
                }
            }
        }
    }

    /**
     * 重置为非运行状态
     *
     * @param webDr 需要重置的webDr
     */
    public synchronized void resetWebDriver(WebDr webDr) {
        webDr.setRunning(false);
    }

    public void closeWebDriver() {
        for (WebDr webDr : webDrs) {
            try {
                webDr.getWebDriver().quit();
//                webDr.getWebDriver().close();
            } catch (Exception e) {
                log.info(e.getMessage());
            }
        }
    }
}
