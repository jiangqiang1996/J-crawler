package top.jiangqiang.crawler.recorder;

import top.jiangqiang.crawler.entities.BaseCrawler;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class RamRecorder<T extends BaseCrawler> {
    ConcurrentLinkedDeque<T> queue = new ConcurrentLinkedDeque<>();

    public List<T> get() {
        T poll = queue.poll();
        if (poll == null) {
            return null;
        } else {
            return List.of(poll);
        }
    }

    public void add(T crawler) {
        queue.add(crawler);
    }
}
