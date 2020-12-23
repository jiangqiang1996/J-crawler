package xin.jiangqiang.manage;

import lombok.extern.slf4j.Slf4j;
import xin.jiangqiang.entities.Crawler;

/**
 * 数据库记录器
 * 保留类，暂时未实现
 *
 * @author jiangqiang
 * @date 2020/12/22 15:10
 */
@Slf4j
public class DbRecorder extends AbstractRecorder {
    @Override
    public void add(Crawler crawler) {

    }

    @Override
    public Crawler getOne() {
        return null;
    }

    @Override
    public void addSucc(Crawler crawler) {

    }

    @Override
    public void addErr(Crawler crawler) {

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
}
