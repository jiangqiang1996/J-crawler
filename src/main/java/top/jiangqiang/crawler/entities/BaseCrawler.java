package top.jiangqiang.crawler.entities;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@RequiredArgsConstructor
@Data
public abstract class BaseCrawler implements Serializable {
    //当前种子来源，可以使用此字段解决防盗链问题
    protected BaseCrawler sourceCrawler;
    protected final String url;
}
