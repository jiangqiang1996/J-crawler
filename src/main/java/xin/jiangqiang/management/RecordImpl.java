package xin.jiangqiang.management;

import xin.jiangqiang.entities.Crawler;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 存储所有种子
 * 运行结束时保存
 */
public class RecordImpl implements Record {
    private static Set<String> succUrls = Collections.synchronizedSet(new HashSet<>());
    private static Set<String> errUrls = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void addSucc(String url) {
        succUrls.add(url);
    }

    @Override
    public Boolean hasUrl(String url) {
        return succUrls.contains(url);
    }

    @Override
    public Set<String> getSucc() {
        return succUrls;
    }

    @Override
    public void addErr(String url) {
        errUrls.add(url);
    }

    @Override
    public Set<String> getErr() {
        return errUrls;
    }
}
