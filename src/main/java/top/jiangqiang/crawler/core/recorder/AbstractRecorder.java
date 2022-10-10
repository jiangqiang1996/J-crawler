package top.jiangqiang.crawler.core.recorder;

import lombok.Getter;
import lombok.Setter;
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

@Getter
@Setter
public abstract class AbstractRecorder implements Recorder {
    private Callback initCallback;
    private CrawlerGlobalConfig config;

    public void initBeforeStart() {
        Callback initCallback = getInitCallback();
        if (initCallback != null) {
            initCallback.process(this);
        }
    }

    public void addAll(List<Crawler> crawlers) {
        crawlers.forEach(this::add);
    }

    @Override
    public void saveBeforeEnd() {

    }

    @Override
    public void add(Crawler crawler) {

    }

    @Override
    public Crawler popOne() {
        return null;
    }

    @Override
    public List<Crawler> getAll() {
        return null;
    }

    @Override
    public void addSuccess(Crawler crawler) {

    }

    @Override
    public List<Crawler> getAllSuccess() {
        return null;
    }

    @Override
    public void addError(Crawler crawler) {

    }

    @Override
    public List<Crawler> getAllError() {
        return null;
    }

    @Override
    public List<Crawler> getAllActive() {
        return null;
    }

    @Override
    public Integer count() {
        return null;
    }

    @Override
    public Integer countSuccess() {
        return null;
    }

    @Override
    public Integer countError() {
        return null;
    }

    @Override
    public Integer countActive() {
        return null;
    }

    @Override
    public Boolean exist(Crawler crawler) {
        return null;
    }

    @Override
    public void addActive(Crawler crawler) {

    }

    @Override
    public void removeActive(Crawler crawler) {

    }
}
