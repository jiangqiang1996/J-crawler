package xin.jiangqiang.selenium.webdriver;

import org.openqa.selenium.WebDriver;
import xin.jiangqiang.selenium.SeleniumHelper;

/**
 * @author jiangqiang
 * @date 2020/12/19 10:36
 */
public class WebDr {
    /**
     * 是否运行中
     */
    private boolean isRunning = false;
    private final WebDriver webDriver;

    public WebDr(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    public WebDriver getWebDriver() {
        return webDriver;
    }

    public synchronized boolean isRunning() {
        return isRunning;
    }

    public synchronized void setRunning(boolean running) {
        isRunning = running;
    }
}
