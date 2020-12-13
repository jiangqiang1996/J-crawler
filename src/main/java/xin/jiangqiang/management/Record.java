package xin.jiangqiang.management;

import xin.jiangqiang.entities.Crawler;

import java.util.List;
import java.util.Set;

/**
 * 存储所有种子
 * 运行结束时保存
 */
public interface Record {
    void addSucc(String url);

    /**
     * 查询记录里是否已经爬取过某URL
     * @param url
     * @return
     */
    Boolean hasUrl(String url);

    Object getSucc();

    void addErr(String url);

    Object getErr();
}
