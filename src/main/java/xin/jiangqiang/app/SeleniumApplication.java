package xin.jiangqiang.app;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import xin.jiangqiang.entities.Crawler;
import xin.jiangqiang.entities.Next;
import xin.jiangqiang.entities.Page;
import xin.jiangqiang.selenium.SeleniumHelper;
import xin.jiangqiang.selenium.SeleniumHelperDefaultImpl;
import xin.jiangqiang.selenium.webdriver.WebDr;
import xin.jiangqiang.selenium.webdriver.WebDriverManage;
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
    private final SeleniumHelper seleniumHelper;
    private WebDriverManage webDriverManage;

    public SeleniumApplication() {
        seleniumHelper = new SeleniumHelperDefaultImpl(config, record);
    }

    @Override
    final protected void init() {
        super.init();
        webDriverManage = new WebDriverManage(config, seleniumHelper, crawler);
        init(webDriverManage, crawler);
    }

    protected void init(WebDriverManage webDriverManage, Crawler crawler) {

    }

    @Override
    public void clearResource() {
        super.clearResource();
        //结束前关闭所有driver
        if (webDriverManage != null) {
            log.info("开始关闭web驱动");
            webDriverManage.closeWebDriver();
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
            tmpCrawler.initDataFromCrawler(crawler);
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
                WebDr webDr = webDriverManage.getWebDr();
                Page page = seleniumHelper.request(webDr, crawler);
                webDriverManage.resetWebDriver(webDr);
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
