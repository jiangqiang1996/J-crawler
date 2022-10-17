package top.jiangqiang.crawler.core.app;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import top.jiangqiang.crawler.core.config.CrawlerGlobalConfig;
import top.jiangqiang.crawler.core.config.LoginConfig;
import top.jiangqiang.crawler.core.entities.Crawler;
import top.jiangqiang.crawler.core.entities.Page;
import top.jiangqiang.crawler.core.handler.DefaultResultHandler;
import top.jiangqiang.crawler.core.handler.ResultHandler;
import top.jiangqiang.crawler.core.http.OkHttpService;
import top.jiangqiang.crawler.core.recorder.RamRecorder;
import top.jiangqiang.crawler.core.recorder.Recorder;
import top.jiangqiang.crawler.core.util.DocumentUtil;
import top.jiangqiang.crawler.core.util.FileUtil;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @Author: JiangQiang
 * @Date: 2022年05月14日 13:45
 */
@Slf4j
@Getter
public class GenericStarter extends AbstractStarter {
    private final CrawlerGlobalConfig globalConfig;
    private final Recorder recorder;
    private final ResultHandler resultHandler;
    private final OkHttpService okHttpService;

    public GenericStarter(CrawlerGlobalConfig globalConfig, Recorder recorder, ResultHandler resultHandler, LoginConfig loginConfig, Interceptor... interceptors) {
        this.globalConfig = Objects.requireNonNullElseGet(globalConfig, CrawlerGlobalConfig::new);
        this.recorder = Objects.requireNonNullElseGet(recorder, RamRecorder::new);
        this.resultHandler = Objects.requireNonNullElseGet(resultHandler, DefaultResultHandler::new);
        this.recorder.setConfig(this.globalConfig);
        this.setLoginConfig(loginConfig);
        this.okHttpService = new OkHttpService(this.globalConfig, interceptors);
    }

    public GenericStarter(CrawlerGlobalConfig globalConfig, Recorder recorder, ResultHandler resultHandler, Interceptor... interceptors) {
        this.globalConfig = Objects.requireNonNullElseGet(globalConfig, CrawlerGlobalConfig::new);
        this.recorder = Objects.requireNonNullElseGet(recorder, RamRecorder::new);
        this.resultHandler = Objects.requireNonNullElseGet(resultHandler, DefaultResultHandler::new);
        this.recorder.setConfig(this.globalConfig);
        this.okHttpService = new OkHttpService(this.globalConfig, interceptors);
    }

    public GenericStarter(Recorder recorder, ResultHandler resultHandler, LoginConfig loginConfig, Interceptor... interceptors) {
        this.globalConfig = new CrawlerGlobalConfig();
        this.recorder = Objects.requireNonNullElseGet(recorder, RamRecorder::new);
        this.resultHandler = Objects.requireNonNullElseGet(resultHandler, DefaultResultHandler::new);
        this.recorder.setConfig(this.globalConfig);
        this.setLoginConfig(loginConfig);
        this.okHttpService = new OkHttpService(this.globalConfig, interceptors);
    }

    public GenericStarter(Recorder recorder, ResultHandler resultHandler, Interceptor... interceptors) {
        this.globalConfig = new CrawlerGlobalConfig();
        this.recorder = Objects.requireNonNullElseGet(recorder, RamRecorder::new);
        this.resultHandler = Objects.requireNonNullElseGet(resultHandler, DefaultResultHandler::new);
        this.recorder.setConfig(this.globalConfig);
        this.okHttpService = new OkHttpService(this.globalConfig, interceptors);
    }

    public GenericStarter(ResultHandler resultHandler, LoginConfig loginConfig, Interceptor... interceptors) {
        this.globalConfig = new CrawlerGlobalConfig();
        this.recorder = new RamRecorder();
        this.resultHandler = Objects.requireNonNullElseGet(resultHandler, DefaultResultHandler::new);
        this.recorder.setConfig(this.globalConfig);
        this.setLoginConfig(loginConfig);
        this.okHttpService = new OkHttpService(this.globalConfig, interceptors);
    }

    public GenericStarter(ResultHandler resultHandler, Interceptor... interceptors) {
        this.globalConfig = new CrawlerGlobalConfig();
        this.recorder = new RamRecorder();
        this.resultHandler = Objects.requireNonNullElseGet(resultHandler, DefaultResultHandler::new);
        this.recorder.setConfig(this.globalConfig);
        this.okHttpService = new OkHttpService(this.globalConfig, interceptors);
    }

    @Override
    public Runnable getTask(Crawler crawler) {
        return new Task(crawler);
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
        String contentType = null;
        MediaType mediaType = body.contentType();
        if (mediaType != null) {
            contentType = mediaType.toString();
        }
        long contentLength = body.contentLength();
        if (StrUtil.isNotBlank(contentType)) {
            String contentTypeNoCharset = FileUtil.subMimeType(contentType);
            List<String> mimeTypeList = globalConfig.getMimeTypeList();
            if (contentLength <= getGlobalConfig().getMaxSize() && (mimeTypeList.contains(contentTypeNoCharset) || mimeTypeList.contains(mediaType.type()))) {
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
                page.setContentLength(contentLength);
                List<Crawler> crawlers = getResultHandler().doSuccess(recorder, crawler, page, response);
                //当前爬虫深度没有达到设置的级别，加入爬虫任务列表
                if (page.getDepth() < (getGlobalConfig().getDepth())) {
                    //把没有爬取的加入任务列表，在addAll方法中需要自定义实现过滤
                    getRecorder().addAll(crawlers);
                }
                return;
            }
        }
        Page page = Page.getPage(crawler, code, contentType);
        page.setContentLength(contentLength);
        getResultHandler().doSuccess(recorder, crawler, page, response);
    }

    @Data
    protected class Task implements Runnable {
        Crawler crawler;

        public Task(Crawler crawler) {
            this.crawler = crawler;
        }

        @Override
        public void run() {
            Call call = okHttpService.request(crawler);
            if (call == null) {
                crawler.setErrorMessage("okhttp客户端出错，请检查请求相关配置");
                crawler.setErrorCode(1);
                getRecorder().activeToError(crawler);
                return;
            }
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException ioException) {
                    try {
                        getResultHandler().doFailure(getRecorder(), crawler, ioException);
                    } catch (Exception exception) {
                        log.info(exception.getMessage());
                    } finally {
                        crawler.setErrorMessage("请求出错：" + recorder.getErrorMessage(ioException));
                        crawler.setErrorCode(2);
                        //处理完成，加入失败结果集
                        getRecorder().activeToError(crawler);
                    }
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    try {
                        doSuccess(crawler, response);
                        //处理完成，加入成功结果集
                        getRecorder().activeToSuccess(crawler);
                    } catch (Exception exception) {
                        log.info(exception.getMessage());
                        crawler.setErrorMessage("数据处理出错：" + recorder.getErrorMessage(exception));
                        crawler.setErrorCode(3);
                        //处理完成，加入失败结果集
                        getRecorder().activeToError(crawler);
                    }
                }
            });
        }
    }
}
