package xin.jiangqiang.selenium;

import org.openqa.selenium.Proxy;
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
import xin.jiangqiang.config.Config;
import xin.jiangqiang.entities.Crawler;
import xin.jiangqiang.entities.Page;
import xin.jiangqiang.management.Record;
import xin.jiangqiang.selenium.webdriver.WebHandler;
import xin.jiangqiang.selenium.webdriver.WebHandler;
import xin.jiangqiang.util.StringUtil;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jiangqiang
 * @date 2020/12/18 23:24
 */
public abstract class AbstractSeleniumHelper implements SeleniumHelper {
    Config config;
    Record record;

    public AbstractSeleniumHelper(Config config, Record record) {
        this.config = config;
        this.record = record;
    }

    public AbstractSeleniumHelper() {
    }

    public abstract Page request(WebHandler webHandler, Crawler crawler);

    @Override
    public final WebDriver getDriver(Crawler crawler) {
        WebDriver driver = null;
        if ("Firefox".equalsIgnoreCase(config.getBrowserType())) {
            driver = getFirefoxDriver(crawler);
        } else if ("Chrome".equalsIgnoreCase(config.getBrowserType())) {
            driver = getChromeDriver(crawler);
        } else if ("IE".equalsIgnoreCase(config.getBrowserType())) {
            driver = getInternetExplorerDriver(crawler);
        } else if ("Edge".equalsIgnoreCase(config.getBrowserType())) {
            driver = getEdgeDriver(crawler);
        } else if ("Opera".equalsIgnoreCase(config.getBrowserType())) {
            driver = getOperaDriver(crawler);
        } else {
            driver = getChromeDriver(crawler);
        }
        driver.manage().window().maximize();
        driver.manage().deleteAllCookies();
        // 与浏览器同步非常重要，必须等待浏览器加载完毕
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        return driver;
    }

    public WebDriver getChromeDriver(Crawler crawler) {
        System.setProperty("webdriver.chrome.driver", config.getDriverPath());
        ChromeOptions options = new ChromeOptions();
        Proxy proxy = getProxy(crawler);
        if (proxy != null) {
            options.setProxy(proxy);
        }
        options.addArguments("--disable-extensions");
        options.addArguments("--no-sandbox");
        if (config.getIsHeadLess()) {
            options.setHeadless(true);
            options.addArguments("--disable-gpu");
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("images", 2);//谷歌浏览器不能禁止CSS，只能禁止图片
//        map.put("javascript", 2);//禁止js
        HashMap<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_setting_values", map);
        options.setExperimentalOption("prefs", prefs);

        if (StringUtil.isNotEmpty(config.getBinaryPath())) {
            options.setBinary(config.getBinaryPath());
        }
        return new ChromeDriver(options);
    }

    public WebDriver getFirefoxDriver(Crawler crawler) {
        System.setProperty("webdriver.gecko.driver", config.getDriverPath());
        FirefoxOptions options = new FirefoxOptions();
        Proxy proxy = getProxy(crawler);
        if (proxy != null) {
            options.setProxy(proxy);
        }
        options.addArguments("--disable-extensions");
        options.addArguments("--no-sandbox");
        if (config.getIsHeadLess()) {
            options.setHeadless(true);
            options.addArguments("--disable-gpu");
        }
        if (StringUtil.isNotEmpty(config.getBinaryPath())) {
            options.setBinary(config.getBinaryPath());
        }
        return new FirefoxDriver(options);
    }

    public WebDriver getInternetExplorerDriver(Crawler crawler) {
        System.setProperty("webdriver.ie.driver", config.getDriverPath());
        InternetExplorerOptions options = new InternetExplorerOptions();
        Proxy proxy = getProxy(crawler);
        if (proxy != null) {
            options.setProxy(proxy);
        }
        return new InternetExplorerDriver(options);
    }

    public WebDriver getEdgeDriver(Crawler crawler) {
        System.setProperty("webdriver.edge.driver", config.getDriverPath());
        EdgeOptions options = new EdgeOptions();
        Proxy proxy = getProxy(crawler);
        if (proxy != null) {
            options.setProxy(proxy);
        }
        options.addArguments("--disable-extensions");
        options.addArguments("--no-sandbox");
        if (config.getIsHeadLess()) {
            options.setHeadless(true);
            options.addArguments("--disable-gpu");
        }
        if (StringUtil.isNotEmpty(config.getBinaryPath())) {
            options.setBinary(config.getBinaryPath());
        }
        return new EdgeDriver(options);
    }

    public WebDriver getOperaDriver(Crawler crawler) {
        System.setProperty("webdriver.opera.driver", config.getDriverPath());
        OperaOptions options = new OperaOptions();
        Proxy proxy = getProxy(crawler);
        if (proxy != null) {
            options.setProxy(proxy);
        }
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        if (StringUtil.isNotEmpty(config.getBinaryPath())) {
            options.setBinary(config.getBinaryPath());
        }
        return new OperaDriver(options);
    }

    private Proxy getProxy(Crawler crawler) {
        Map<String, String> configs = crawler.getConfigs();
        String IP = configs.get("IP");
        String port = configs.get("port");
        String protocol = configs.get("protocol");
        String username = configs.get("username");//暂时不支持带身份验证的代理
        String password = configs.get("password");
        Proxy proxy = new Proxy();
        if (StringUtil.isNotEmpty(IP) && StringUtil.isNotEmpty(port)) {
            proxy.setHttpProxy(IP + ":" + port).setSslProxy(IP + ":" + port);
            return proxy;
        } else {
            return null;
        }
    }
}
