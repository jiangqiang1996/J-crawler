package xin.jiangqiang.filter;

import xin.jiangqiang.entities.Crawler;
import xin.jiangqiang.entities.Next;
import xin.jiangqiang.entities.Page;

import java.util.*;

public class NextFilter implements Filter {
    /**
     * 根据URL去重
     *
     * @param next 下次请求时会执行的对象
     * @param page 本次请求结束后的对象
     */
    @Override
    public void filter(Next next, Page page) {
        List<Crawler> crawlers = next.getCrawlers();
        Set<Crawler> set = new HashSet<>(crawlers);
        next.setCrawlers(new ArrayList<>(set));
    }
}
