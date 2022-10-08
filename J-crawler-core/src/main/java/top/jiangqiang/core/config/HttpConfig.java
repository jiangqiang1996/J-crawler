package top.jiangqiang.core.config;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: JiangQiang
 * @Date: 2022年05月15日 12:07
 */
@Data
public class HttpConfig implements Serializable {
    private Boolean useProxy = false;
    private Map<String, String> lines = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> body = new HashMap<>();
    private Map<String, String> proxyConfig = new HashMap<>();

    {
        headers.put("Accept-Encoding", "identity");
    }
}
