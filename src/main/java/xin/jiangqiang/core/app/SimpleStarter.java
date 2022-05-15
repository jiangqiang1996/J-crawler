package xin.jiangqiang.core.app;

import cn.hutool.core.lang.Singleton;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import xin.jiangqiang.common.DocumentUtil;
import xin.jiangqiang.core.config.Config;
import xin.jiangqiang.core.entities.Crawler;
import xin.jiangqiang.core.entities.Page;
import xin.jiangqiang.core.net.OkHttpClientHelper;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @Author: JiangQiang
 * @Date: 2022年05月14日 13:45
 */
@Slf4j
public class SimpleStarter extends AbstractStarter {
    @Override
    public Runnable getTask(Crawler crawler) {
        return new Task(crawler);
    }

    @Data
    protected class Task implements Runnable {
        Crawler crawler;
        Config config = Singleton.get(Config.class);

        public Task(Crawler crawler) {
            this.crawler = crawler;
        }

        @Override
        public void run() {
            OkHttpClientHelper okHttpClientHelper = Singleton.get(OkHttpClientHelper.class);
            Call call = okHttpClientHelper.request(crawler);
            if (call == null) {
                recorder.addErr(crawler);
                return;
            }
            if (config.getAsync()) {
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        recorder.addErr(crawler);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        deal(response);
                    }
                });
            } else {
                try {
                    Response response = call.execute();
                    deal(response);
                } catch (IOException e) {
                    log.info("URL： " + crawler.getUrl() + " " + e.getMessage());
                }
            }
        }

        /**
         * 处理响应请求
         *
         * @param response
         * @throws IOException
         */
        private void deal(Response response) throws IOException {
            Integer code = response.code();
            ResponseBody body = response.body();
            byte[] content = Objects.requireNonNull(response.body()).bytes();
            String contentType = "";
            if (body != null) {
                MediaType mediaType = body.contentType();
                if (mediaType != null) {
                    contentType = mediaType.toString();
                }
            }
            Page page = Page.getPage(crawler, code, contentType, content, config.getCharset());
            //如果正正则或反正则列表有一个有值，就会抽取所有URL
            if (config.getRegExs().size() != 0 || config.getReverseRegExs().size() != 0) {
                //此处用于抓取所有URL
                List<String> urls = DocumentUtil.getAllUrl(page.getHtml(), crawler.getUrl());
                //使用正则表达式筛选URL
                urls = getMatchUrls(urls);
                page.addSeeds(urls, "");
            }
            callMethodHelper.match(page);
            callMethodHelper.deal(page);
            //处理完成，加入成功结果集
            String tmpCode = page.getResponseCode().toString();
            if (tmpCode.startsWith("2")) {
                recorder.addSucc(page);
            } else {
                recorder.addErr(page);
            }
            if (page.getDepth() <= (config.getDepth())) {//当前爬虫深度没有达到设置的级别，加入爬虫任务列表
                //过滤本次提取出来的URL
                filter.filter(page.getCrawlers());
                //把没有爬取的加入任务列表
            }
        }
    }
}
