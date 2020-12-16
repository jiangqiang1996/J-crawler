package xin.jiangqiang.app;

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
import xin.jiangqiang.util.FileUtil;
import xin.jiangqiang.util.RegExpUtil;
import xin.jiangqiang.util.StringUtil;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Data
public class Application {
    Config config;
    Crawler crawler = new Crawler();
    CallMethodHelper callMethodHelper;
    ExecutorService executor;
    Filter filter = new NextFilter();
    Record record = new RecordImpl();
    //所有任务是否执行完毕
    Boolean isEnd = false;
    //结束前保存
    private static List<Crawler> crawlers = Collections.synchronizedList(new LinkedList<>());
    //初始时读取
    private static List<Crawler> initCrawlers = Collections.synchronizedList(new LinkedList<>());

    public Application() {
        config = new Config(getClass());
    }

    /**
     * 当前活动线程数可能不准确,因此判断连续几秒内活动线程相等为准
     */
    private Integer[] activeCounts = new Integer[3];

    public final void start() {
        init();
        callMethodHelper = new CallMethodHelper(config);
        callMethodHelper.before();
        executor = Executors.newFixedThreadPool(config.getThreads());
        for (Crawler crawler : initCrawlers) {
            Runnable runnable = new Task(crawler);
            executor.execute(runnable);
        }
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
                    isEnd = true;
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

    @Data
    protected class Task implements Runnable {
        Crawler crawler;

        public Task(Crawler crawler) {
            this.crawler = crawler;
            crawlers.add(crawler);
        }

        @Override
        public void run() {
            OkHttpClientHelper okHttpClientHelper = new OkHttpClientHelper(config, record);
            if (crawler.getDepth() <= config.getDepth()) {
                Next next = new Next();
                Page page = okHttpClientHelper.request(crawler);
                if (page == null) {//出错 直接返回
                    crawlers.remove(crawler);
                    return;
                }
                //如果正正则或反正则列表有一个有值，就会抽取所有URL
                if (config.getRegExs().size() != 0 || config.getReverseRegExs().size() != 0) {
                    //此处用于抓取所有URL
                    List<String> urls = DocumentUtil.getAllUrl(page.getHtml());
                    //使用正则表达式筛选URL
                    urls = getMatchUrls(urls);
                    next.addSeeds(urls, "");
                }
                //获取元数据,深度,类型
                next.initDataFromCrawler(page);
                next.setDepth(crawler.getDepth() + 1);

                callMethodHelper.match(page, next);
                callMethodHelper.deal(page, next);
                //处理完成，加入成功结果集
                String code = page.getResponseCode().toString();
                if (code.startsWith("4") || code.startsWith("5")) {
                    record.addErr(page.getUrl());
                } else {
                    record.addSucc(page.getUrl());
                }
                crawlers.remove(crawler);
                //过滤下次爬取的URL
                filter.filter(next, page);
                for (Crawler tmpCrawler : next.getCrawlers()) {
                    tmpCrawler.initDataFromCrawler(next);
                    Runnable runnable = new Task(tmpCrawler);
                    executor.execute(runnable);
                }
            } else {
                crawlers.remove(crawler);
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
                if (RegExpUtil.isMatch(url, defaultReverseRegEx)) {
                    continue OUT;
                }
            }

            for (String reverseRegEx : reverseRegExs) {
                if (RegExpUtil.isMatch(url, reverseRegEx)) {
                    continue OUT;
                }
            }
            for (String regEx : regExs) {
                if (RegExpUtil.isMatch(url, regEx)) {
                    tmpUrls.add(url);
                    continue OUT;
                }
            }
        }
        return tmpUrls;
    }

    private void init() {
        //注册结束前执行的线程逻辑
        beforeEnd();
        //保存路径不为空，则读取
        if (config.getIsContinue() && StringUtil.isNotEmpty(config.getSavePath())) {
            File f = new File(config.getSavePath());
            try (
                    //创建对象输入流
                    FileInputStream fis = new FileInputStream(f);
                    ObjectInputStream ois = new ObjectInputStream(fis);
            ) {
                initCrawlers = (List<Crawler>) ois.readObject();
                log.debug("从文件获取的爬虫种子:\n" + initCrawlers.toString());
            } catch (IOException | ClassNotFoundException e) {
                initCrawlers = Collections.synchronizedList(new LinkedList<>());
                //路径设置后是保存时才创建，所以会爆找不到指定路径
                log.error(e.getMessage());
            }
        }
    }

    private void beforeEnd() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("程序准备结束");
            //保存路径不为空，则保存
            if (crawlers.size() != 0 && StringUtil.isNotEmpty(config.getSavePath())) {
                FileUtil.mkParentDirIfNot(config.getSavePath());
                File f = new File(config.getSavePath());
                try (
                        //创建对象输出流
                        FileOutputStream fos = new FileOutputStream(f);
                        ObjectOutputStream oos = new ObjectOutputStream(fos);
                ) {
                    oos.writeObject(crawlers);
                    log.info(crawlers.toString());
                    log.info("保存爬取状态成功");
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
            log.info("程序结束了");
        }));
    }
}
