package xin.jiangqiang.app;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import xin.jiangqiang.entities.Crawler;
import xin.jiangqiang.net.OkHttpClientHelper;
import xin.jiangqiang.entities.Page;
import xin.jiangqiang.util.DocumentUtil;

import java.util.*;
import java.util.concurrent.Executors;

/**
 * 此类用于爬取传统页面
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public class TradApplication extends AbstractStarter {

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
            OkHttpClientHelper okHttpClientHelper = new OkHttpClientHelper(recorder);
            Page page = okHttpClientHelper.request(crawler);
            if (page == null) {//出错 直接返回
                return;
            }
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
            String code = page.getResponseCode().toString();
            if (code.startsWith("2")) {
                recorder.addSucc(page);
            } else {
                recorder.addErr(page);
            }
            if (page.getDepth().equals(config.getDepth())) {//当前爬虫深度已经达到设置的级别，子爬虫不再爬取
                return;
            }
            //过滤本次提取出来的URL
            filter.filter(page);
            for (Crawler crawler : page.getCrawlers()) {
                recorder.add(crawler);//记录没有爬取的
            }
        }
    }

}
