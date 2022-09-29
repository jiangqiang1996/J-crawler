package top.jiangqiang.sample.jiangqiang.core.recoder;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import top.jiangqiang.sample.jiangqiang.core.entities.Crawler;
import lombok.extern.slf4j.Slf4j;
import top.jiangqiang.util.JSONUtil;

import java.io.File;
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
    private final static Set<Crawler> crawlersSet = new ConcurrentHashSet<>();
    /**
     * 正在爬取中的URL，也可能是已经加入任务正在等待的任务，如果结束时需要保存，那么此集合与crawlersSet中的种子都会进行保存
     */
    private final static Set<Crawler> tmpCrawlersSet = new ConcurrentHashSet<>();

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
    private final static Map<String, Crawler> crawlerMap = Collections.synchronizedMap(new HashMap<>());

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
        if ((crawlersSet.size() != 0 || tmpCrawlersSet.size() != 0) && StrUtil.isNotEmpty(config.getSavePath())) {
            log.info("开始保存未爬取的爬虫");
            FileUtil.mkParentDirs(config.getSavePath());
            File file = new File(config.getSavePath());
            Set<Crawler> allCrawlers = new HashSet<>();
            allCrawlers.addAll(crawlersSet);
            allCrawlers.addAll(tmpCrawlersSet);
            String jsonStr = JSONUtil.toJsonPrettyStr(allCrawlers);
            IoUtil.write(FileUtil.getOutputStream(file), true, jsonStr.getBytes());
            log.info("保存爬取状态成功");
        }
    }

    /**
     * 初始化内存记录器
     * 写集合crawlersList
     */
    @Override
    public synchronized void initBeforeStart() {
        //保存路径不为空，config的isContinue属性为true,则读取
        if (config.getIsContinue() && StrUtil.isNotEmpty(config.getSavePath())) {
            File file = new File(config.getSavePath());
            if (file.exists()) {
                String jsonStr = IoUtil.read(FileUtil.getReader(file, config.getCharset()), true);
                Set<Crawler> crawlers = JSONUtil.parse(jsonStr, new HashSet<>());
                this.addAll(new ArrayList<>(crawlers));
                log.debug("从文件获取的爬虫种子:\n" + crawlers);
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
