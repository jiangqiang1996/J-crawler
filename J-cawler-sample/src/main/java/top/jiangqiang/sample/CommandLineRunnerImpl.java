package top.jiangqiang.sample;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import top.jiangqiang.core.app.GenericStarter;
import top.jiangqiang.core.config.CrawlerGlobalConfig;
import top.jiangqiang.core.entities.Crawler;
import top.jiangqiang.core.entities.Page;
import top.jiangqiang.core.handler.ResultHandler;
import top.jiangqiang.core.recorder.RamRecorder;
import top.jiangqiang.core.recorder.Recorder;
import top.jiangqiang.core.util.FileUtil;
import top.jiangqiang.core.util.JSONUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CommandLineRunnerImpl implements CommandLineRunner {
    @Override
    public void run(String... args) {
//        fetchOpenSourceChina();
//        fetchWeChatArticle();
//        fetchPicture();
        fetchPicture1();
    }

    private void fetchPicture1() {
        RamRecorder ramRecorder = new RamRecorder();
        CrawlerGlobalConfig crawlerGlobalConfig = new CrawlerGlobalConfig();
        //下面写法会在任务开始前执行
        ramRecorder.setInitCallback(recorder -> {
            /**
             * 这里可以从数据库中读取上次保存的种子。。。
             */
            //https://www.pixiv.net/artworks/101741272
            /**
             * mode 按天或按周 daily按天
             * content illust
             * date 按天时的日期
             * format 返回的格式
             * p 页数 1-10
             * https://www.pixiv.net/ranking.php?mode=daily&content=illust&date=20221007&p=1&format=json
             */
            Date date = new Date();
            int days = 3;
            for (int i = 0; i < days; i++) {
                String dateStr = DateUtil.format(date, DatePattern.PURE_DATE_PATTERN);
                date = DateUtil.offsetDay(date, -1);
                int pages = 1;
                for (int page = 1; page <= pages; page++) {
                    String url;
                    if (days == 1) {
                        url = "https://www.pixiv.net/ranking.php?mode=daily&content=illust" + "&p=" + page + "&format=json";
                    } else {
                        url = "https://www.pixiv.net/ranking.php?mode=daily&content=illust&date=" + dateStr + "&p=" + page + "&format=json";
                    }
                    System.out.println(url);
                    Crawler crawler = new Crawler(url);
                    recorder.add(crawler);
                }
            }
        });
//        crawlerGlobalConfig.addRegEx("(http|https)://.*");
        crawlerGlobalConfig.setAllowEnd(true);
        crawlerGlobalConfig.setForceEnd(true);
        crawlerGlobalConfig.setDepth(3);
        crawlerGlobalConfig.setMaxSize((long) 1024 * 1024);
        crawlerGlobalConfig.setUseProxy(true);
        crawlerGlobalConfig.addProxyIp("127.0.0.1");
        crawlerGlobalConfig.addProxyPort("7890");
        crawlerGlobalConfig.addProxyProtocol("HTTP");
        crawlerGlobalConfig.setLogLevel(HttpLoggingInterceptor.Level.BODY);
        new GenericStarter(crawlerGlobalConfig, ramRecorder, new ResultHandler() {
            public Set<Crawler> doSuccess(Recorder recorder, Crawler crawler, Page page, Response response) {
                // 储存下载文件的目录
                File dir = FileUtil.file("D:/cache/cache");
//                FileUtil.downloadFile(page, response, dir.getAbsolutePath());
                String mimeType = FileUtil.subMimeType(page.getContentType());
                if ("application/json".equals(mimeType) && page.getUrl().startsWith("https://www.pixiv.net/ranking.php")) {
                    String content = page.getContent();
                    HashMap<String, Object> hashMap = JSONUtil.parse(content, new HashMap<>());
                    JSONArray contents = (JSONArray) hashMap.get("contents");
                    if (CollUtil.isNotEmpty(contents)) {
                        Set<JSONObject> jsonObjects = contents.stream().map(o -> (JSONObject) o).collect(Collectors.toSet());
                        List<String> strings = jsonObjects.stream().map(jsonObject -> {
                            Object illust_id = jsonObject.get("illust_id");
                            if (illust_id != null) {
                                return "https://www.pixiv.net/artworks/" + illust_id;
                            } else return null;
                        }).filter(StrUtil::isNotBlank).toList();
                        page.addSeeds(strings);
                    }
                }
                if (page.getUrl().startsWith("https://www.pixiv.net/artworks/")) {
                    System.out.println(page.getContent());
                }
                return page.getCrawlers();
            }
        }).start();
    }

    private void fetchPicture() {
        RamRecorder ramRecorder = new RamRecorder();
        Crawler crawler = new Crawler("https://www.huashi6.com/rank");
        crawler.addParam("key", "value1");
        crawler.addLine("method", "GET");
        crawler.addHeader("Referer", "http://www.baidu.com");
        ramRecorder.add(crawler);
        CrawlerGlobalConfig crawlerGlobalConfig = new CrawlerGlobalConfig();
//        crawlerGlobalConfig.addRegEx("(http|https)://.*");
        crawlerGlobalConfig.setDepth(3);
        crawlerGlobalConfig.setMaxSize((long) 1024 * 1024);
        new GenericStarter(crawlerGlobalConfig, ramRecorder, new ResultHandler() {
            public Set<Crawler> doSuccess(Recorder recorder, Crawler crawler, Page page, Response response) {

                List<String> urlList = new ArrayList<>();
                String regEx = """
                        ("originalPath":")[(\\w),(\\\\u002F)]{1,}(\\.)([a-z]{3})
                        """;
                List<String> allGroups = ReUtil.findAllGroup0(Pattern.compile(regEx.trim(), Pattern.DOTALL), page.getContent());
                for (String str : allGroups) {
                    String url = "http://img2.huashi6.com/" + str.substring(16).replaceAll("\\\\u002F", "/");
                    if (StrUtil.isNotBlank(url)) {
                        urlList.add(url);
                    }
                }
                page.addSeeds(urlList);
                return page.getCrawlers();
            }

            public void doFailure(Recorder recorder, Crawler crawler, IOException e) {

            }
        }).start();
    }

    void fetchWeChatArticle() {
        RamRecorder ramRecorder = new RamRecorder();
        ramRecorder.add(new Crawler("https://mp.weixin.qq.com/s?__biz=MzIxMjgzMDUyNw==&mid=2247489048&idx=1&sn=072866b456945d297ec2516dd72e5a41&chksm=97414648a036cf5eba9ddf88c7cf7a27809ae414b4ce43d5595c7351172d04d70664eab25761&scene=90&subscene=93&sessionid=1664460391&clicktime=1664460397&enterid=1664460397&ascene=56&fasttmpl_type=0&fasttmpl_fullversion=6351034-zh_CN-zip&fasttmpl_flag=0&realreporttime=1664460397767&devicetype=android-31&version=28001c3b&nettype=WIFI&abtest_cookie=AAACAA%3D%3D&lang=zh_CN&session_us=gh_391abad800db&exportkey=A01mvM0fP%2BtbjfOBlrDdga8%3D&pass_ticket=fRKCL5vF5nmJEU4Y0DJ60ftOP9hbDgcI5Syn9wR%2BP26sjnBzcmbbozXA3pV42cES&wx_header=3"));
        CrawlerGlobalConfig crawlerGlobalConfig = new CrawlerGlobalConfig();
        crawlerGlobalConfig.addRegEx("(http|https)://.*");
        crawlerGlobalConfig.setAllowEnd(true);
        crawlerGlobalConfig.setForceEnd(true);
        crawlerGlobalConfig.setDepth(3);
        new GenericStarter(crawlerGlobalConfig, ramRecorder, new ResultHandler() {
            public Set<Crawler> doSuccess(Recorder recorder, Crawler crawler, Page page, Response response) {
//                Set<Crawler> crawlers = page.getCrawlers().stream().filter(
//                        crawler1 -> {
//                            return ReUtil.isMatch(".*\\.(jpg|jpeg|png|webp|gif)", crawler1.getUrl());
//                        }
//                ).collect(Collectors.toSet());
                List<String> urlList = page.getCrawlers().stream().map(Crawler::getUrl).toList();
//                System.out.println(urlList.size());
//                System.out.println(urlList);
                page.addSeeds(urlList);
                return page.getCrawlers();
            }

            public void doFailure(Recorder recorder, Crawler crawler, IOException e) {

            }
        }).start();
    }

    /**
     * 爬取开源中国的数据
     */
    void fetchOpenSourceChina() {
        RamRecorder ramRecorder = new RamRecorder();
        ramRecorder.add(new Crawler("https://gitee.com/explore"));
        CrawlerGlobalConfig crawlerGlobalConfig = new CrawlerGlobalConfig();
        crawlerGlobalConfig.addRegEx("http://.*");
        crawlerGlobalConfig.addRegEx("https://.*");
        crawlerGlobalConfig.setDepth(2);
        GenericStarter genericStarter = new GenericStarter(crawlerGlobalConfig, ramRecorder, new ResultHandler() {
            public Set<Crawler> doSuccess(Recorder recorder, Crawler crawler, Page page, Response response) {
                if (ReUtil.isMatch("^https://gitee.com/explore/([A-Za-z0-9-]{1,})", page.getUrl())) {
                    match(page);
                }
                Set<Crawler> crawlers = page.getCrawlers().stream().filter(
                        crawler1 -> ReUtil.isMatch("^https://gitee.com/explore/([A-Za-z0-9-]{1,})", crawler1.getUrl())
                ).collect(Collectors.toSet());
                List<String> strings = crawlers.stream().map(Crawler::getUrl).toList();
//                System.out.println(strings);
                return crawlers;
            }


            public void doFailure(Recorder recorder, Crawler crawler, IOException e) {

            }
        });
        genericStarter.start();
    }

    /**
     * 匹配左侧菜单URL
     *
     * @param page
     */
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