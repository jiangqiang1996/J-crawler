package xin.jiangqiang.filter;

import xin.jiangqiang.entities.Next;
import xin.jiangqiang.entities.Page;

public interface Filter {

    void filter(Next next, Page page);
}
