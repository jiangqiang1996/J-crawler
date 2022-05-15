package xin.jiangqiang.sample;

import cn.hutool.core.lang.Singleton;
import lombok.extern.slf4j.Slf4j;
import xin.jiangqiang.core.config.Config;
import xin.jiangqiang.core.annotation.After;
import xin.jiangqiang.core.annotation.Before;
import xin.jiangqiang.core.annotation.Deal;
import xin.jiangqiang.core.annotation.Match;
import xin.jiangqiang.core.app.SimpleStarter;
import xin.jiangqiang.core.entities.Crawler;
import xin.jiangqiang.core.entities.Page;
import xin.jiangqiang.core.filter.Filter;
import xin.jiangqiang.core.filter.NextFilter;
import xin.jiangqiang.core.net.RequestMethod;
import xin.jiangqiang.core.recoder.RAMRecorder;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: JiangQiang
 * @Date: 2022年05月14日 09:51
 */
@Slf4j
public class MyStarter extends SimpleStarter {

    public static void main(String[] args) {
        Config config = Singleton.get(Config.class);
        config.setAppClass(MyStarter.class);//start方法上
        config.setFilterClass(NextFilter.class);
        config.setRecorderClass(RAMRecorder.class);
        config.setThreads(10);
        config.setDepth(2);
        config.setAsync(false);
        config.addRegEx("https://.*");//满足正则表达式的所有URL将会自动提取到deal生命周期的page对象
        config.setIsUseDefault(true);//启用默认内置正则表达式过滤，默认会过滤css，js的url

        config.addHeaders("admin-authorization", "3764bce099ba42e1a114583bc312279a");
        config.addHeaders("cookie","__gads=ID=f9ab7b07a350c9ac-225b11b988d200cc:T=1651621186:RT=1651621186:S=ALNI_Mb1DPwmzj084XwV9QkER_bPe87SlA; __gpi=UID=000005261783952b:T=1651633467:RT=1652575639:S=ALNI_MYsmI_kSJTfRhtznhJztC-ifaqfBw; JSESSIONID=node01qeu9cawpkm4l16hjtfomoefz595.node0");
        config.addHeaders("referer","https://www.qianyi.xin/admin/index.html");
//        config.addHeaders("","");
//        config.addHeaders("","");
//        config.addHeaders("","");

        new MyStarter().start(MyStarter.class);
    }

    @Before
    public void before() {
        log.info("before");
        Crawler crawler = new Crawler("https://www.qianyi.xin/api/admin/posts?page=0&size=10");
        recorder.add(crawler);
    }

    /**
     * 解析页面数据，包括页面中的文字内容或者文件内容，这里可以排除文件类型，不进行爬取
     * 这里也可以过滤下一代爬虫
     *
     * @param page
     */
    @Deal
    public void deal(Page page) {
        log.info(page.getHtml());
        for (Crawler crawler : page.getCrawlers()) {//筛选下一代子爬虫，此处也可以实现对下一代的过滤功能
            log.info("正在解析的URL： " + page.getUrl() + " 深度：" + page.getDepth() + "解吸出来的子爬虫URL： " + crawler.getUrl() + " 深度：" + crawler.getDepth());
            recorder.add(crawler);
        }
        log.info("deal");
    }

    @Match
    public void match(Page page) {
        log.info("match");
    }

    @After
    public void after() {
        log.info("after");
    }
}
