package top.jiangqiang.crawler.core.entities;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import top.jiangqiang.crawler.core.config.HttpConfig;

import java.io.Serializable;
import java.util.*;

/**
 * @Author: JiangQiang
 * @Date: 2022年05月14日 10:43
 */
@Data
@Accessors(chain = true)
@Slf4j
@NoArgsConstructor
public class Crawler implements Serializable {
    protected List<String> sourceList = new ArrayList<>();
    //深度，初始种子作为第1层
    protected Integer depth = 1;
    //当前的URL
    protected String url;
    //当前URL中提取出来的子爬虫
    protected List<Crawler> crawlers = new ArrayList<>();
    protected HttpConfig httpConfig = new HttpConfig();
    //错误信息
    protected String errorMessage;
    //错误码，根据错误码进行分类，可以自定义设置是否需要重新爬取
    protected Integer errorCode;

    public Crawler(String url) {
        this.url = url;
        this.id = url;
    }

    /**
     * 存储一些额外的数据，例如给同一批来源的种子做个相同的标记，用于分类等等。
     */
    protected Map<String, Object> metaData = new HashMap<>();

    /**
     * 种子唯一标识，默认与url相同，用于比较是否为同一个种子，方便去重。
     * 因为某些情况下，同一个URL在不同时间请求时返回的数据是不一样的，所以不能简单的根据URL进行去重。
     */
    protected String id;

    /**
     * 创建时子类爬虫深度会自动+1
     *
     * @param url url
     * @return 返回子爬虫
     */

    public Crawler addSeed(String url) {
        if (StrUtil.isNotBlank(url)) {
            //this是当前爬虫，crawler是子爬虫
            Crawler crawler = new Crawler();
            crawler.setUrl(url);
            crawler.setId(url);
            crawler.setDepth(this.depth + 1);
            crawler.setHttpConfig(this.httpConfig.clone());
            List<String> subSourceList = new ArrayList<>(this.sourceList);
            subSourceList.add(this.url);
            crawler.setSourceList(subSourceList);
            crawler.setMetaData(new HashMap<>(this.metaData));
            crawler.addHeader("referer", this.url);
            crawlers.add(crawler);
            return crawler;
        }
        return null;
    }

    public void addSeeds(List<String> urlList) {
        if (CollUtil.isNotEmpty(urlList)) {
            urlList.forEach(this::addSeed);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Crawler crawler = (Crawler) o;
        return id.equals(crawler.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @JSONField(serialize = false, deserialize = false)
    public Map<String, String> getLines() {
        if (httpConfig == null) {
            return null;
        } else {
            return httpConfig.getLines();
        }
    }

    public Crawler addLine(String key, String value) {
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

    @JSONField(serialize = false, deserialize = false)
    public Map<String, String> getHeaders() {
        if (httpConfig == null) {
            return null;
        } else {
            return httpConfig.getHeaders();
        }
    }

    public Crawler addHeader(String key, String value) {
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

    @JSONField(serialize = false, deserialize = false)
    public Map<String, String> getBody() {
        if (httpConfig == null) {
            return null;
        } else {
            return httpConfig.getBody();
        }
    }

    public Crawler addParam(String key, String value) {
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

    public Crawler addProxyConfig(String key, String value) {
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

    public Crawler addProxyIp(String ip) {
        return addProxyConfig("IP", ip);
    }

    public Crawler addProxyPort(String port) {
        return addProxyConfig("port", port);
    }

    public Crawler addProxyProtocol(String protocol) {
        return addProxyConfig("protocol", protocol);
    }

    public Crawler addProxyUsername(String username) {
        return addProxyConfig("username", username);
    }

    public Crawler addProxyPassword(String password) {
        return addProxyConfig("password", password);
    }

    @JSONField(serialize = false, deserialize = false)
    public Map<String, String> getProxyConfig() {
        if (httpConfig == null) {
            return null;
        } else {
            return httpConfig.getProxyConfig();
        }
    }

}
