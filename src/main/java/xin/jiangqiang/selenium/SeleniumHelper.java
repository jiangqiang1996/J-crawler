package xin.jiangqiang.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaOptions;
import xin.jiangqiang.entities.Crawler;
import xin.jiangqiang.entities.Page;
import xin.jiangqiang.selenium.webdriver.WebDr;
import xin.jiangqiang.util.StringUtil;

/**
 * @author jiangqiang
 * @date 2020/12/18 23:14
 */
public interface SeleniumHelper {

    WebDriver getDriver(Crawler crawler);

    Page request(WebDr driver, Crawler crawler);

    WebDriver getChromeDriver(Crawler crawler);

    WebDriver getFirefoxDriver(Crawler crawler);

    WebDriver getInternetExplorerDriver(Crawler crawler);

    WebDriver getEdgeDriver(Crawler crawler);

    WebDriver getOperaDriver(Crawler crawler);
}
