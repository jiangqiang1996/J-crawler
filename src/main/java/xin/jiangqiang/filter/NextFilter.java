package xin.jiangqiang.filter;

import xin.jiangqiang.entities.Crawler;
import xin.jiangqiang.entities.Page;

import java.util.*;

public class NextFilter implements Filter {
    /**
     * 过滤掉每次请求后解析的URL中不需要的URL
     * 可以在此方法实现去重，但是意义不大，不同的URL访问之后可以解析到相同的URL，因此在这里去重不是一个很好的办法
     *
     * @param crawler 本次请求的爬虫实例，存储了下一代爬虫对象
     */
    @Override
    public void filter(Crawler crawler) {
        List<Crawler> crawlers = crawler.getCrawlers();
        Set<Crawler> set = new HashSet<>(crawlers);
        crawler.setCrawlers(new ArrayList<>(set));
    }
}
