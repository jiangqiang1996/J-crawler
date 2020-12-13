package xin.jiangqiang.crawler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import xin.jiangqiang.config.Config;
import xin.jiangqiang.entities.Crawler;
import xin.jiangqiang.entities.Next;
import xin.jiangqiang.filter.Filter;
import xin.jiangqiang.filter.NextFilter;
import xin.jiangqiang.management.Record;
import xin.jiangqiang.management.RecordImpl;
import xin.jiangqiang.net.OkHttpClientHelper;
import xin.jiangqiang.reflect.CallMethodHelper;
import xin.jiangqiang.entities.Page;
import xin.jiangqiang.util.DocumentUtil;
import xin.jiangqiang.util.RegExpUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Data
public class RAMCrawler {
    Config config;
    Crawler crawler;
    CallMethodHelper callMethodHelper;
    ExecutorService executor;
    Filter filter = new NextFilter();
    Record record = new RecordImpl();
    /**
     * 当前活动线程数可能不准确,因此判断连续几秒内活动线程相等为准
     */
    private Integer[] activeCounts = new Integer[3];

    public final void start() throws IOException {
        callMethodHelper = new CallMethodHelper(config);
        callMethodHelper.before();
        executor = Executors.newFixedThreadPool(config.getThreads());
        for (Crawler tmpCrawler : crawler.getCrawlers()) {
            Runnable runnable = new Task(tmpCrawler);
            executor.execute(runnable);
        }
//        executor.shutdown();
        while (true) {
            try {
                activeCounts[0] = ((ThreadPoolExecutor) executor).getActiveCount();
                Thread.sleep(1000);
                activeCounts[1] = ((ThreadPoolExecutor) executor).getActiveCount();
                Thread.sleep(1000);
                activeCounts[2] = ((ThreadPoolExecutor) executor).getActiveCount();
                if (executor.isTerminated()) {
                    log.info("所有线程执行完毕");
                    break;
                }
                if (activeCounts[0] == 0 && activeCounts[1] == 0 && activeCounts[2] == 0) {
                    executor.shutdown();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        callMethodHelper.after();
    }

    @AllArgsConstructor
    @Data
    protected class Task implements Runnable {
        Crawler crawler;

        @Override
        public void run() {
            OkHttpClientHelper okHttpClientHelper = new OkHttpClientHelper(config, record);
            if (crawler.getDepth() <= config.getDepth()) {
                Next next = new Next();
                Page page = okHttpClientHelper.request(crawler);
                //如果正正则或反正则列表有一个有值，就会抽取所有URL
                if (config.getRegExs().size() != 0 || config.getReverseRegExs().size() != 0) {
                    //此处用于抓取所有URL
                    List<String> urls = DocumentUtil.getAllUrl(page.getHtml());
                    //使用正则表达式筛选URL
                    urls = getMatchUrls(urls);
                    next.addSeeds(urls, "");
                }
                //此处page是发送请求后新创建的，没有type的值，需要手动赋值
                page.setType(crawler.getType());
                callMethodHelper.match(page, next);
                callMethodHelper.deal(page, next);
                filter.filter(next, page);
                next.setDepth(crawler.getDepth() + 1);
                for (Crawler tmpCrawler : next.getCrawlers()) {
                    tmpCrawler.setDepth(next.getDepth());
                    Runnable runnable = new Task(tmpCrawler);
                    executor.execute(runnable);
                }
            }
        }
    }

    /**
     * 逆正则优先级更高
     * 一旦匹配逆正则，则不会匹配正正则
     *
     * @param urls 链接
     * @return
     */
    private List<String> getMatchUrls(List<String> urls) {
        if (urls == null || urls.size() == 0) {
            return new ArrayList<>();
        }
        List<String> regExs = config.getRegExs();
        List<String> reverseRegExs = config.getReverseRegExs();
        List<String> defaultReverseRegExs = config.getIsUseDefault() ? config.getDefaultReverseRegExs() : new ArrayList<>();
        List<String> tmpUrls = new ArrayList<>();
        Iterator<String> interator = urls.iterator();
        OUT:
        while (interator.hasNext()) {
            String url = interator.next();

            for (String defaultReverseRegEx : defaultReverseRegExs) {
//                Pattern pattern = Pattern.compile(defaultReverseRegEx);
//                Matcher matcher = pattern.matcher(url);
//                if (matcher.matches()) {
//                    //匹配默认正则
//                    continue OUT;
//                }
                if (RegExpUtil.isMatch(url, defaultReverseRegEx)) {
                    continue OUT;
                }
            }

            for (String reverseRegEx : reverseRegExs) {
//                Pattern pattern = Pattern.compile(reverseRegEx);
//                Matcher matcher = pattern.matcher(url);
//                if (matcher.matches()) {
////                    interator.remove();
//                    //如果匹配逆正则，则删除该URL，结束本次循环
//                    continue OUT;
//                }
                if (RegExpUtil.isMatch(url, reverseRegEx)) {
                    continue OUT;
                }
            }
            for (String regEx : regExs) {
//                Pattern pattern = Pattern.compile(regEx);
//                Matcher matcher = pattern.matcher(url);
//                if (matcher.matches()) {
//                    tmpUrls.add(url);
//                    continue OUT;
//                }
                if (RegExpUtil.isMatch(url, regEx)) {
                    tmpUrls.add(url);
                    continue OUT;
                }
            }
        }
        return tmpUrls;
    }
}
