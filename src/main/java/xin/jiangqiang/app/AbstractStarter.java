package xin.jiangqiang.app;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import xin.jiangqiang.config.Config;
import xin.jiangqiang.entities.Crawler;
import xin.jiangqiang.filter.Filter;
import xin.jiangqiang.filter.NextFilter;
import xin.jiangqiang.management.Record;
import xin.jiangqiang.management.RecordImpl;
import xin.jiangqiang.reflect.CallMethodHelper;
import xin.jiangqiang.util.FileUtil;
import xin.jiangqiang.util.StringUtil;

import java.io.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
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
    protected Config config;
    protected Crawler crawler = new Crawler();
    protected CallMethodHelper callMethodHelper;
    protected ExecutorService executor;
    protected Filter filter = new NextFilter();
    protected Record record = new RecordImpl();
    //所有任务是否执行完毕
    protected Boolean isEnd = false;
    //结束前保存
    protected static List<Crawler> crawlers = Collections.synchronizedList(new LinkedList<>());
    //初始时读取
    protected static List<Crawler> initCrawlers = Collections.synchronizedList(new LinkedList<>());
    /**
     * 当前活动线程数可能不准确,因此判断连续几秒内活动线程相等为准
     */
    protected Integer[] activeCounts = new Integer[3];

    public AbstractStarter() {
        config = new Config(getClass());
    }

    protected void init() {
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
        callMethodHelper = new CallMethodHelper(config);
    }

    protected void beforeEnd() {
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
            clearResource();
            log.info("程序结束了");
        }));
    }

    public final void start() {
        init();
        callMethodHelper.before();//初始化完成后执行before方法
        run();
        close();
    }

    /**
     * 爬虫任务执行完成
     */
    private void close() {
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
                log.info(e.getMessage());
            }
        }
        callMethodHelper.after();
    }

    public void clearResource() {
    }

    abstract void run();
}
