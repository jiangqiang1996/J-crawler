package xin.jiangqiang.core.filter;

import xin.jiangqiang.core.entities.Crawler;

import java.util.Set;

public interface Filter {

    void filter(Set<Crawler> urlList);
}
