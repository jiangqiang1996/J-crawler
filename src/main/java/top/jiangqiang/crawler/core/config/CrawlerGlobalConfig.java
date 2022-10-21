package top.jiangqiang.crawler.core.config;

import cn.hutool.core.util.StrUtil;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

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
    /**
     * 返回一个时间，单位毫秒，用于指定每次爬虫任务请求的时间间隔，
     * 建议返回一个随机数，不容易被察觉为机器请求，防止被封禁。
     * timeout为null或Supplier.get()返回null或小于等于0的数字则不需要时间间隔
     */
    private Supplier<Long> timeout = () -> null;
    /**
     * 请求和响应报文的日志打印级别
     */
    private HttpLoggingInterceptor.Level logLevel = null;
    /**
     * 是否使用代理
     */
    private Boolean useProxy = false;
    /**
     * 是否重新爬取之前失败的任务
     */
    private Boolean isContinue = false;
    /**
     * 挂载到全局配置中的自定义变量，全局配置对象在整个爬虫任务的大多数可定制部分都可以获取到，
     * 因此通过此字段注入全局可用的自定义配置，在重写其他接口时会使用到此字段进行自定义扩展。
     * 例如自定义内存记录器时，通过此字段注入文件保存位置，将内存数据保存到文件等等。
     */
    private Map<String, Object> customConfig;
    /**
     * 超过指定字节数的响应数据，不会对响应内容进行处理，响应数据过大，一般是静态资源文件，超过此大小的响应会被当成非文本响应内容
     */
    private Long maxSize = 1024 * 512L;
    /**
     * 可以是父类型，例如text，application等，也可以配置具体的子类型，例如：application/json
     * 需要解析内容的媒体类型列表，此列表中的媒体类型会被当做文本类型，在配置正则表达式之后会自动从此列表中列举的类型中提取URL。
     * 不在此列表中的内容往往是二进制文件，图片，音频，视频等格式。这些需要自己过滤后进行下载。
     */
    private List<String> mimeTypeList = new ArrayList<>();
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
     * 开启此选项后，如果recorder中没有正在进行的任务，则会立即结束爬虫任务，停止整个程序。
     * 强制结束会使整个程序结束，如果是其他项目（web项目）引用此工具二次开发，不应该开启强制结束，
     */
    private Boolean forceEnd = false;
    /**
     * 是否允许任务结束，如果为false，当没有爬虫任务时会定期轮训检查是否有新的种子，
     * 如果种子来源不仅仅是此程序，建议设置为false
     * 如果为true，则会在一段时间后结束程序
     */
    private Boolean allowEnd = true;
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
     * http请求代理参数设置
     */
    private HttpConfig httpConfig = new HttpConfig();

    {
        defaultReverseRegExs.add(".*\\.(js|css).*");
        mimeTypeList.add("text");
        mimeTypeList.add("application/json");
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

    public CrawlerGlobalConfig addProxyIp(String ip) {
        return addProxyConfig("IP", ip);
    }

    public CrawlerGlobalConfig addProxyPort(String port) {
        return addProxyConfig("port", port);
    }

    public CrawlerGlobalConfig addProxyProtocol(String protocol) {
        return addProxyConfig("protocol", protocol);
    }

    public CrawlerGlobalConfig addProxyUsername(String username) {
        return addProxyConfig("username", username);
    }

    public CrawlerGlobalConfig addProxyPassword(String password) {
        return addProxyConfig("password", password);
    }

    public Map<String, String> getProxyConfig() {
        if (httpConfig == null) {
            return null;
        } else {
            return httpConfig.getProxyConfig();
        }
    }

}
