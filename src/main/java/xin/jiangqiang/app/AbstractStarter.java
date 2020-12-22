package xin.jiangqiang.app;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import xin.jiangqiang.config.Config;
import xin.jiangqiang.entities.Crawler;
import xin.jiangqiang.filter.Filter;
import xin.jiangqiang.filter.NextFilter;
import xin.jiangqiang.manage.RAMRecorder;
import xin.jiangqiang.manage.Recorder;
import xin.jiangqiang.reflect.CallMethodHelper;
import xin.jiangqiang.util.RegExpUtil;
import xin.jiangqiang.util.StringUtil;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 定义开始爬取前，爬取完成后的基本流程
 *
 * @author jiangqiang
 * @date 2020/12/17 20:17
 */
@Slf4j
@Data
public abstract class AbstractStarter implements Starter {

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    protected final Integer[] activeCounts = new Integer[3];//当前活动线程数可能不准确,因此判断连续几秒内活动线程相等为准
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    protected CallMethodHelper callMethodHelper;//调用反射方法的工具
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    protected Boolean isEnd = false;//所有任务是否执行完毕,好像暂时没用上
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    protected ExecutorService executor;//线程池

    @Setter(AccessLevel.NONE)
    protected final Config config;//不可修改config,但是可以修改config的属性值

    @Setter(AccessLevel.NONE)
    protected final Crawler crawler = new Crawler();//不可修改爬虫对象,但是可以新增他的子类爬虫实例

    protected Filter filter = new NextFilter();//可以在main方法设置外部自定义过滤器
    protected Recorder recorder = new RAMRecorder();//可以在main方法设置外部自定义过滤器

    public AbstractStarter() {
        config = new Config(getClass());
    }

    public final void start() {
        init();
        callMethodHelper.before();//初始化完成后执行before方法@Before注解的方法
        run();//爬取main方法中设置的爬虫
        dispatcher();//分配线程爬取recorder中的实例
        finish();//爬虫任务执行完成后执行关闭线程的操作
    }

    protected void init() {
        callMethodHelper = new CallMethodHelper(config);//构造反射工具类对象
        //注册程序结束前执行的线程逻辑
        beforeEnd();
        recorder.initBeforeStart(config);//断点续传功能
    }

    //程序结束前执行的线程逻辑
    protected void beforeEnd() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("程序准备结束");
            recorder.saveBeforeEnd(config);
            clearResource();
            log.info("程序结束了");
        }));
    }

    /**
     * 程序结束前会执行,用于清理资源,比如使用selenium时关闭驱动
     */
    public void clearResource() {
    }

    /**
     * 使用线程池执行爬虫任务的真正逻辑
     */
    protected void run() {
        executor = Executors.newFixedThreadPool(config.getThreads());
        for (Crawler tmpCrawler : crawler.getCrawlers()) {//初始爬虫
            Runnable runnable = getTask(tmpCrawler);
            executor.execute(runnable);
        }
    }

    /**
     * 从Recorder分配任务
     */
    public void dispatcher() {
        new Thread(() -> {//使用一个线程专门分配爬虫任务
            while (true) {
                if (isEnd) {//线程池内所有线程结束了
                    break;
                } else {
                    Crawler crawler = recorder.getOne();
                    if (crawler == null) {//如果为空,则释放资源,等待爬虫线程解析到新的子爬虫
                        Thread.yield();//释放CPU资源
                        continue;
                    }
                    Runnable runnable = getTask(crawler);
                    executor.execute(runnable);
                }
            }
        }).start();
    }

    public abstract Runnable getTask(Crawler crawler);

    /**
     * 爬虫任务执行完成
     */
    private void finish() {
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
                if (activeCounts[0] == 0 && activeCounts[1] == 0 && activeCounts[2] == 0) {//连续三秒活动线程为0
                    executor.shutdown();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.info(e.getMessage());
            }
        }
        callMethodHelper.after();
    }


    /**
     * 逆正则优先级更高
     * 一旦匹配逆正则，则不会匹配正正则
     *
     * @param urls 链接
     * @return
     */
    protected List<String> getMatchUrls(List<String> urls) {
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

    /**
     * 不建议获取初始爬虫对象，如果需要添加爬虫种子，可以直接调用addSeed
     *
     * @return 返回初始爬虫对象
     */
    @Deprecated
    public Crawler getCrawler() {
        return crawler;
    }

    /**
     * 创建时子类爬虫深度会自动+1
     *
     * @param url url
     * @return 返回子爬虫
     */
    public Crawler addSeed(String url) {
        return crawler.addSeed(url);
    }

    /**
     * 批量创建初始爬虫，批量设置子类类型
     *
     * @param urls url列表
     * @param type 类型
     */
    public void addSeeds(List<String> urls, String type) {
        crawler.addSeeds(urls, type);
    }
}
