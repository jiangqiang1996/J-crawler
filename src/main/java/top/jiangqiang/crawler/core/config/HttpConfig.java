package top.jiangqiang.crawler.core.config;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: JiangQiang
 * @Date: 2022年05月15日 12:07
 */
@Data
public class HttpConfig implements Serializable, Cloneable {
    private Map<String, String> lines = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> body = new HashMap<>();
    private Map<String, String> proxyConfig = new HashMap<>();

    {
        headers.put("Accept-Encoding", "identity");
    }

    @Override
    public HttpConfig clone() {
        try {
            HttpConfig httpConfig = (HttpConfig) super.clone();
            httpConfig.setLines(new HashMap<>(lines));
            httpConfig.setHeaders(new HashMap<>(headers));
            httpConfig.setBody(new HashMap<>(body));
            httpConfig.setProxyConfig(new HashMap<>(proxyConfig));
            return httpConfig;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

}
