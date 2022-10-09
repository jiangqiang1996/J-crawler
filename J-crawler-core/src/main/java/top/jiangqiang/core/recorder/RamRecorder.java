package top.jiangqiang.core.recorder;


import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import top.jiangqiang.core.config.CrawlerGlobalConfig;
import top.jiangqiang.core.entities.Crawler;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author Jiangqiang
 * @version 1.0
 * @description 内存记录器，会大量占用内存，只建议简单使用，深度使用推荐自定义实现数据库持久化
 * @date 2022/9/30 9:47
 */
@Slf4j
public class RamRecorder extends AbstractRecorder {
    private  final BlockingQueue<Crawler> crawlerBlockingQueue = CollUtil.newBlockingQueue(1000, true);
    private final List<Crawler> successList = Collections.synchronizedList(new LinkedList<>());
    private final List<Crawler> errorList = Collections.synchronizedList(new LinkedList<>());
    private final List<Crawler> activeList = Collections.synchronizedList(new LinkedList<>());

    @Override
    public synchronized void add(Crawler crawler) {
        if (!exist(crawler)) {
            try {
                crawlerBlockingQueue.add(crawler);
            } catch (Exception e) {
                log.debug(e.getMessage());
            }
        }
    }

    /**
     * 取出后放active列表
     *
     * @return
     */
    @Override
    public synchronized Crawler popOne() {
        try {
            return crawlerBlockingQueue.poll(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return null;
        }
    }

    @Override
    public synchronized List<Crawler> getAll() {
        return crawlerBlockingQueue.stream().toList();
    }

    @Override
    public synchronized void addSuccess(Crawler crawler) {
        successList.add(crawler);
    }

    @Override
    public synchronized List<Crawler> getAllSuccess() {
        return successList;
    }

    @Override
    public synchronized void addError(Crawler crawler) {
        errorList.add(crawler);
    }

    @Override
    public synchronized List<Crawler> getAllError() {
        return errorList;
    }

    @Override
    public synchronized List<Crawler> getAllActive() {
        return activeList;
    }

    @Override
    public synchronized void addActive(Crawler crawler) {
        activeList.add(crawler);
    }

    @Override
    public synchronized void removeActive(Crawler crawler) {
        activeList.remove(crawler);
    }

    @Override
    public synchronized Integer count() {
        return crawlerBlockingQueue.size();
    }

    @Override
    public synchronized Integer countSuccess() {
        return successList.size();
    }

    @Override
    public synchronized Integer countError() {
        return errorList.size();
    }

    @Override
    public synchronized Integer countActive() {
        return activeList.size();
    }

    @Override
    public synchronized Boolean exist(Crawler crawler) {
        return crawlerBlockingQueue.contains(crawler) || errorList.contains(crawler) || successList.contains(crawler) || activeList.contains(crawler);
    }

    @Override
    public synchronized Callback getInitCallback() {
        return super.getInitCallback();
    }

    @Override
    public synchronized CrawlerGlobalConfig getConfig() {
        return super.getConfig();
    }

    @Override
    public synchronized void setInitCallback(Callback initCallback) {
        super.setInitCallback(initCallback);
    }

    @Override
    public synchronized void setConfig(CrawlerGlobalConfig config) {
        super.setConfig(config);
    }

    @Override
    public synchronized void initBeforeStart() {
        super.initBeforeStart();
    }

    @Override
    public synchronized void addAll(List<Crawler> crawlers) {
        super.addAll(crawlers);
    }

    @Override
    public synchronized void saveBeforeEnd() {
        super.saveBeforeEnd();
    }
}
