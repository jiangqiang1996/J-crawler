package top.jiangqiang.sample.jiangqiang.core.filter;

import top.jiangqiang.sample.jiangqiang.core.entities.Crawler;

import java.util.Set;

public interface Filter {

    void filter(Set<Crawler> urlList);
}
