package top.jiangqiang.sample.jiangqiang.sample;

import cn.hutool.core.lang.Singleton;
import top.jiangqiang.sample.jiangqiang.core.annotation.After;
import top.jiangqiang.sample.jiangqiang.core.annotation.Before;
import top.jiangqiang.sample.jiangqiang.core.annotation.Deal;
import top.jiangqiang.sample.jiangqiang.core.annotation.Match;
import top.jiangqiang.sample.jiangqiang.core.config.Config;
import top.jiangqiang.sample.jiangqiang.core.entities.Crawler;
import top.jiangqiang.sample.jiangqiang.core.entities.Page;
import top.jiangqiang.sample.jiangqiang.core.filter.NextFilter;
import top.jiangqiang.sample.jiangqiang.core.recoder.RAMRecorder;
import lombok.extern.slf4j.Slf4j;
import top.jiangqiang.sample.jiangqiang.core.app.SimpleStarter;

/**
 * @Author: JiangQiang
 * @Date: 2022年05月14日 09:51
 */
@Slf4j
public class MyStarter extends SimpleStarter {

    public static void main(String[] args) {
        Config config = Singleton.get(Config.class);
        /**
         * start方法上
         */
        config.setAppClass(MyStarter.class);
        config.setFilterClass(NextFilter.class);
        config.setRecorderClass(RAMRecorder.class);
        config.setThreads(10);
        config.setDepth(2);
        config.setAsync(false);
        /**
         * 满足正则表达式的所有URL将会自动提取到deal生命周期的page对象
         */
        config.addRegEx("https://.*");
        /**
         * 启用默认内置正则表达式过滤，默认会过滤css，js的url
         */
        config.setIsUseDefault(true);

        config.addHeaders("admin-authorization", "3764bce099ba42e1a114583bc312279a");
        config.addHeaders("cookie", "__gads=ID=f9ab7b07a350c9ac-225b11b988d200cc:T=1651621186:RT=1651621186:S=ALNI_Mb1DPwmzj084XwV9QkER_bPe87SlA; __gpi=UID=000005261783952b:T=1651633467:RT=1652575639:S=ALNI_MYsmI_kSJTfRhtznhJztC-ifaqfBw; JSESSIONID=node01qeu9cawpkm4l16hjtfomoefz595.node0");
        config.addHeaders("referer", "https://www.qianyi.xin/admin/index.html");
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
        /**
         * 筛选下一代子爬虫，此处也可以实现对下一代的过滤功能
         */
        for (Crawler crawler : page.getCrawlers()) {
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
