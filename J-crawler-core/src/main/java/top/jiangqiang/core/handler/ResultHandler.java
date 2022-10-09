package top.jiangqiang.core.handler;

import okhttp3.Response;
import top.jiangqiang.core.entities.Crawler;
import top.jiangqiang.core.entities.Page;
import top.jiangqiang.core.recorder.Recorder;

import java.io.IOException;
import java.util.Set;

public interface ResultHandler {
    default Set<Crawler> doSuccess(Recorder recorder, Crawler crawler, Page page, Response response) {
        return page.getCrawlers();
    }


    default void doFailure(Recorder recorder, Crawler crawler, IOException e) {
    }
}
