package xin.jiangqiang.selenium.webdriver;

import org.openqa.selenium.WebDriver;
import xin.jiangqiang.selenium.SeleniumHelper;

/**
 * 管理标签页
 * 拥有一个单例的WebDriver，以及标签页句柄字符串
 * 每一个实例都表示一个具体的标签页
 *
 * @author jiangqiang
 * @date 2020/12/19 10:36
 */
public class WebHandler {
    /**
     * 是否运行中
     */
    private boolean isRunning = false;
    private final WebDriver webDriver;
    private final String handler;

    public WebHandler(WebDriver webDriver, String handler) {
        this.webDriver = webDriver;
        this.handler = handler;
    }

    public String getHandler() {
        return handler;
    }

    public WebDriver getWebDriver() {
        return webDriver;
    }

    public synchronized boolean isRunning() {
        return isRunning;
    }

    //只能通过WebHandlerManager的实例来重置状态
    synchronized void setRunning(boolean running) {
        isRunning = running;
    }

    /**
     * 切换到当前标签页
     */
    public synchronized void switchToThis() {
        this.webDriver.switchTo().window(this.handler);
    }
}
