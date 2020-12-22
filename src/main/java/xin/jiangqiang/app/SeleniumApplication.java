package xin.jiangqiang.app;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import xin.jiangqiang.entities.Crawler;
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
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private WebHandlerManager webHandlerManager;//单例，维护一个标签页列表,此对象不支持修改

    public SeleniumApplication() {
        seleniumHelper = SeleniumHelperDefaultImpl.getInstance(config, recorder);
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
     * selenium模式独有方法
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
    public Runnable getTask(Crawler crawler) {
        return new Task(crawler);
    }

    @Data
    protected class Task implements Runnable {
        Crawler crawler;

        public Task(Crawler crawler) {
            this.crawler = crawler;
        }

        @Override
        public void run() {
            //获取一个可以使用的标签页
            WebHandler webHandler = webHandlerManager.getWebHandler();
            Page page = seleniumHelper.request(webHandler, crawler);
            if (page == null) {//出错 直接返回
                return;
            }
            //如果正正则或反正则列表有一个有值，就会抽取所有URL
            if (config.getRegExs().size() != 0 || config.getReverseRegExs().size() != 0) {
                //此处用于抓取所有URL
                List<String> urls = DocumentUtil.getAllUrl(page.getHtml(), crawler.getUrl());
                //使用正则表达式筛选URL
                urls = getMatchUrls(urls);
                page.addSeeds(urls, "");
            }
            webHandlerManager.resetWebHandler(webHandler);
            callMethodHelper.match(page);
            callMethodHelper.deal(page);
            //处理完成，加入成功结果集
            //todo selenium模式没有code,此处会报错,待修复
//            String code = page.getResponseCode().toString();
//            if (code.startsWith("2")) {
//                recorder.addSucc(page);
//            } else {
//                recorder.addErr(page);
//            }
            if (page.getDepth().equals(config.getDepth())) {//当前爬虫深度已经达到设置的级别，子爬虫不再爬取
                return;
            }
            //过滤下次爬取的URL
            filter.filter(page);
            for (Crawler crawler : page.getCrawlers()) {
                recorder.add(crawler);//记录没有爬取的
            }
        }
    }
}
