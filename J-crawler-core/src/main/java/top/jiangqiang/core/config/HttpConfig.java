package top.jiangqiang.core.config;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @Author: JiangQiang
 * @Date: 2022年05月15日 12:07
 */
@Data
public class HttpConfig implements Serializable {
    private Boolean useProxy = false;
    private Map<String, String> lines;
    private Map<String, String> headers;
    private Map<String, String> body;
    private Map<String, String> proxyConfig;
}
