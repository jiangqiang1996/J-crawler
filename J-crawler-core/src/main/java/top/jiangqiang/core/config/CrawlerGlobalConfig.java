package top.jiangqiang.core.config;

import cn.hutool.core.util.StrUtil;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局配置
 *
 * @author jiangqiang
 * @date 2022-09-30
 */
@Slf4j
@Data
public class CrawlerGlobalConfig implements Serializable {
    private Charset charset = Charset.defaultCharset();

    //超过指定字节数的响应数据，不会对响应内容进行处理，响应数据过大，一般是静态资源文件
    private Long maxSize = 1024 * 300L;
    /**
     * 最小值为1
     */
    private Integer threads = 10;
    /**
     * 注意，如果上一次爬取的最大深度为4，程序意外终止后，修改最大深度为2，继续上次爬取保存的结果继续爬取，那么无论如何至少会爬取一次。
     */
    private Integer depth = 3;

    /**
     * 强力模式，将使用正则表达式强行抽取js中的链接
     */
    private Boolean strong = true;
    /**
     * 线程池内所有任务空闲之后，需要等待一分钟才会结束，开启此选项可以强制结束，
     * 如果只有极少数线程正在执行特别耗费时间的任务时（下载文件等任务），强制结束会导致数据丢失。
     * 强制结束会使整个程序结束，如果是其他项目（web项目）引用此工具二次开发，不应该开启强制结束
     */
    private Boolean forceEnd = false;
    /**
     * 满足此正则表达式列表的URL会被提取
     */
    @Setter(AccessLevel.NONE)
    private List<String> regExs = new ArrayList<>();
    /**
     * 满足此正则表达式列表的会被过滤,不作为种子进行下次爬取
     */
    @Setter(AccessLevel.NONE)
    private List<String> reverseRegExs = new ArrayList<>();
    /**
     * 满足此正则表达式列表的也会被过滤,这是系统默认过滤规则,会过滤掉css,js
     */
    @Setter(AccessLevel.NONE)
    private List<String> defaultReverseRegExs = new ArrayList<>();

    /**
     * 是否启用默认正则表达式过滤,如果不启用则defaultReverseRegExs无效
     */
    private Boolean isUseDefault = true;

    /**
     * 下面两个属性只对内存记录器有效，对数据库记录器无效
     * 结束时没有爬取的种子会保存到此路径，用于断点续爬。路径不能为空，且未爬取的种子数不能为0，才会保存
     */
    private String savePath = "";
    /**
     * 是否继续上次爬取，保存路径为空或此属性值为false时均不会继续爬取
     */
    private Boolean isContinue = true;

    /**
     * http请求代理参数设置
     */
    private HttpConfig httpConfig = new HttpConfig();

    {
        defaultReverseRegExs.add(".*\\.(js|css).*");
    }

    public void setThreads(Integer threads) {
        if (threads < 1) {
            this.threads = 1;
        } else {
            this.threads = threads;
        }
    }


    /**
     * 加号开头，去掉加号放正正则列表
     * 减号开头，去掉减号放逆正则列表
     * 否则直接放正正则列表
     *
     * @param regEx 正则表达式
     */
    public void addRegEx(String regEx) {
        if (StrUtil.isNotEmpty(regEx)) {
            if (regEx.startsWith("-")) {
                reverseRegExs.add(regEx.substring(1));
            } else if (regEx.startsWith("+")) {
                regExs.add(regEx.substring(1));
            } else {
                regExs.add(regEx);
            }
        }
    }

    /**
     * 添加默认正则表达式
     * 默认正则表达式没有正正则和逆正则之分
     *
     * @param regEx 满足该正则表达式的URL会被过滤
     */
    public void addDefaultRegEx(String regEx) {
        if (StrUtil.isNotEmpty(regEx)) {
            if (regEx.startsWith("-") || regEx.startsWith("+")) {
                defaultReverseRegExs.add(regEx.substring(1));
            } else {
                defaultReverseRegExs.add(regEx);
            }
        }
    }

    public Map<String, String> getLines() {
        if (httpConfig == null) {
            return null;
        } else {
            return httpConfig.getLines();
        }
    }

    public CrawlerGlobalConfig addLine(String key, String value) {
        if (httpConfig == null) {
            httpConfig = new HttpConfig();
        }
        Map<String, String> lines = httpConfig.getLines();
        if (lines == null) {
            lines = new HashMap<>();
            httpConfig.setLines(lines);
        }
        lines.put(key, value);
        return this;
    }

    public Boolean getUseProxy() {
        if (httpConfig == null) {
            return false;
        } else {
            return httpConfig.getUseProxy();
        }
    }

    public Map<String, String> getHeaders() {
        if (httpConfig == null) {
            return null;
        } else {
            return httpConfig.getHeaders();
        }
    }

    public CrawlerGlobalConfig addHeader(String key, String value) {
        if (httpConfig == null) {
            httpConfig = new HttpConfig();
        }
        Map<String, String> headers = httpConfig.getHeaders();
        if (headers == null) {
            headers = new HashMap<>();
            httpConfig.setHeaders(headers);
        }
        headers.put(key, value);
        return this;
    }

    public Map<String, String> getBody() {
        if (httpConfig == null) {
            return null;
        } else {
            return httpConfig.getBody();
        }
    }

    public CrawlerGlobalConfig addParam(String key, String value) {
        if (httpConfig == null) {
            httpConfig = new HttpConfig();
        }
        Map<String, String> body = httpConfig.getBody();
        if (body == null) {
            body = new HashMap<>();
            httpConfig.setBody(body);
        }
        body.put(key, value);
        return this;
    }

    public CrawlerGlobalConfig addProxyConfig(String key, String value) {
        if (httpConfig == null) {
            httpConfig = new HttpConfig();
        }
        Map<String, String> proxyConfig = httpConfig.getProxyConfig();
        if (proxyConfig == null) {
            proxyConfig = new HashMap<>();
            httpConfig.setProxyConfig(proxyConfig);
        }
        proxyConfig.put(key, value);
        return this;
    }

    public Map<String, String> getProxyConfig() {
        if (httpConfig == null) {
            return null;
        } else {
            return httpConfig.getProxyConfig();
        }
    }

}
