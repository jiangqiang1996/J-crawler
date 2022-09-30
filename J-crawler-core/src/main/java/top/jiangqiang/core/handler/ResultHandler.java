package top.jiangqiang.core.handler;

import top.jiangqiang.core.entities.Crawler;
import top.jiangqiang.core.entities.Page;
import top.jiangqiang.core.recorder.Recorder;

import java.io.IOException;
import java.util.Set;

public interface ResultHandler {
    default Set<Crawler> doSuccess(Recorder recorder, Crawler crawler, Page page) {
        //处理完成，加入成功结果集
        recorder.addSuccess(crawler);
        return page.getCrawlers();
    }


    default void doFailure(Recorder recorder, Crawler crawler, IOException e) {
        //处理完成，加入失败结果集
        recorder.addError(crawler);
    }
}
