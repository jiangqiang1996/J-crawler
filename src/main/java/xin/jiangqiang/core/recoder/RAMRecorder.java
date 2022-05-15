package xin.jiangqiang.core.recoder;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import xin.jiangqiang.core.config.Config;
import xin.jiangqiang.core.entities.Crawler;

import java.io.*;
import java.util.*;


/**
 * 内存记录器，存储统计各个阶段的爬虫
 * 此类所有方法同步，是限制并发的主要因素
 */
@Slf4j
public class RAMRecorder extends AbstractRecorder {
    /**
     * 存储没有爬取的URL
     */
    private final static Set<Crawler> crawlersSet = Collections.synchronizedSet(new HashSet<>());
    /**
     * 正在爬取中的URL，如果遇到强行终止程序，只会保存没有爬取的URL，正在爬取中的URL会丢失
     */
    private final static Set<Crawler> tmpCrawlersSet = Collections.synchronizedSet(new HashSet<>());

    /**
     * 存储爬取成功的URL
     */
    private final static List<Crawler> succCrawlers = Collections.synchronizedList(new ArrayList<>());
    /**
     * 存储爬取失败的URL
     */
    private final static List<Crawler> errCrawlers = Collections.synchronizedList(new ArrayList<>());
    /**
     * 存储所有记录
     */
    private final Map<String, Crawler> crawlerMap = Collections.synchronizedMap(new HashMap<>());

    /**
     * 写集合crawlersSet
     * 不能添加重复的爬虫
     *
     * @param crawler 需要存储的爬虫
     */
    @Override

    public synchronized void add(Crawler crawler) {
        if (crawler.getDepth() < config.getDepth()) {
            crawlerMap.put(crawler.getUrl(), crawler);
            crawlersSet.add(crawler);
        }
    }

    /**
     * 写集合crawlersSet
     * 不能添加重复的爬虫
     *
     * @param crawlers 需要存储的爬虫列表
     */
    @Override
    public synchronized void addAll(List<Crawler> crawlers) {
        if (crawlers != null) {
            crawlersSet.addAll(crawlers);
            for (Crawler crawler : crawlers) {
                crawlerMap.put(crawler.getUrl(), crawler);
            }
        }
    }

    /**
     * 读集合crawlersSet
     * 写集合tmpCrawlersSet
     * 获取的同时需要删除存储的该爬虫实例（保证下一个线程不会取到同一个）
     *
     * @return 获取一个爬虫实例
     */
    @Override
    public synchronized Crawler getOne() {
        Crawler crawler = null;
        Iterator<Crawler> iterator = crawlersSet.iterator();
        if (iterator.hasNext()) {
            crawler = iterator.next();
            iterator.remove();
            tmpCrawlersSet.add(crawler);
        }
        return crawler;
    }

    /**
     * 写集合succCrawlers tmpCrawlersSet
     * 成功或失败后移除正在爬取列表中的对应爬虫
     *
     * @param crawler 成功爬取的爬虫
     */
    @Override
    public synchronized void addSucc(Crawler crawler) {
        succCrawlers.add(crawler);
        tmpCrawlersSet.remove(crawler);
    }

    /**
     * 写集合errCrawlers tmpCrawlersSet
     * 成功或失败后移除正在爬取列表中的对应爬虫
     *
     * @param crawler 爬取失败的爬虫
     */
    @Override
    public synchronized void addErr(Crawler crawler) {
        errCrawlers.add(crawler);
        tmpCrawlersSet.remove(crawler);
    }

    @Override
    public synchronized Integer count() {
        return crawlersSet.size();
    }

    @Override
    public synchronized Integer countSucc() {
        return succCrawlers.size();
    }

    @Override
    public synchronized Integer countErr() {
        return errCrawlers.size();
    }

    /**
     * 读集合crawlersList
     */
    @Override
    public synchronized void saveBeforeEnd() {
        //保存路径不为空，则保存
        if (crawlersSet.size() != 0 && StrUtil.isNotEmpty(config.getSavePath())) {
            log.info("开始保存未爬取的爬虫");
            FileUtil.mkParentDirs(config.getSavePath());
            File f = new File(config.getSavePath());
            try (
                    //创建对象输出流
                    FileOutputStream fos = new FileOutputStream(f);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
            ) {
                crawlersSet.addAll(tmpCrawlersSet);//正在爬取的爬虫因为程序意外终止，重置到没有爬取状态
                oos.writeObject(crawlersSet);
                log.info(crawlersSet.toString());
                log.info("保存爬取状态成功");
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    /**
     * 写集合crawlersList
     */
    @Override
    public synchronized void initBeforeStart() {
        //保存路径不为空，config的isContinue属性为true,则读取
        if (config.getIsContinue() && StrUtil.isNotEmpty(config.getSavePath())) {
            File f = new File(config.getSavePath());
            try (
                    //创建对象输入流
                    FileInputStream fis = new FileInputStream(f);
                    ObjectInputStream ois = new ObjectInputStream(fis);
            ) {
                Set<Crawler> crawlers = (Set<Crawler>) ois.readObject();
                this.addAll(new ArrayList<>(crawlers));
                log.debug("从文件获取的爬虫种子:\n" + crawlers.toString());
            } catch (IOException | ClassNotFoundException e) {
                //路径设置后是保存时才创建，所以会爆找不到指定路径
                log.error(e.getMessage());
            }
        }
    }

    @Override
    public List<Crawler> getAll() {
        return super.getAll();
    }

    @Override
    public List<Crawler> getSucc() {
        return super.getSucc();
    }

    @Override
    public List<Crawler> getErr() {
        return super.getErr();
    }

    @Override
    public Boolean exist(Crawler crawler) {
        return ObjectUtil.isNotNull(crawlerMap.get(crawler.getUrl()));
    }

}
