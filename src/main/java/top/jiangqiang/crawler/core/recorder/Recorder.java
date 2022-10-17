package top.jiangqiang.crawler.core.recorder;

import top.jiangqiang.crawler.core.config.CrawlerGlobalConfig;
import top.jiangqiang.crawler.core.entities.Crawler;

import java.util.List;
import java.util.function.Consumer;

/**
 * 记录各种爬取状态的爬虫
 *
 * @author Jiangqiang
 * @version 1.0
 * @description TODO
 * @date 2022/9/30 9:46
 */

public interface Recorder {

    /**
     * 用于初始化爬虫任务
     * 手动注入种子，在readSeeds方法后执行
     */
    Consumer<Recorder> getInitCallback();

    void setInitCallback(Consumer<Recorder> initCallback);

    /**
     * 获取全局配置
     *
     * @return
     */
    CrawlerGlobalConfig getConfig();

    void setConfig(CrawlerGlobalConfig globalConfig);

    /**
     * 此方法用于内存记录器的断点续爬
     * 程序结束前执行
     * 此方法并非是安全的，因为可能由于断电或程序突然崩溃等原因，机器根本没机会保存内存中的数据，
     * 所以建议使用持久化记录器，并在开始时重置状态。
     */
    void saveBeforeEnd();

    /**
     * 从持久化数据中读取爬虫种子
     * 爬虫任务开始前执行
     * 使用持久化记录器时，通常是通过修改任务的状态而不是删除任务，所以在此方法中通过重置状态达到续爬功能。
     * 例如，在此方法中将爬取失败的任务重新放进任务队列。
     */
    void initBeforeStart();

    /**
     * 存储未爬取的爬虫
     * 需要去重处理
     *
     * @param crawler 需要存储的爬虫
     */
    void add(Crawler crawler);

    /**
     * 批量存储未爬取的爬虫
     * 需要去重处理
     *
     * @param crawlers 需要存储的爬虫列表
     */
    void addAll(List<Crawler> crawlers);

    /**
     * 取出未爬取的爬虫
     * 取出后需要删除该爬虫
     */
    Crawler popOne();

    /**
     * 查询所有未爬取的爬虫
     */
    List<Crawler> getAll();

    /**
     * @return 查询未爬取的种子条数
     */
    Long count();

    /**
     * 记录爬取成功的
     *
     * @param crawler 成功爬取的爬虫
     */
    void addSuccess(Crawler crawler);

    /**
     * 查询爬取成功的
     *
     * @return 爬取成功的爬虫列表
     */
    List<Crawler> getAllSuccess();

    /**
     * @return 查询成功条数
     */
    Long countSuccess();

    /**
     * 记录失败的
     *
     * @param crawler 爬取失败的爬虫
     */
    void addError(Crawler crawler);

    /**
     * 查询爬取失败的
     *
     * @return 爬取失败的爬虫列表
     */
    List<Crawler> getAllError();

    /**
     * @return 查询失败条数
     */
    Long countError();

    /**
     * 正在进行的种子
     *
     * @param crawler
     */
    void addActive(Crawler crawler);

    /**
     * 爬取成功或失败后移除
     *
     * @param crawler
     */
    void removeActive(Crawler crawler);

    /**
     * 获取正在爬取中的任务
     *
     * @return
     */
    List<Crawler> getAllActive();

    /**
     * 正在爬取中的数量
     *
     * @return
     */
    Long countActive();

    /**
     * 是否已经存在此爬虫URL
     * 可能已经爬取失败，也可能还未爬取，或者正在爬取
     *
     * @return
     */
    Boolean exist(Crawler crawler);

    /**
     * 将等待队列中的一个任务移动到激活队列，并返回改任务
     *
     * @return
     */
    Crawler waitToActive();

    /**
     * 将正在进行的任务状态修改为成功
     *
     * @return
     */
    Crawler activeToSuccess(Crawler crawler);

    /**
     * 将正在进行的任务状态修改为失败
     *
     * @param crawler 失败的种子
     * @return
     */
    Crawler activeToError(Crawler crawler);

    /**
     * 获取错误信息，用于持久化
     *
     * @param exception
     * @return
     */
    String getErrorMessage(Exception exception);
}
