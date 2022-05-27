package xin.jiangqiang.core.entities;

import cn.hutool.core.lang.Singleton;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import xin.jiangqiang.core.config.Config;
import xin.jiangqiang.core.interfaces.HttpConfig;

import java.io.Serializable;
import java.util.*;

/**
 * @Author: JiangQiang
 * @Date: 2022年05月14日 10:43
 * 定义公共属性和元数据
 */
@Data
@Accessors(chain = true)
@Slf4j
public class Crawler implements Serializable, HttpConfig<Crawler> {
    //深度
    private Integer depth = 0;
    private String url;//当前的URL
    //根据type执行不同逻辑，对应@Match注解的value
    private String type = "";//对爬虫进行分类，方便后面使用@Match进行分类处理
    private Set<Crawler> crawlers = new HashSet<>();//当前URL中提取出来的子爬虫

    @Setter(AccessLevel.NONE)
    private Map<String, String> httpConfig = new HashMap<>(); //http请求网络代理参数设置
    private Boolean useProxy = false;//请求客户端是否使用代理
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Map<String, Map<String, String>> metaData = new HashMap<>();

    /**
     * 1. 从crawler构造Page对象,发送请求之后是Page对象,请求前是Crawler对象
     * 2. 将Page中的元数据复制到Next对象
     *
     * @param crawler 种子对象，后续爬虫会继承他的设置
     * @return 返回调用此方法的对象
     */
    public Crawler initDataFromCrawler(Crawler crawler) {
        this.depth = crawler.depth;
        this.url = crawler.url;
        if (StrUtil.isEmpty(this.type)) {
            this.type = crawler.type;
        }
        String metaDataStr = JSONUtil.toJsonStr(crawler.metaData);
        this.metaData = JSONUtil.parseObj(metaDataStr).toBean(Map.class);
        return this;
    }

    /**
     * 创建时子类爬虫深度会自动+1
     *
     * @param url url
     * @return 返回子爬虫
     */
    public Crawler addSeed(String url) {
        if (StrUtil.isNotEmpty(url)) {
            //this是当前爬虫，crawler是子爬虫
            Crawler crawler = new Crawler();
            crawler.initDataFromCrawler(this);
            crawler.setUrl(url);
            crawler.setDepth(this.getDepth() + 1);
            crawlers.add(crawler);
            return crawler;
        }
        return null;
    }

    /**
     * 批量创建子爬虫，批量设置子类类型
     *
     * @param urls url列表
     * @param type 类型
     */
    public void addSeeds(Collection<String> urls, String type) {
        for (String url : urls) {
            addSeed(url).setType(type);
        }
    }

    public Crawler(String url) {
        this.url = url;
        initFromGlobConfig(Singleton.get(Config.class));
    }

    public Crawler() {
        initFromGlobConfig(Singleton.get(Config.class));
    }

    /**
     * 创建爬虫
     *
     * @param url  链接
     * @param type 爬虫分类
     */
    public Crawler(String url, String type) {
        this.url = url;
        this.type = type;
        initFromGlobConfig(Singleton.get(Config.class));
    }

    public Crawler initFromGlobConfig(HttpConfig<?> config) {
        this.useProxy = config.getUseProxy();
        String configStr = JSONUtil.toJsonStr(config.getHttpConfig());
        this.httpConfig = JSONUtil.parseObj(configStr).toBean(Map.class);
        this.metaData.put("lines", config.getLines());
        this.metaData.put("headers", config.getHeaders());
        this.metaData.put("bodys", config.getBodys());
        return this;
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
        return Objects.hash(url);
    }

    public Map<String, String> getLines() {
        return metaData.computeIfAbsent("lines", k -> new HashMap<>());
    }

    public Crawler addLines(String key, String value) {
        Map<String, String> lines = getLines();
        lines.put(key, value);
        return this;
    }

    public Map<String, String> getHeaders() {
        return metaData.computeIfAbsent("headers", k -> new HashMap<>());
    }

    public Crawler addHeaders(String key, String value) {
        Map<String, String> headers = getHeaders();
        headers.put(key, value);
        return this;
    }

    public Map<String, String> getBodys() {
        return metaData.computeIfAbsent("bodys", k -> new HashMap<>());
    }

    public Crawler addBodys(String key, String value) {
        Map<String, String> bodys = getBodys();
        bodys.put(key, value);
        return this;
    }

    /**
     * 设置参数
     *
     * @param key
     * @param map
     */
    public void setMetaData(String key, Map<String, String> map) {
        metaData.put(key, map);
    }

    /**
     * 获取参数
     *
     * @param key
     * @return
     */
    public Map<String, String> setMetaData(String key) {
        return metaData.get(key);
    }

}
