package top.jiangqiang.sample.jiangqiang.core.app;

import top.jiangqiang.sample.jiangqiang.core.entities.Crawler;

/**
 * @Author: JiangQiang
 * @Date: 2022年05月14日 11:04
 */
public interface Starter {
    void start(Class<? extends Starter> clazz);

    /**
     * 程序没有正常执行完时会执行此方法
     */
    void saveAndClearResource();

    /**
     * 每一个爬虫任务的逻辑
     *
     * @param crawler
     * @return
     */
    Runnable getTask(Crawler crawler);
}
