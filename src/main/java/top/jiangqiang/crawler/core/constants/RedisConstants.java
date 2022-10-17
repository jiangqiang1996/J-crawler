package top.jiangqiang.crawler.core.constants;

/**
 * @author Jiangqiang
 * @version 1.0
 * @description TODO
 * @date 2022/10/12 13:02
 */
public class RedisConstants {
    //爬虫队列
    public final static String CRAWLER_WAITING_LIST = "crawler:waiting:list";
    /**
     * 正在爬取的列表
     * 如果用list存对象，因为对象的属性改变后，无法删除指定对象
     */
    public final static String CRAWLER_ACTIVE_HASH = "crawler:active:hash";
    //成功的列表
    public final static String CRAWLER_SUCCESS_HASH = "crawler:success:hash";
    //失败的列表
    public final static String CRAWLER_ERROR_HASH = "crawler:error:hash";
    //存储所有的列表，用于去重
    public final static String CRAWLER_HISTORY_HASH = "crawler:history:hash";
}
