package xin.jiangqiang.crawler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import xin.jiangqiang.config.Config;
import xin.jiangqiang.entities.Crawler;
import xin.jiangqiang.entities.Next;
import xin.jiangqiang.net.OkHttpClientHelper;
import xin.jiangqiang.reflect.CallMethodHelper;
import xin.jiangqiang.entities.Page;

import java.io.IOException;
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
    /**
     * 当前活动线程数可能不准确,因此判断连续几秒内活动线程相等为准
     */
    private Integer[] activeCounts = new Integer[3];

    public void start() throws IOException {
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
            OkHttpClientHelper okHttpClientHelper = new OkHttpClientHelper(config);
            if (crawler.getDepth() <= config.getDepth()) {
                try {
                    Next next = new Next();
                    Page page = okHttpClientHelper.request(crawler);
                    for (String regEx : config.getRegExs()) {
                        Pattern pattern = Pattern.compile(regEx);
                        Matcher matcher = pattern.matcher(page.getHtml());
                        StringBuilder buffer = new StringBuilder();
                        //ToDo
                        //此处需改进,先提取所有的链接,然后再根据配置的正则表达式获取对应的URL,而不是直接根据正则表达式获取URL
                        while (matcher.find()) {
                            buffer.append(matcher.group());
                            buffer.append("\r\n");
                        }
                        if ("".equals(buffer.toString().trim())) {
                            log.info("提取到的URL:" + buffer.toString());
                            next.addSeed(buffer.toString());
                        }
                    }
                    page.setType(crawler.getType());
                    callMethodHelper.match(page, next);
                    callMethodHelper.deal(page, next);
                    next.setDepth(crawler.getDepth() + 1);
                    for (Crawler tmpCrawler : next.getCrawlers()) {
                        tmpCrawler.setDepth(next.getDepth());
                        Runnable runnable = new Task(tmpCrawler);
                        executor.execute(runnable);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
