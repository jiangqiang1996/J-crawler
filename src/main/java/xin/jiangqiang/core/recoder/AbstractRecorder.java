package xin.jiangqiang.core.recoder;

import cn.hutool.core.lang.Singleton;
import lombok.extern.slf4j.Slf4j;
import xin.jiangqiang.core.config.Config;
import xin.jiangqiang.core.entities.Crawler;

import java.util.List;

/**
 * 下面方法作为保留方法，暂时没有使用，但是后续可能会使用到
 * 如果后续使用到下面方法可以通过子类覆盖
 */
@Slf4j
public abstract class AbstractRecorder implements Recorder {
    protected Config config = Singleton.get(Config.class);

    @Override
    public void add(Crawler crawler) {

    }

    @Override
    public void addAll(List<Crawler> crawlers) {

    }

    @Override
    public void saveBeforeEnd() {

    }

    @Override
    public void initBeforeStart() {

    }

    @Override
    public Crawler getOne() {
        return null;
    }

    @Override
    public List<Crawler> getAll() {
        return null;
    }

    @Override
    public void addSucc(Crawler crawler) {

    }

    @Override
    public List<Crawler> getSucc() {
        return null;
    }

    @Override
    public void addErr(Crawler crawler) {

    }

    @Override
    public List<Crawler> getErr() {
        return null;
    }

    @Override
    public Integer count() {
        return null;
    }

    @Override
    public Integer countSucc() {
        return null;
    }

    @Override
    public Integer countErr() {
        return null;
    }

    @Override
    public Boolean exist(Crawler crawler) {
        return null;
    }
}
