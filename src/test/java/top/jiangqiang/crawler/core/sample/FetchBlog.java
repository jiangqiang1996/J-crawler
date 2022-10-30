package top.jiangqiang.crawler.core.sample;

import okhttp3.Response;
import top.jiangqiang.crawler.core.app.GenericStarter;
import top.jiangqiang.crawler.core.config.CrawlerGlobalConfig;
import top.jiangqiang.crawler.core.entities.Crawler;
import top.jiangqiang.crawler.core.entities.Page;
import top.jiangqiang.crawler.core.handler.ResultHandler;
import top.jiangqiang.crawler.core.recorder.RamRecorder;
import top.jiangqiang.crawler.core.recorder.Recorder;
import top.jiangqiang.crawler.core.util.FileUtil;

import java.io.File;
import java.util.List;

/**
 * @author jiangqiang
 * @date 2022-10-30
 */
public class FetchBlog {
    public static void main(String[] args) {
        RamRecorder ramRecorder = new RamRecorder();
        CrawlerGlobalConfig crawlerGlobalConfig = new CrawlerGlobalConfig();
        crawlerGlobalConfig.setMaxSize(10 * 1024 * 1024L);
        crawlerGlobalConfig.setIsUseDefault(false);
        crawlerGlobalConfig.addRegEx("https://candinya.com/[\\w./-]+");
        crawlerGlobalConfig.setStrong(true);
        ramRecorder.setInitCallback(recorder -> {
            recorder.add(new Crawler("https://candinya.com/"));
        });
        String fileDirPath = System.getProperty("user.dir") + "/tmp";
        new GenericStarter(crawlerGlobalConfig, ramRecorder, new ResultHandler() {
            @Override
            public List<Crawler> doSuccess(Recorder recorder, Crawler crawler, Page page, Response response) {
                if ("https://candinya.com".equals(page.getUrl()) || "https://candinya.com/".equals(page.getUrl())) {
                    FileUtil.downloadFile(page, response, FileUtil.file(fileDirPath + "/home.html"));
                } else {
                    File file = FileUtil.file(page.getUrl().replace("https://candinya.com", fileDirPath));
                    FileUtil.downloadFile(page, response, file);
                }
                return page.getCrawlers();
            }
        }).start();
    }
}
