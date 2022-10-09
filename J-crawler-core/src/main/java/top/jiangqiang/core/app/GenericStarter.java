package top.jiangqiang.core.app;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import top.jiangqiang.core.config.CrawlerGlobalConfig;
import top.jiangqiang.core.entities.Crawler;
import top.jiangqiang.core.entities.Page;
import top.jiangqiang.core.handler.ResultHandler;
import top.jiangqiang.core.http.OkHttpUtil;
import top.jiangqiang.core.recorder.Recorder;
import top.jiangqiang.core.util.DocumentUtil;

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
            Call call = OkHttpUtil.request(crawler, getGlobalConfig());
            if (call == null) {
                getRecorder().addError(crawler);
                getRecorder().removeActive(crawler);
                return;
            }
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    try {
                        getResultHandler().doFailure(getRecorder(), crawler, e);
                    } catch (Exception ignored) {
                        log.info(ignored.getMessage());
                    } finally {
                        //处理完成，加入失败结果集
                        getRecorder().addError(crawler);
                        getRecorder().removeActive(crawler);
                    }
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    boolean isSuccess = true;
                    try {
                        doSuccess(crawler, response);
                    } catch (Exception ignored) {
                        isSuccess=false;
                        log.info(ignored.getMessage());
                    } finally {
                        if (isSuccess) {
                            //处理完成，加入成功结果集
                            getRecorder().addSuccess(crawler);
                        } else {
                            //处理完成，加入失败结果集
                            getRecorder().addError(crawler);
                        }
                        getRecorder().removeActive(crawler);
                    }
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
        if (body == null) {
            return;
        }
        MediaType mediaType = body.contentType();
        if (mediaType == null) {
            return;
        }
        String contentType = mediaType.toString();
        long contentLength = body.contentLength();

        List<String> mimeTypeList = globalConfig.getMimeTypeList();
        if (StrUtil.isNotBlank(contentType)
                && (mimeTypeList.contains(contentType) || mimeTypeList.contains(mediaType.type()))
                && contentLength <= getGlobalConfig().getMaxSize()) {
            byte[] bodyBytes = null;
            try {
                bodyBytes = body.bytes();
            } catch (IOException e) {
                log.debug(e.getMessage());
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
            Set<Crawler> crawlers = getResultHandler().doSuccess(recorder, crawler, page, response);
            //当前爬虫深度没有达到设置的级别，加入爬虫任务列表
            if (page.getDepth() < (getGlobalConfig().getDepth())) {
                //把没有爬取的加入任务列表，在addAll方法中需要自定义实现过滤
                getRecorder().addAll(new ArrayList<>(crawlers));
            }
        } else {
            Page page = Page.getPage(crawler, code, contentType);
            getResultHandler().doSuccess(recorder, crawler, page, response);
        }
    }
}
