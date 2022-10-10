package top.jiangqiang.crawler.core.recorder;

import top.jiangqiang.crawler.core.config.CrawlerGlobalConfig;
import top.jiangqiang.crawler.core.entities.Crawler;

import java.util.List;

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
    Callback getInitCallback();

    void setInitCallback(Callback initCallback);

    void setConfig(CrawlerGlobalConfig globalConfig);

    CrawlerGlobalConfig getConfig();

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
     * 此方法用于内存记录器的断点续爬
     * 程序结束前执行
     */
    void saveBeforeEnd();

    /**
     * 从持久化数据中读取爬虫种子
     * 爬虫任务开始前执行
     */
    void initBeforeStart();

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
     * 获取正在爬取中的任务
     *
     * @return
     */
    List<Crawler> getAllActive();

    /**
     * @return 查询未爬取的种子条数
     */
    Integer count();

    /**
     * @return 查询成功条数
     */
    Integer countSuccess();

    /**
     * @return 查询失败条数
     */
    Integer countError();

    /**
     * 正在爬取中的数量
     *
     * @return
     */
    Integer countActive();

    /**
     * 是否已经存在此爬虫URL
     * 可能已经爬取失败，也可能还未爬取，或者正在爬取
     *
     * @return
     */
    Boolean exist(Crawler crawler);

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
}
