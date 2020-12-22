package xin.jiangqiang.manage;

import lombok.extern.slf4j.Slf4j;
import xin.jiangqiang.config.Config;
import xin.jiangqiang.entities.Crawler;
import xin.jiangqiang.util.FileUtil;
import xin.jiangqiang.util.StringUtil;

import java.io.*;
import java.util.*;

/**
 * 下面方法是目前框架使用到的方法
 */
@Slf4j
public class RAMRecorder extends AbstractRecorder {
    /**
     * 存储没有爬取的URL
     */
    private final static List<Crawler> crawlersList = Collections.synchronizedList(new ArrayList<>());
    /**
     * 存储爬取成功的URL
     */
    private final static List<Crawler> succCrawlers = Collections.synchronizedList(new ArrayList<>());
    /**
     * 存储爬取失败的URL
     */
    private final static List<Crawler> errCrawlers = Collections.synchronizedList(new ArrayList<>());

    /**
     * @param crawler 需要存储的爬虫
     */
    @Override
    public synchronized void add(Crawler crawler) {
        crawlersList.add(crawler);
    }

    /**
     * 写集合crawlersList
     *
     * @param crawlers 需要存储的爬虫列表
     */
    @Override
    public synchronized void addAll(List<Crawler> crawlers) {
        if (crawlers != null) {
            crawlersList.addAll(crawlers);
        }
    }

    /**
     * 读写集合crawlersList
     * 获取的同时需要删除存储的该爬虫实例
     *
     * @return 获取一个爬虫实例
     */
    @Override
    public synchronized Crawler getOne() {
        if (crawlersList.size() > 0) {
            Crawler crawler = crawlersList.get(0);
            crawlersList.remove(crawler);
            return crawler;
        } else {
            return null;
        }
    }

    /**
     * 写集合succCrawlers
     *
     * @param crawler 成功爬取的爬虫
     */
    @Override
    public synchronized void addSucc(Crawler crawler) {
        succCrawlers.add(crawler);
    }

    /**
     * 写集合errCrawlers
     *
     * @param crawler 爬取失败的爬虫
     */
    @Override
    public synchronized void addErr(Crawler crawler) {
        errCrawlers.add(crawler);
    }

    /**
     * 读集合crawlersList
     *
     * @param config 保存路径,为空不保存
     */
    @Override
    public synchronized void saveBeforeEnd(Config config) {
        //保存路径不为空，则保存
        if (crawlersList.size() != 0 && StringUtil.isNotEmpty(config.getSavePath())) {
            FileUtil.mkParentDirIfNot(config.getSavePath());
            File f = new File(config.getSavePath());
            try (
                    //创建对象输出流
                    FileOutputStream fos = new FileOutputStream(f);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
            ) {
                oos.writeObject(crawlersList);
                log.info(crawlersList.toString());
                log.info("保存爬取状态成功");
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    /**
     * 写集合crawlersList
     *
     * @param config 保存路径
     */
    @Override
    public synchronized void initBeforeStart(Config config) {
        //保存路径不为空，则读取
        if (config.getIsContinue() && StringUtil.isNotEmpty(config.getSavePath())) {
            File f = new File(config.getSavePath());
            try (
                    //创建对象输入流
                    FileInputStream fis = new FileInputStream(f);
                    ObjectInputStream ois = new ObjectInputStream(fis);
            ) {
                List<Crawler> crawlers = (List<Crawler>) ois.readObject();
                this.addAll(crawlers);
                log.debug("从文件获取的爬虫种子:\n" + crawlers.toString());
            } catch (IOException | ClassNotFoundException e) {
                //路径设置后是保存时才创建，所以会爆找不到指定路径
                log.error(e.getMessage());
            }
        }
    }
}
