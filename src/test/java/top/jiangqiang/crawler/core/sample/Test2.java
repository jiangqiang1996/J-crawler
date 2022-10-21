package top.jiangqiang.crawler.core.sample;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import top.jiangqiang.crawler.core.app.GenericStarter;
import top.jiangqiang.crawler.core.config.CrawlerGlobalConfig;
import top.jiangqiang.crawler.core.config.LoginConfig;
import top.jiangqiang.crawler.core.entities.Crawler;
import top.jiangqiang.crawler.core.entities.Page;
import top.jiangqiang.crawler.core.handler.ResultHandler;
import top.jiangqiang.crawler.core.recorder.RamRecorder;
import top.jiangqiang.crawler.core.recorder.Recorder;
import top.jiangqiang.crawler.core.util.HttpUtil;

import java.util.List;

/**
 * @author Jiangqiang
 * @version 1.0
 * @description TODO
 * @date 2022/10/10 9:39
 */
@Slf4j
public class Test2 {
    public static void main(String[] args) {
        fetch1();
    }

    /**
     * 爬取微信公众号文章的图片
     */
    static void fetch1() {
        RamRecorder ramRecorder = new RamRecorder();
        ramRecorder.add(new Crawler("https://gitee.com/"));
        CrawlerGlobalConfig crawlerGlobalConfig = new CrawlerGlobalConfig();
//        crawlerGlobalConfig.addRegEx("(http|https)://.*");
        crawlerGlobalConfig.setAllowEnd(true);
        crawlerGlobalConfig.setForceEnd(true);
        crawlerGlobalConfig.setDepth(3);
        LoginConfig loginConfig = null;
//        此接口会在爬虫任务开始前请求，并把响应头全部作为后续爬虫任务的请求头
//        loginConfig = new LoginConfig("https://***/login");
//        loginConfig.setMethod("POST");
//        loginConfig.addBody("username", "suixin");
//        loginConfig.addBody("password", "123456");
//        loginConfig.setContentType(LoginConfig.APPLICATION_JSON_UTF8);
//        loginConfig.setFilter(headers -> {
//            headers.forEach(header -> {
//                System.out.println(header.getFirst() + ": " + header.getSecond());
//            });
//            return headers;
//        });
        HttpUtil.level = HttpLoggingInterceptor.Level.HEADERS;
        crawlerGlobalConfig.setLogLevel(HttpLoggingInterceptor.Level.HEADERS);
        new GenericStarter(crawlerGlobalConfig, ramRecorder, loginConfig, new ResultHandler() {
            public List<Crawler> doSuccess(Recorder recorder, Crawler crawler, Page page, Response response) {

                return page.getCrawlers();
            }

        }).start();
    }

}
