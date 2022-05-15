package xin.jiangqiang.core.app;

import cn.hutool.core.lang.Singleton;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import xin.jiangqiang.common.RegExpUtil;
import xin.jiangqiang.core.config.Config;
import xin.jiangqiang.core.entities.Crawler;
import xin.jiangqiang.core.filter.Filter;
import xin.jiangqiang.core.recoder.Recorder;
import xin.jiangqiang.reflect.CallMethodHelper;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author: JiangQiang
 * @Date: 2022年05月14日 09:46
 */
@Slf4j
public abstract class AbstractStarter implements Starter {
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    protected final Integer[] activeCounts = new Integer[3];//当前活动线程数可能不准确,因此判断连续几秒内活动线程相等为准
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    protected ExecutorService executor;//线程池
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    protected Boolean isEnd = false;//所有任务是否执行完毕,好像暂时没用上
    protected final CallMethodHelper callMethodHelper = Singleton.get(CallMethodHelper.class);//调用反射方法的工具
    protected Recorder recorder;
    protected Filter filter;
    private final Config config = Singleton.get(Config.class);

    public AbstractStarter() {
        this.filter = Singleton.get(config.getFilterClass());
        this.recorder = Singleton.get(config.getRecorderClass());
    }

    /**
     * 程序启动入口
     */
    public final void start(Class<? extends Starter> clazz) {
        config.setAppClass(clazz);
        start();
    }

    public final void start() {
        if (ObjectUtil.isNull(config.getAppClass())) {
            throw new RuntimeException("config.appClass不能为空");
        }
        beforeEnd();//注册一个线程维护程序，用于程序结束时清理资源和持久化保存。
        callMethodHelper.before();//配置初始爬取地址，或者从持久化数据中读取爬取地址，实现续爬功能
        run();//爬取before方法中设置的爬虫，分配线程爬取recorder中的实例，启动爬虫任务
        finish();//爬虫任务执行完成后执行关闭线程的操作

    }

    /**
     * 爬虫任务执行完成
     */
    private void finish() {
        while (true) {
            activeCounts[0] = ((ThreadPoolExecutor) executor).getActiveCount();
            ThreadUtil.safeSleep(1000);
            activeCounts[1] = ((ThreadPoolExecutor) executor).getActiveCount();
            ThreadUtil.safeSleep(1000);
            activeCounts[2] = ((ThreadPoolExecutor) executor).getActiveCount();
            if (executor.isTerminated()) {
                log.info("所有线程执行完毕");
                isEnd = true;
                break;
            }
            if (activeCounts[0] == 0 && activeCounts[1] == 0 && activeCounts[2] == 0) {//连续三秒活动线程为0
                log.info("线程池清理完毕");
                executor.shutdown();
            }
        }
        callMethodHelper.after();//爬虫任务完成时执行

    }


    public abstract Runnable getTask(Crawler crawler);

    private void run() {
        executor = ThreadUtil.newExecutor(0, config.getThreads(), 1000);
        new Thread(() -> {
            while (true) {
                Crawler crawler = recorder.getOne();
                if (crawler != null) {//如果为空,则释放资源,等待爬虫线程解析到新的子爬虫
                    executor.execute(getTask(crawler));
                    continue;
                }
                if (isEnd) {
                    break;
                }
            }
        }).start();
    }

    /**
     * 程序结束前会执行,此方法用于保存内存中的数据到本地，并且清理内存资源
     */
    public void saveAndClearResource() {

    }

    //程序结束前执行的线程逻辑
    private void beforeEnd() {//程序正常退出前执行，用于多线程处理后清理资源以及保存任务
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("程序准备结束");
            saveAndClearResource();
            log.info("程序结束了");
        }));
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
        Set<String> sets = new HashSet<>(urls);//去重
        Iterator<String> interator = sets.iterator();
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
}
