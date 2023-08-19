package top.jiangqiang.crawler.app;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jetbrains.annotations.NotNull;
import top.jiangqiang.crawler.config.CrawlerGlobalConfig;
import top.jiangqiang.crawler.config.ProxyAuthenticatorConfig;
import top.jiangqiang.crawler.entities.BaseCrawler;
import top.jiangqiang.crawler.entities.RequestEntity;
import top.jiangqiang.crawler.log.HttpLogger;

import java.io.IOException;
import java.net.ProxySelector;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Getter
@Slf4j
public abstract class AbstractStarter<T extends BaseCrawler> implements Starter<T> {
    protected final OkHttpClient okHttpClient;
    //okhttp内部使用的线程池
    protected final ThreadPoolExecutor executorService;
    protected final CrawlerGlobalConfig config;

    public AbstractStarter() {
        this(null);
    }

    public AbstractStarter(CrawlerGlobalConfig config) {
        if (config == null) {
            config = new CrawlerGlobalConfig();
        }
        HttpLoggingInterceptor.Level logLevel = config.getLogLevel();
        if (logLevel == null) {
            logLevel = HttpLoggingInterceptor.Level.BASIC;
        }
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addNetworkInterceptor(new HttpLoggingInterceptor(new HttpLogger()).setLevel(logLevel));
        //没有自定义线程池
        if (config.getThreadPoolExecutor() != null) {
            // 创建Dispatcher并设置自定义线程池
            builder.setDispatcher$okhttp(new Dispatcher(config.getThreadPoolExecutor()));
        }

        ProxySelector proxySelector = getProxySelector();
        if (proxySelector != null) {
            builder.setProxySelector$okhttp(proxySelector);
        }
        ProxyAuthenticatorConfig proxyAuthenticatorConfig = config.getProxyAuthenticatorConfig();
        if (proxyAuthenticatorConfig != null) {
            String username = proxyAuthenticatorConfig.getUsername();
            String password = proxyAuthenticatorConfig.getPassword();
            if (StrUtil.isBlank(username) && StrUtil.isBlank(password)) {
                builder.setProxyAuthenticator$okhttp((route, response) -> {
                    //设置代理服务器账号密码
                    String credential = Credentials.basic(username, password);
                    return response.request().newBuilder().header("Proxy-Authorization", credential).build();
                });
            }
        }
        this.okHttpClient = builder.build();
        this.executorService = (ThreadPoolExecutor) this.okHttpClient.dispatcher().executorService();
        this.config = config;
    }

    @Override
    public Call doRequest(OkHttpClient client, BaseCrawler crawler) {
        return client.newCall(new RequestEntity(crawler.getUrl()).buildRequest());
    }

    @Override
    public final void start() {
        while (true) {
            //1.线程池中没有正在执行的任务并且队列中也没有任务 2.存在空闲线程
            if ((executorService.getQueue().isEmpty() && executorService.getActiveCount() == 0) || executorService.getActiveCount() < executorService.getCorePoolSize()) {
                List<T> crawlers = getCrawlers();
                if (CollUtil.isNotEmpty(crawlers)) {
                    for (T crawler : crawlers) {
                        //发送请求
                        Call call = doRequest(okHttpClient, crawler);
                        call.enqueue(new Callback() {
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException ioException) {
                                try {
                                    log.info(ExceptionUtil.stacktraceToString(ioException));
                                    doFailure(crawler, ioException);
                                } catch (Exception exception) {
                                    log.info(exception.getMessage());
                                }
                            }

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) {
                                try {
                                    doResponse(crawler, response);
                                } catch (Exception exception) {
                                    log.info(exception.getMessage());
                                } finally {
                                    IoUtil.close(response);
                                }
                            }
                        });
                        if (config.getDelayTime() != null) {
                            Long delayTime = config.getDelayTime().get();
                            if (delayTime != null && delayTime > 0) {
                                try {
                                    Thread.sleep(delayTime);
                                } catch (InterruptedException e) {
                                    log.debug(e.getMessage());
                                }
                            }
                        }
                    }

                }
                //线程池中没有正在执行的任务并且队列中也没有任务
                if (executorService.getQueue().isEmpty() && executorService.getActiveCount() == 0 && crawlers == null) {
                    try {
                        Thread.sleep(1000L);
                        if (executorService.getQueue().isEmpty() && executorService.getActiveCount() == 0) {
                            //退出程序
                            executorService.shutdown();
                            break;
                        }
                    } catch (InterruptedException e) {
                        log.debug(e.getMessage());
                    }
                }
            }
        }
    }
}
