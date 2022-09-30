package top.jiangqiang.core.app;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import top.jiangqiang.core.common.DocumentUtil;
import top.jiangqiang.core.config.CrawlerGlobalConfig;
import top.jiangqiang.core.entities.Crawler;
import top.jiangqiang.core.entities.Page;
import top.jiangqiang.core.handler.ResultHandler;
import top.jiangqiang.core.http.HttpUtil;
import top.jiangqiang.core.recorder.Recorder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @Author: JiangQiang
 * @Date: 2022年05月14日 13:45
 */
@Slf4j
@AllArgsConstructor
@Getter
public class GenericStarter extends AbstractStarter {
    private final CrawlerGlobalConfig globalConfig;
    private final Recorder recorder;
    private final ResultHandler resultHandler;

    @Override
    public Runnable getTask(Crawler crawler) {
        return new Task(crawler);
    }

    @Data
    protected class Task implements Runnable {
        Crawler crawler;

        public Task(Crawler crawler) {
            this.crawler = crawler;
        }

        @Override
        public void run() {
            Call call = HttpUtil.request(crawler, getGlobalConfig());
            if (call == null) {
                getRecorder().addError(crawler);
                return;
            }
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    getResultHandler().doFailure(recorder, crawler, e);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    doSuccess(crawler, response);
                }
            });
        }
    }

    /**
     * 处理响应请求
     *
     * @param crawler
     * @param response
     * @throws IOException
     */
    public final void doSuccess(Crawler crawler, Response response) {
        Integer code = response.code();
        ResponseBody body = response.body();
        byte[] bodyBytes = null;
        try {
            if (body != null) {
                bodyBytes = body.bytes();
            }
        } catch (IOException e) {
            log.debug(e.getMessage());
        }
        String contentType = "";
        if (body != null) {
            MediaType mediaType = body.contentType();
            if (mediaType != null) {
                contentType = mediaType.toString();
            }
        }
        Page page = Page.getPage(crawler, code, contentType, bodyBytes, getGlobalConfig().getCharset());
        //如果正正则或反正则列表有一个有值，就会抽取所有URL
        if (getGlobalConfig().getRegExs().size() != 0 || getGlobalConfig().getReverseRegExs().size() != 0) {
            //此处用于抓取所有URL
            List<String> urls = DocumentUtil.getAllUrl(page.getContent(), crawler.getUrl(), getGlobalConfig().getStrong());
            //使用正则表达式筛选URL
            urls = getMatchUrls(urls);
            page.addSeeds(urls);
        }
        Set<Crawler> crawlers = getResultHandler().doSuccess(recorder, crawler, page);
        if (page.getDepth() < (getGlobalConfig().getDepth())) {//当前爬虫深度没有达到设置的级别，加入爬虫任务列表
            //过滤本次提取出来的URL
            getRecorder().addAll(new ArrayList<>(crawlers));
            //把没有爬取的加入任务列表
        }
    }

}
