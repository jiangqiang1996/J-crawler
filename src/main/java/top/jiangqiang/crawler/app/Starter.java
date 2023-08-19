package top.jiangqiang.crawler.app;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import top.jiangqiang.crawler.entities.BaseCrawler;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public interface Starter<T extends BaseCrawler> {
    /**
     * 获取种子，可以自行实现从数据库中获取，但是当没有种子可以获取时，必须遵守如下约定：
     * 1. 阻塞，直到可以获取到一个种子为止。
     * 2. 立即返回一个空的List集合，此时他会一直反复调用此接口。
     * 3. 立即返回一个null，表示不再有新的种子，当前线程池中的任务执行完毕后结束此程序。除非想实现爬取任务完成后退出程序之类的功能，否则不应该返回null。
     * <p>
     * 种子取出后，可以在数据库中删除。
     * ·
     *
     * @return
     */
    List<T> getCrawlers();

    /**
     * 种子正在爬取
     *
     * @param okHttpClient
     * @param crawler
     * @return
     */
    Call doRequest(OkHttpClient okHttpClient, T crawler);

    /**
     * 请求成功，可以根据具体的响应码判断是否成功或失败，自行在数据库中进行存取
     *
     * @param crawler
     * @param response
     */
    void doResponse(T crawler, Response response);

    /**
     * 请求失败，网络问题的失败
     *
     * @param crawler
     * @param ioException
     */
    void doFailure(T crawler, Exception ioException);

    /**
     * 启动程序
     */
    void start();

    /**
     * 根据URL定义代理池
     *
     * @return
     */
    default ProxySelector getProxySelector() {
        return new ProxySelector() {
            /**
             * 每次发送请求就会执行此方法，获取到一个代理池
             * @param uri 发请求的地址
             * @return 返回一个可以执行该URI的代理池
             */
            @Override
            public List<Proxy> select(URI uri) {
                //代理池
                List<Proxy> proxyList = new ArrayList<>();
                //请求的URL
                String host = uri.getHost();
                // 根据请求的URL选择不同的代理
//                if (host.equals("example.com")) {
//                    String proxyHost = "proxy1.example.com";
//                    int proxyPort = 8080;
//                    Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
//                    proxyList.add(proxy);
//                } else if (host.startsWith("example.com")) {
//                    String proxyHost = "proxy1.example.com";
//                    int proxyPort = 8080;
//                    Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
//                    proxyList.add(proxy);
//                }
                // 如果没有匹配到特定的代理，则使用默认代理（没有代理）
                if (proxyList.isEmpty()) {
                    Proxy noProxy = Proxy.NO_PROXY;
                    proxyList.add(noProxy);
                }
                return proxyList;
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                // 处理连接失败的情况
            }
        };
    }
}
