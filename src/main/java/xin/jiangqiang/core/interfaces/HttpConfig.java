package xin.jiangqiang.core.interfaces;

import java.util.Map;

/**
 * @Author: JiangQiang
 * @Date: 2022年05月15日 12:07
 */
public interface HttpConfig<T> {
    Map<String, String> getHttpConfig();

    Boolean getUseProxy();//是否使用代理

    Map<String, String> getLines();

    T addLines(String key, String value);

    Map<String, String> getHeaders();

    T addHeaders(String key, String value);

    Map<String, String> getBodys();

    T addBodys(String key, String value);
}
