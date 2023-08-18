package top.jiangqiang.crawler.newcore.entities;

import lombok.Getter;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

import java.util.concurrent.ThreadPoolExecutor;
@Getter
public abstract class SStarter implements Starter {
    protected final OkHttpClient okHttpClient;

    protected final ThreadPoolExecutor executorService;

    public SStarter() {
        this.okHttpClient = new OkHttpClient.Builder().build();
        this.executorService = (ThreadPoolExecutor) this.okHttpClient.dispatcher().executorService();
    }

    public SStarter(ThreadPoolExecutor threadPoolExecutor) {
        this.executorService = threadPoolExecutor;
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        // 创建Dispatcher并设置自定义线程池
        builder.setDispatcher$okhttp(new Dispatcher(threadPoolExecutor));
        this.okHttpClient = builder.build();
    }
}
