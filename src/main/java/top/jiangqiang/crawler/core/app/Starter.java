package top.jiangqiang.crawler.core.app;

import cn.hutool.core.thread.ThreadUtil;
import okhttp3.Headers;
import top.jiangqiang.crawler.core.config.CrawlerGlobalConfig;
import top.jiangqiang.crawler.core.config.LoginConfig;
import top.jiangqiang.crawler.core.entities.Crawler;
import top.jiangqiang.crawler.core.http.OkHttpService;
import top.jiangqiang.crawler.core.recorder.Recorder;

import java.util.concurrent.ExecutorService;

/**
 * @Author: JiangQiang
 * @Date: 2022年05月14日 11:04
 */
public interface Starter {

    /**
     * 注入全局配置
     *
     * @param config
     */
    void init(CrawlerGlobalConfig config);

    OkHttpService getOkHttpService();

    /**
     * 过滤掉某些header，返回的header将被加入全局配置中的header中
     *
     * @param headers
     * @return
     */
    Headers filterHeaders(Headers headers);

    /**
     * 登录配置
     *
     * @return
     */
    LoginConfig getLoginConfig();

    void setLoginConfig(LoginConfig loginConfig);

    /**
     * 每一个爬虫任务的逻辑
     *
     * @param crawler
     * @return
     */
    Runnable getTask(Crawler crawler);

    CrawlerGlobalConfig getGlobalConfig();

    Recorder getRecorder();

    /**
     * 初始化线程池
     *
     * @return
     */
    default ExecutorService initExecutorService() {
        return ThreadUtil.newExecutor(getGlobalConfig().getThreads() / 2, getGlobalConfig().getThreads(), 1000);
    }
}
