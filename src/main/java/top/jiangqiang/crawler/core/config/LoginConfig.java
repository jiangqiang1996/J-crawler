package top.jiangqiang.crawler.core.config;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jiangqiang
 * @date 2022-10-17
 */
@Getter
public class LoginConfig {
    /**
     * 登录接口地址
     */
    @Setter
    private final String url;
    private final Map<String, String> lines = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();
    /**
     * 登录参数
     */
    private final Map<String, String> body = new HashMap<>();
    private final Map<String, String> proxyConfig = new HashMap<>();

    public LoginConfig(String url) {
        this.url = url;
    }

    public LoginConfig addLines(String key, String value) {
        lines.put(key, value);
        return this;
    }

    public LoginConfig setMethod(String method) {
        lines.put("method", method);
        return this;
    }

    public LoginConfig addHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    public LoginConfig addBody(String key, String value) {
        body.put(key, value);
        return this;
    }

    public LoginConfig addProxyIp(String ip) {
        proxyConfig.put("IP", ip);
        return this;
    }

    public LoginConfig addProxyPort(String port) {
        proxyConfig.put("port", port);
        return this;
    }

    public LoginConfig addProxyProtocol(String protocol) {
        proxyConfig.put("protocol", protocol);
        return this;
    }

    public LoginConfig addProxyUsername(String username) {
        proxyConfig.put("username", username);
        return this;
    }

    public LoginConfig addProxyPassword(String password) {
        proxyConfig.put("password", password);
        return this;
    }

}
