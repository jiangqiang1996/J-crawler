package xin.jiangqiang.util;

import xin.jiangqiang.entities.Crawler;

import java.util.ArrayList;

/**
 * @author jiangqiang
 * @date 2020/12/18 21:47
 */
public class CrawlerUtil {
    /**
     * 清空下一代种子
     *
     * @param crawler
     */
    public static void clearNextCrawler(Crawler crawler) {
        crawler.setCrawlers(new ArrayList<>());
    }
}
