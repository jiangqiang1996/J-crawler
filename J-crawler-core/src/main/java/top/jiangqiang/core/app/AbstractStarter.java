package top.jiangqiang.core.app;

import cn.hutool.core.util.ReUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import top.jiangqiang.core.base.BaseException;
import top.jiangqiang.core.entities.Crawler;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Author: JiangQiang
 * @Date: 2022年05月14日 09:46
 */
@Slf4j
@Data
public abstract class AbstractStarter implements Starter {
    /**
     * 线程池
     */
    private static volatile ExecutorService executor;

    public final ExecutorService getExecutor() {
        //双重检测，为了减少竞争锁的次数
        if (executor == null) {
            synchronized (this) {
                if (executor == null) {
                    executor = initExecutorService();
                }
            }
        }
        return executor;
    }

    /**
     * 程序启动入口
     */
    public final void start() {

        /**
         * 注册一个线程维护程序，用于程序结束时清理资源和持久化保存。
         * 程序结束前执行的线程逻辑
         * 程序正常退出前执行，用于多线程处理后清理资源以及保存任务
         */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("程序准备结束");
            getRecorder().saveBeforeEnd();
            log.info("程序结束了");
        }));
        /**
         * 配置身份验证信息
         * 配置初始爬取地址，或者从持久化数据中读取爬取地址，实现续爬功能
         */
        init(getRecorder());
        getRecorder().initBeforeStart();
        if (getRecorder().count() == 0) {
            throw new BaseException("没有爬取任务，任务结束");
        }
        /**
         * 爬取before方法中设置的爬虫，分配线程爬取recorder中的实例，启动爬虫任务
         */
        run();
        /**
         * 爬虫任务执行完成后执行关闭线程的操作
         */
        finish();
    }

    /**
     * 爬虫任务执行完成
     */
    private void finish() {

    }

    /**
     * 获取一个线程业务逻辑
     *
     * @param crawler 爬虫种子
     * @return Runnable
     */
    public abstract Runnable getTask(Crawler crawler);

    private void run() {
        new Thread(() -> {
            while (true) {
                Crawler crawler = getRecorder().popOne();
                if (crawler != null) {
                    getRecorder().addActive(crawler);
                    getExecutor().execute(getTask(crawler));
                } else {
                    log.info("没有获取到种子");
                    crawler = getRecorder().popOne();
                    if (crawler != null) {
                        getRecorder().addActive(crawler);
                        getExecutor().execute(getTask(crawler));
                    } else {
                        executor.shutdown();
                        try {
                            boolean b = executor.awaitTermination(2, TimeUnit.SECONDS);
                            if (b) {
                                log.info("没有获取到种子，程序即将结束");
                                break;
                            }
                        } catch (InterruptedException e) {
                            log.debug(e.getMessage());
                        }
                    }
                }
            }
        }).start();
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
        List<String> regExs = getGlobalConfig().getRegExs();
        List<String> reverseRegExs = getGlobalConfig().getReverseRegExs();
        List<String> defaultReverseRegExs = getGlobalConfig().getIsUseDefault() ? getGlobalConfig().getDefaultReverseRegExs() : new ArrayList<>();
        List<String> tmpUrls = new ArrayList<>();
        Set<String> sets = new HashSet<>(urls);//去重
        Iterator<String> interator = sets.iterator();
        OUT:
        while (interator.hasNext()) {
            String url = interator.next();

            for (String defaultReverseRegEx : defaultReverseRegExs) {
                if (ReUtil.isMatch(defaultReverseRegEx, url)) {
                    continue OUT;
                }
            }

            for (String reverseRegEx : reverseRegExs) {
                if (ReUtil.isMatch(reverseRegEx, url)) {
                    continue OUT;
                }
            }
            for (String regEx : regExs) {
                if (ReUtil.isMatch(regEx, url)) {
                    tmpUrls.add(url);
                    continue OUT;
                }
            }
        }
        return tmpUrls;
    }
}
