package top.jiangqiang.crawler.sample;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import top.jiangqiang.crawler.core.app.GenericStarter;
import top.jiangqiang.crawler.core.config.CrawlerGlobalConfig;
import top.jiangqiang.crawler.core.entities.Crawler;
import top.jiangqiang.crawler.core.entities.Page;
import top.jiangqiang.crawler.core.handler.ResultHandler;
import top.jiangqiang.crawler.core.recorder.RamRecorder;
import top.jiangqiang.crawler.core.recorder.Recorder;
import top.jiangqiang.crawler.core.util.FileUtil;
import top.jiangqiang.crawler.core.util.JSONUtil;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * @author Jiangqiang
 * @version 1.0
 * @description TODO
 * @date 2022/10/10 9:33
 */
@Slf4j
public class FetchPixiv {
    public static void main(String[] args) {
        fetchPicture1();
    }

    private static void fetchPicture1() {
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
            int days = 30;
            for (int day = 1; day <= days; day++) {
                String dateStr = DateUtil.format(date, DatePattern.PURE_DATE_PATTERN);
                date = DateUtil.offsetDay(date, -1);
                int pages = 10;
                for (int page = 1; page <= pages; page++) {
                    String url;
                    if (day == 1) {
                        url = "https://www.pixiv.net/ranking.php?mode=daily&content=illust" + "&p=" + page + "&format=json";
                    } else {
                        url = "https://www.pixiv.net/ranking.php?mode=daily&content=illust&date=" + dateStr + "&p=" + page + "&format=json";
                    }
//                    System.out.println(url);
                    Crawler crawler = new Crawler(url);
                    recorder.add(crawler);
                }
            }
        });
//        crawlerGlobalConfig.addRegEx("(http|https)://.*");
        crawlerGlobalConfig.setAllowEnd(true);
        crawlerGlobalConfig.setForceEnd(false);
        crawlerGlobalConfig.setDepth(3);
        crawlerGlobalConfig.setMaxSize((long) 1024 * 1024);
        crawlerGlobalConfig.setUseProxy(true);
        crawlerGlobalConfig.addProxyIp("127.0.0.1");
        crawlerGlobalConfig.addProxyPort("7890");
        crawlerGlobalConfig.addProxyProtocol("HTTP");
//        crawlerGlobalConfig.setLogLevel(HttpLoggingInterceptor.Level.BODY);
        new GenericStarter(crawlerGlobalConfig, ramRecorder, new ResultHandler() {
            public Set<Crawler> doSuccess(Recorder recorder, Crawler crawler, Page page, Response response) {
                // 储存下载文件的目录
                File dir = FileUtil.file("D:/cache/cache");
//                FileUtil.downloadFile(page, response, dir.getAbsolutePath());
                String mimeType = FileUtil.subMimeType(page.getContentType());
                if ("application/json".equals(mimeType) && page.getUrl().startsWith("https://www.pixiv.net/ranking.php")) {
                    String content = page.getContent();
                    if (StrUtil.isNotBlank(content)) {
                        HashMap<String, Object> hashMap = JSONUtil.parse(content, new HashMap<>());
                        JSONArray contents = (JSONArray) hashMap.get("contents");
                        if (CollUtil.isNotEmpty(contents)) {
                            List<JSONObject> jsonObjects = contents.stream().map(o -> (JSONObject) o).toList();
                            List<String> urlList = jsonObjects.stream().map(jsonObject -> {
                                Object illust_id = jsonObject.get("illust_id");
                                if (illust_id != null) {
                                    return "https://www.pixiv.net/artworks/" + illust_id;
                                } else return null;
                            }).filter(StrUtil::isNotBlank).toList();
                            page.addSeeds(urlList);
                        }
                    }

                }
                if (page.getUrl().startsWith("https://www.pixiv.net/artworks/")) {
//                    "original":"https://i.pximg.net/img-original/img/2022/10/05/12/00/01/101699259_p0.png";

                    String regEx = """
                                  "original":"https://[\\w./-]+
                            """;
                    String group0 = ReUtil.getGroup0(regEx.trim(), page.getContent());
                    if (StrUtil.isNotBlank(group0)) {
                        String url = StrUtil.subAfter(group0, "\"original\":\"", false);
                        page.addSeed(url);
                    }
                }
                if (page.getUrl().startsWith("https://i.pximg.net/img-original/img/") && mimeType.startsWith("image")) {
                    FileUtil.downloadFile(page, response, dir.getAbsolutePath());
                }
                return page.getCrawlers();
            }
        }).start();
    }
}
