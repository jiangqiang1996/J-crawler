package xin.jiangqiang.core.entities;

import cn.hutool.core.util.StrUtil;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import xin.jiangqiang.common.BeanUtil;
import xin.jiangqiang.core.net.RequestMethod;

import java.io.Serializable;
import java.util.*;

/**
 * @Author: JiangQiang
 * @Date: 2022年05月14日 10:43
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
    //根据type执行不同逻辑，对应@Match注解的value
    private String type = "";//对爬虫进行分类，方便后面使用@Match进行分类处理
    private Set<Crawler> crawlers = new HashSet<>();//当前URL中提取出来的子爬虫
    @Setter(AccessLevel.NONE)
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
        this.metaData = BeanUtil.clone(crawler.getMetaData());
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
}
