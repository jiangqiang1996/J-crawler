package top.jiangqiang.crawler.core.config;

import lombok.Getter;
import lombok.Setter;
import okhttp3.Headers;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

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
    /**
     * 过滤掉某些header，返回的header将被加入全局配置中的header中
     */
    @Setter
    private Function<Headers, Headers> filter = headers -> headers;

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

    public void setContentType(String contentType) {
        headers.put("Content-Type", contentType);
    }

    public static String APPLICATION_JSON_UTF8 = "application/json;charset=utf-8";
    public static String APPLICATION_XML_UTF8 = "application/xml;charset=utf-8";
    public static String FORM_URLENCODED = "application/x-www-form-urlencoded";
}
