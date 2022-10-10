package top.jiangqiang.crawler.core.app;

import cn.hutool.core.thread.ThreadUtil;
import top.jiangqiang.crawler.core.config.CrawlerGlobalConfig;
import top.jiangqiang.crawler.core.entities.Crawler;
import top.jiangqiang.crawler.core.recorder.Recorder;

import java.util.concurrent.ExecutorService;

/**
 * @Author: JiangQiang
 * @Date: 2022年05月14日 11:04
 */
public interface Starter {

    default void init(Recorder recorder) {
    }

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
