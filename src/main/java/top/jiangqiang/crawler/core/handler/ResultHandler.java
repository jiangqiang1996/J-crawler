package top.jiangqiang.crawler.core.handler;

import okhttp3.Response;
import top.jiangqiang.crawler.core.entities.Crawler;
import top.jiangqiang.crawler.core.entities.Page;
import top.jiangqiang.crawler.core.recorder.Recorder;

import java.io.IOException;
import java.util.List;

public interface ResultHandler {
    default List<Crawler> doSuccess(Recorder recorder, Crawler crawler, Page page, Response response) {
        return page.getCrawlers();
    }


    default void doFailure(Recorder recorder, Crawler crawler, IOException e) {
    }
}
