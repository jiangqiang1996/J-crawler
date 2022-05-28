package xin.jiangqiang.sample;

import cn.hutool.core.lang.Singleton;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xin.jiangqiang.core.annotation.Before;
import xin.jiangqiang.core.annotation.Match;
import xin.jiangqiang.core.app.SimpleStarter;
import xin.jiangqiang.core.config.Config;
import xin.jiangqiang.core.entities.Crawler;
import xin.jiangqiang.core.entities.Page;
import xin.jiangqiang.core.filter.NextFilter;
import xin.jiangqiang.core.recoder.RAMRecorder;


/**
 * @Author: JiangQiang
 * @Date: 2022年05月28日 09:51
 */
@Slf4j
public class MyStarterGitee extends SimpleStarter {

    public static void main(String[] args) {
        Config config = Singleton.get(Config.class);
        /**
         * start方法上
         */
        config.setAppClass(MyStarterGitee.class);
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

        new MyStarterGitee().start(MyStarterGitee.class);
    }

    @Before
    public void before() {
        log.info("before");
        Crawler crawler = new Crawler("https://gitee.com/explore");
        crawler.setData("aa", "aa");
        recorder.add(crawler);
    }

    /**
     * 匹配左侧菜单URL
     *
     * @param page
     */
    @Match(regEx = "^https://gitee.com/explore/([A-Za-z0-9-]{1,})")
    public void match(Page page) {
        log.info("match: {}", page.getUrl());
        try {
            Elements select = page.getDocument().select(".pagination>.item");//翻页URL
            if (select.size() > 0) {
                String text = select.get(select.size() - 2).text();
                if (StrUtil.isNotBlank(text)) {
                    int pages = Integer.parseInt(text);//最大页数
                    for (int i = 1; i <= pages; i++) {
                        String url = page.getUrl();
                        if (url.contains("?page=")) {
                            String[] split = url.split("page=");
                            url = split[0] + "page=" + i;
                            page.addSeed(url);
                            log.info("url: " + url);
                        } else {
                            url = url + "?page=" + i;
                            log.info("url: " + url);
                            page.addSeed(url);
                        }
                    }
                }
            }
            Elements elements = page.getDocument().select("div.ui.relaxed.divided.items.explore-repo__list>.item");
            Element categoryElement = page.getDocument().selectFirst("div > div.explore-project__selection > div > div.ui.breadcrumb > div.section");
            String category = categoryElement.text();
            for (Element element : elements) {
                Element contentEle = element.selectFirst(".content");
                Element tagA = contentEle.selectFirst("h3 > a");
                String url = tagA.absUrl("href");//项目地址
                String title = tagA.text();//项目标题
                Element descEle = element.selectFirst(".project-desc");
                String desc = descEle.text();
                String language = contentEle.select(".project-language").text();
                String type = contentEle.select(".project-item-bottom__item").text();
                String time = contentEle.select(".text-muted").text();
                log.info("category: " + category + " title: " + title + " url: " + url + " desc: " + desc + " language: " + language + " type: " + type + " time: " + time);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
