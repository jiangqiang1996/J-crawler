package xin.jiangqiang.entities;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.apache.commons.beanutils.BeanUtils;
import org.openqa.selenium.Cookie;
import xin.jiangqiang.net.RequestMethod;
import xin.jiangqiang.util.StringUtil;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * 定义公共属性和元数据
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@Slf4j
public class Crawler implements Serializable {
    //深度
    private Integer depth = 0;
    private String url;//当前的URL
    //根据type执行不同逻辑
    private String type = "";
    private List<Crawler> crawlers = new ArrayList<>();//当前URL中提取出来的子爬虫

    /**
     * 1. 从crawler构造Page对象,发送请求之后是Page对象,请求前是Crawler对象
     * 2. 将Page中的元数据复制到Next对象
     *
     * @param crawler 种子对象，后续爬虫会继承他的设置
     * @return 返回调用此方法的对象
     */
    //todo
    public Crawler initDataFromCrawler(Crawler crawler) {
        this.depth = crawler.depth;
//        this.url = crawler.url;
        if (StringUtil.isEmpty(this.type)) {
            this.type = crawler.type;
        }
        //此处需要深拷贝
        Map<String, String> lines = new HashMap<>(crawler.getLines());
        this.setLines(lines);

        Map<String, String> headers = new HashMap<>(crawler.getHeaders());
        this.setHeaders(headers);

        Map<String, String> bodys = new HashMap<>(crawler.getBodys());
        this.setBodys(bodys);

        Map<String, String> data = new HashMap<>(crawler.getData());
        this.setData(data);

        Map<String, String> configs = new HashMap<>(crawler.getData());
        this.setConfigs(configs);
        return this;
    }

    @Setter(AccessLevel.NONE)
    private Map<String, Map<String, String>> metaData;

    {//初始化存储元数据的对象，初始化请求头，请求体，请求行相关对象
        metaData = new HashMap<>();
        setHeaders(new HashMap<>());
        setBodys(new HashMap<>());
        setLines(new HashMap<>());
        setData(new HashMap<>());
        setConfigs(new HashMap<>());
    }

    //设置请求配置（代理信息）
    public Crawler setConfigs(Map<String, String> configs) {
        metaData.put("configs", configs);
        return this;
    }

    //获取请求配置（代理信息）
    public Map<String, String> getConfigs() {
        return metaData.get("configs");
    }

    //设置其他数据信息（自定义数据）
    public Crawler setData(Map<String, String> others) {
        metaData.put("others", others);
        return this;
    }

    //获取自定义数据
    public Map<String, String> getData() {
        return metaData.get("others");
    }

    //设置请求头
    public Crawler setHeaders(Map<String, String> headers) {
        metaData.put("headers", headers);
        return this;
    }

    //获取请求头
    public Map<String, String> getHeaders() {
        return metaData.get("headers");
    }

    //设置请求体
    public Crawler setBodys(Map<String, String> bodys) {
        metaData.put("bodys", bodys);
        return this;
    }

    //获取请求体
    public Map<String, String> getBodys() {
        return metaData.get("bodys");
    }

    //设置请求方法
    public Crawler setMethod(RequestMethod method) {
        Map<String, String> lines = getLines();
        lines.put("method", method.getMethod());
        setLines(lines);
        return this;
    }

    //获取请求行
    public String getMethod() {
        String method = getLines().get("method");
        return StringUtil.isNotEmpty(method) ? method : "GET";
    }

    //设置请求行
    public Crawler setLines(Map<String, String> lines) {
        metaData.put("lines", lines);
        return this;
    }

    //获取请求行
    public Map<String, String> getLines() {
        return metaData.get("lines");
    }

    public Crawler(Crawler crawler) {
        try {//this是新创建的子爬虫
            BeanUtils.copyProperties(this, crawler);//继承公共参数
        } catch (Exception e) {
            e.printStackTrace();
            log.warn("继承公共参数失败，可能会对程序逻辑有影响，请检查，错误信息：" + e.getMessage());
        }
    }

    /**
     * 创建时子类爬虫深度会自动+1
     *
     * @param url url
     * @return 返回子爬虫
     */
    public Crawler addSeed(String url) {
        if (StringUtil.isNotEmpty(url)) {
            //this是当前爬虫
            Crawler crawler = new Crawler(this);
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
    public void addSeeds(List<String> urls, String type) {
        for (String url : urls) {
            if (StringUtil.isNotEmpty(url)) {
                addSeed(url).setType(type);
            }
        }
    }

    public Crawler(String url) {
        this.url = url;
    }

    public Crawler(String url, String type) {
        this.url = url;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Crawler crawler = (Crawler) o;
        return Objects.equals(url, crawler.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

}
