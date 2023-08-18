package top.jiangqiang.crawler.newcore.entities;

import okhttp3.Call;
import okhttp3.Response;

import java.util.List;

public interface Starter {
    /**
     * 获取种子，可以自行实现，但是当没有种子可以获取时，必须遵守如下约定：
     * 1. 阻塞，直到可以获取到一个种子为止。
     * 2. 立即返回一个空的List集合，此时他会一直反复调用此接口。
     * 3. 立即返回一个null，表示不再有新的种子，当前线程池中的任务执行完毕后结束此程序。除非想实现爬取任务完成后退出程序之类的功能，否则不应该返回null。
     *
     * @return
     */
    List<BaseCrawler> obtainCrawler();

    Call request(BaseCrawler crawler);

    void response(BaseCrawler baseCrawler, Response response);

    void failure(BaseCrawler crawler, Exception ioException);

    void start();
}
