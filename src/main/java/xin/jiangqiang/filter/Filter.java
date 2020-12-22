package xin.jiangqiang.filter;

import xin.jiangqiang.config.Config;
import xin.jiangqiang.entities.Crawler;
import xin.jiangqiang.entities.Page;

public interface Filter {

    void filter(Crawler crawler);
}
