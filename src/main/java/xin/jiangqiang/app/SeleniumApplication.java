package xin.jiangqiang.app;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import xin.jiangqiang.entities.Crawler;
import xin.jiangqiang.entities.Next;
import xin.jiangqiang.entities.Page;
import xin.jiangqiang.selenium.SeleniumHelper;
import xin.jiangqiang.selenium.SeleniumHelperDefaultImpl;
import xin.jiangqiang.selenium.webdriver.WebHandler;
import xin.jiangqiang.selenium.webdriver.WebHandlerManager;
import xin.jiangqiang.util.DocumentUtil;
import xin.jiangqiang.util.RegExpUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * @author jiangqiang
 * @date 2020/12/17 21:53
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class SeleniumApplication extends AbstractStarter {
    private final SeleniumHelper seleniumHelper;//单例，用来创建驱动，以及发送请求，将页面转换为Page对象
    private WebHandlerManager webHandlerManager;//单例，维护一个标签页列表

    public SeleniumApplication() {
        seleniumHelper = SeleniumHelperDefaultImpl.getInstance(config, record);
    }

    @Override
    final protected void init() {
        super.init();
        //使用seleniumHelper来创建一个驱动，并且创建多个标签页进行维护
        webHandlerManager = WebHandlerManager.getInstance(config, seleniumHelper, crawler);
        init(webHandlerManager, crawler);
        //重置状态，此处是单线程，必须重置，否则后面拿不到
        webHandlerManager.resetAllWebHandler();
    }

    /**
     * 此方法专门用于子类重写，方便实现自定义功能，比如爬虫启动前模拟登录操作，获取cookie
     *
     * @param webHandlerManager 维护标签页的对象
     * @param crawler           爬虫初始化种子，可以往里面放一些从登录操作获取的数据
     */
    protected void init(WebHandlerManager webHandlerManager, Crawler crawler) {

    }

    @Override
    public void clearResource() {
        super.clearResource();
        //结束前关闭所有driver
        if (webHandlerManager != null) {
            log.info("开始关闭web驱动");
            webHandlerManager.closeWebDriver();
            log.info("关闭web驱动完成");
        }
    }

    @Override
    public final void run() {
        executor = Executors.newFixedThreadPool(config.getThreads());
        for (Crawler crawler : initCrawlers) {
            Runnable runnable = new Task(crawler);
            executor.execute(runnable);
        }
        for (Crawler tmpCrawler : crawler.getCrawlers()) {
            //快速继承init方法中放的一些数据
//            tmpCrawler.initDataFromCrawler(crawler);
            Runnable runnable = new Task(tmpCrawler);
            executor.execute(runnable);
        }
    }

    @Data
    protected class Task implements Runnable {
        Crawler crawler;

        public Task(Crawler crawler) {
            this.crawler = crawler;
            crawlers.add(crawler);
        }

        @Override
        public void run() {
            if (crawler.getDepth() <= config.getDepth()) {
                Next next = new Next();
                //获取一个可以使用的标签页
                WebHandler webHandler = webHandlerManager.getWebHandler();
                Page page = seleniumHelper.request(webHandler, crawler);
                webHandlerManager.resetWebHandler(webHandler);
                if (page == null) {//出错 直接返回
                    crawlers.remove(crawler);
                    return;
                }
                //如果正正则或反正则列表有一个有值，就会抽取所有URL
                if (config.getRegExs().size() != 0 || config.getReverseRegExs().size() != 0) {
                    //此处用于抓取所有URL
                    List<String> urls = DocumentUtil.getAllUrl(page.getHtml(), crawler.getUrl());
                    //使用正则表达式筛选URL
                    urls = getMatchUrls(urls);
                    next.addSeeds(urls, "");
                }
                //获取元数据,深度,类型,cookie
                next.initDataFromCrawler(page);
                next.setDepth(crawler.getDepth() + 1);

                callMethodHelper.match(page, next);
                callMethodHelper.deal(page, next);
                //处理完成，加入成功结果集
//                String code = page.getResponseCode().toString();
//                if (code.startsWith("4") || code.startsWith("5")) {
//                    record.addErr(page.getUrl());
//                } else {
//                    record.addSucc(page.getUrl());
//                }
                crawlers.remove(crawler);
                //过滤下次爬取的URL
                filter.filter(next, page);
                for (Crawler tmpCrawler : next.getCrawlers()) {
                    tmpCrawler.initDataFromCrawler(next);
                    Runnable runnable = new Task(tmpCrawler);
                    executor.execute(runnable);
                }
            } else {
                crawlers.remove(crawler);
            }
        }
    }

    /**
     * 逆正则优先级更高
     * 一旦匹配逆正则，则不会匹配正正则
     *
     * @param urls 链接
     * @return
     */
    private List<String> getMatchUrls(List<String> urls) {
        if (urls == null || urls.size() == 0) {
            return new ArrayList<>();
        }
        List<String> regExs = config.getRegExs();
        List<String> reverseRegExs = config.getReverseRegExs();
        List<String> defaultReverseRegExs = config.getIsUseDefault() ? config.getDefaultReverseRegExs() : new ArrayList<>();
        List<String> tmpUrls = new ArrayList<>();
        Iterator<String> interator = urls.iterator();
        OUT:
        while (interator.hasNext()) {
            String url = interator.next();

            for (String defaultReverseRegEx : defaultReverseRegExs) {
                if (RegExpUtil.isMatch(url, defaultReverseRegEx)) {
                    continue OUT;
                }
            }

            for (String reverseRegEx : reverseRegExs) {
                if (RegExpUtil.isMatch(url, reverseRegEx)) {
                    continue OUT;
                }
            }
            for (String regEx : regExs) {
                if (RegExpUtil.isMatch(url, regEx)) {
                    tmpUrls.add(url);
                    continue OUT;
                }
            }
        }
        return tmpUrls;
    }
}
