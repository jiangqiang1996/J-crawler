package top.jiangqiang.crawler.newcore.entities;

import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@RequiredArgsConstructor
public class BaseCrawler implements Serializable {
    //当前种子来源，可以使用此字段解决防盗链问题
    protected BaseCrawler sourceCrawler;
    protected final String url;
}
