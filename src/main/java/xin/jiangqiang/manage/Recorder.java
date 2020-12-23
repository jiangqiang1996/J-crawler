package xin.jiangqiang.manage;

import xin.jiangqiang.config.Config;
import xin.jiangqiang.entities.Crawler;

import java.util.List;

/**
 * 记录各种爬取状态的爬虫
 */
public interface Recorder {
    /**
     * 存储未爬取的爬虫
     * 需要去重处理
     * @param crawler 需要存储的爬虫
     */
    void add(Crawler crawler);

    /**
     * 存储未爬取的爬虫
     * 需要去重处理
     * @param crawlers 需要存储的爬虫列表
     */
    void addAll(List<Crawler> crawlers);

    /**
     * 此方法用于内存记录器的断点续爬
     * 结束前保存没有爬取的爬虫到文件，而数据库记录器无需此功能
     * 内存记录器可以配置读取路径以及是否断点续爬,而数据库记录器只要数据表中有数据,则都会被爬取
     */
    void saveBeforeEnd(Config config);

    /**
     * 此方法用于内存记录器的断点续爬
     * 初始化文件中的数据，保证getOne或getAll方法可以获取到东西
     * 如果是数据库存储则不需要此方法
     * 内存记录器可以配置是否保存以及保存路径,而数据库记录器一律保存
     */
    void initBeforeStart(Config config);

    /**
     * 取出未爬取的爬虫
     * 取出后需要删除该爬虫
     */
    Crawler getOne();

    /**
     * 查询所有未爬取的爬虫
     */
    List<Crawler> getAll();

    /**
     * 记录爬取成功的
     *
     * @param crawler 成功爬取的爬虫
     */
    void addSucc(Crawler crawler);

    /**
     * 查询爬取成功的
     *
     * @return 爬取成功的爬虫列表
     */
    List<Crawler> getSucc();

    /**
     * 记录失败的
     *
     * @param crawler 爬取失败的爬虫
     */
    void addErr(Crawler crawler);

    /**
     * 查询爬取失败的
     *
     * @return 爬取失败的爬虫列表
     */
    List<Crawler> getErr();

    /**
     * @return 查询未爬取的种子条数
     */
    Integer count();

    /**
     * @return 查询成功条数
     */
    Integer countSucc();

    /**
     * @return 查询失败条数
     */
    Integer countErr();
}
