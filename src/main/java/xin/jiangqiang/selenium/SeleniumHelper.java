package xin.jiangqiang.selenium;

import org.openqa.selenium.WebDriver;
import xin.jiangqiang.entities.Crawler;
import xin.jiangqiang.entities.Page;
import xin.jiangqiang.selenium.webdriver.WebHandler;

/**
 * 创建驱动并且发送请求的接口
 *
 * @author jiangqiang
 * @date 2020/12/18 23:14
 */
public interface SeleniumHelper {

    WebDriver getDriver(Crawler crawler);

    Page request(WebHandler driver, Crawler crawler);

    WebDriver getChromeDriver(Crawler crawler);

    WebDriver getFirefoxDriver(Crawler crawler);

    WebDriver getInternetExplorerDriver(Crawler crawler);

    WebDriver getEdgeDriver(Crawler crawler);

    WebDriver getOperaDriver(Crawler crawler);
}
