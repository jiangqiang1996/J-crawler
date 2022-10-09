package top.jiangqiang.core.entities;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import top.jiangqiang.core.config.HttpConfig;

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
    //深度，初始种子作为第1层
    private Integer depth = 1;
    //当前的URL
    private String url;
    //当前URL中提取出来的子爬虫
    private Set<Crawler> crawlers = new HashSet<>();
    private HttpConfig httpConfig = new HttpConfig();

    public Crawler(String url) {
        this.url = url;
    }

    /**
     * 创建时子类爬虫深度会自动+1
     *
     * @param url url
     * @return 返回子爬虫
     */
    public Crawler addSeed(String url) {
        if (StrUtil.isNotBlank(url)) {
            //this是当前爬虫，crawler是子爬虫
            Crawler crawler = ObjectUtil.cloneByStream(this);
            crawler.setUrl(url);
            crawler.setDepth(this.depth + 1);
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Crawler crawler = (Crawler) o;

        return url.equals(crawler.url);
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

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

    public Map<String, String> getProxyConfig() {
        if (httpConfig == null) {
            return null;
        } else {
            return httpConfig.getProxyConfig();
        }
    }

}
