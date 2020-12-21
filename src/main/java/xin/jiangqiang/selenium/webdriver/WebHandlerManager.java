package xin.jiangqiang.selenium.webdriver;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WindowType;
import xin.jiangqiang.config.Config;
import xin.jiangqiang.entities.Crawler;
import xin.jiangqiang.selenium.SeleniumHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 单例模式
 * 此对象用于维护一个List，存放标签页
 *
 * @author jiangqiang
 * @date 2020/12/19 20:37
 */
@Slf4j
public class WebHandlerManager {
    private static WebHandlerManager singleton = null;
    private final List<WebHandler> webHandlers;

    private WebHandlerManager(Config config, SeleniumHelper seleniumHelper, Crawler crawler) {
        WebDriver webDriver = seleniumHelper.getDriver(crawler);
        webHandlers = new ArrayList<>(config.getThreads() * 2);
        for (int i = 0; i < config.getThreads() - 1; i++) {
            try {
                webDriver.switchTo().newWindow(WindowType.TAB);
            } catch (Exception e) {
                log.info(e.getMessage());
                e.printStackTrace();
            }
        }
        Set<String> windowHandles = webDriver.getWindowHandles();
        for (String handle : windowHandles) {
            webHandlers.add(new WebHandler(webDriver, handle));
        }
    }

    public synchronized static WebHandlerManager getInstance(Config config, SeleniumHelper seleniumHelper, Crawler crawler) {
        if (singleton == null) {
            singleton = new WebHandlerManager(config, seleniumHelper, crawler);
        }
        return singleton;
    }

    public List<WebHandler> getWebHandlers() {
        return webHandlers;
    }

    /**
     * 使用完必须重置状态，否则不可重复使用
     * 根据标记状态判断能否使用
     * 一旦可以使用则切换到该标签页,并且标记为不可使用状态
     *
     * @return 获取一个可以使用的标签页
     */
    public synchronized WebHandler getWebHandler() {
        while (true) {
            for (WebHandler webHandler : webHandlers) {
                if (!webHandler.isRunning()) {//没有运行
                    //设置为正在运行中
                    webHandler.setRunning(true);
                    webHandler.switchToThis();
                    return webHandler;
                }
            }
        }
    }

    /**
     * 重置为非运行状态
     */
    public synchronized void resetWebHandler(WebHandler handler) {
        handler.setRunning(false);
    }

    /**
     * 在init方法内可以使用此方法，在爬虫正式开始后，慎用
     *
     * @param
     */
    public synchronized void resetAllWebHandler() {
        for (WebHandler webHandler : webHandlers) {
            resetWebHandler(webHandler);
        }
    }

    public void closeWebDriver() {
        webHandlers.get(0).getWebDriver().quit();
    }
}