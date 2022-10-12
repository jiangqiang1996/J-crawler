package top.jiangqiang.crawler.core.recorder;

import cn.hutool.core.collection.CollUtil;
import lombok.Getter;
import lombok.Setter;
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

@Getter
@Setter
public abstract class AbstractRecorder implements Recorder {
    private Consumer<Recorder> initCallback;
    private CrawlerGlobalConfig config;

    public void initBeforeStart() {
        if (initCallback != null) {
            initCallback.accept(this);
        }
    }

    @Override
    public void saveBeforeEnd() {

    }

    public synchronized void addAll(List<Crawler> crawlers) {
        if (CollUtil.isEmpty(crawlers)) {
            crawlers.forEach(this::add);
        }
    }

    @Override
    public synchronized Crawler waitToActive() {
        Crawler crawler = popOne();
        if (crawler != null) {
            addActive(crawler);
        }
        return crawler;
    }

    @Override
    public synchronized Crawler activeToSuccess(Crawler crawler) {
        addSuccess(crawler);
        removeActive(crawler);
        return crawler;
    }

    @Override
    public synchronized Crawler activeToError(Crawler crawler) {
        addError(crawler);
        removeActive(crawler);
        return crawler;
    }
}
