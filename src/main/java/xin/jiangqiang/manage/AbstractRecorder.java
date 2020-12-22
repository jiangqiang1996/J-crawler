package xin.jiangqiang.manage;

import lombok.extern.slf4j.Slf4j;
import xin.jiangqiang.config.Config;
import xin.jiangqiang.entities.Crawler;

import java.util.List;

/**
 * 下面方法作为保留方法，暂时没有使用，但是后续可能会使用到
 * 如果后续使用到下面方法可以通过子类覆盖
 */
@Slf4j
public abstract class AbstractRecorder implements Recorder {

    @Override
    public void addAll(List<Crawler> crawlers) {

    }

    @Override
    public List<Crawler> getAll() {
        return null;
    }

    @Override
    public List<Crawler> getSucc() {
        return null;
    }

    @Override
    public List<Crawler> getErr() {
        return null;
    }

    @Override
    public void saveBeforeEnd(Config config) {

    }

    @Override
    public void initBeforeStart(Config config) {

    }
}
