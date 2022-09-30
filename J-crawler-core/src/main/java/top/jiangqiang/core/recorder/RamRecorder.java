package top.jiangqiang.core.recorder;


import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
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
public class RamRecorder implements Recorder {
    private final BlockingQueue<Crawler> crawlerBlockingQueue = CollUtil.newBlockingQueue(1000, true);
    private final List<Crawler> successList = Collections.synchronizedList(new LinkedList<>());
    private final List<Crawler> errorList = Collections.synchronizedList(new LinkedList<>());
    private final List<Crawler> activeList = Collections.synchronizedList(new LinkedList<>());

    @Override
    public void add(Crawler crawler) {
        if (!exist(crawler)) {
            try {
                crawlerBlockingQueue.add(crawler);
            } catch (Exception e) {
                log.debug(e.getMessage());
            }
        }
    }

    @Override
    public void saveBeforeEnd() {
        //保存 successList activeList
        log.info("保存爬虫数据");
    }

    @Override
    public void initBeforeStart() {
        log.info("读取爬虫数据");
    }

    @Override
    public Crawler popOne() {
        try {
            return crawlerBlockingQueue.poll(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return null;
        }
    }

    @Override
    public List<Crawler> getAll() {
        return crawlerBlockingQueue.stream().toList();
    }

    @Override
    public void addSuccess(Crawler crawler) {
        successList.add(crawler);
    }

    @Override
    public List<Crawler> getAllSuccess() {
        return successList;
    }

    @Override
    public void addError(Crawler crawler) {
        errorList.add(crawler);
    }

    @Override
    public List<Crawler> getAllError() {
        return errorList;
    }

    @Override
    public List<Crawler> getAllActive() {
        return activeList;
    }

    @Override
    public void addActive(Crawler crawler) {
        activeList.add(crawler);
    }

    @Override
    public Integer count() {
        return crawlerBlockingQueue.size();
    }

    @Override
    public Integer countSuccess() {
        return successList.size();
    }

    @Override
    public Integer countError() {
        return errorList.size();
    }

    @Override
    public Integer countActive() {
        return activeList.size();
    }

    @Override
    public Boolean exist(Crawler crawler) {
        return crawlerBlockingQueue.contains(crawler) || errorList.contains(crawler) || successList.contains(crawler) || activeList.contains(crawler);
    }

}
