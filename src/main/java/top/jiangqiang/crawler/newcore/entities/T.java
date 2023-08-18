package top.jiangqiang.crawler.newcore.entities;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import top.jiangqiang.crawler.core.base.BaseException;
import top.jiangqiang.crawler.core.util.JSONUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class T {
    public static void main(String[] args) {
        // 创建自定义线程池
        int corePoolSize = 5; // 核心线程数
        int maxPoolSize = 10; // 最大线程数
        long keepAliveTime = 60L; // 线程空闲时间（秒）
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>()
        );
        new SStarter(threadPoolExecutor) {

            AtomicInteger atomicInteger = new AtomicInteger(0);

            @Override
            public List<BaseCrawler> obtainCrawler() {
                if (atomicInteger.get() == 0) {
                    atomicInteger.addAndGet(1);
                    return List.of(new BaseCrawler("https://www.baidu.com"));
                } else {
                    return null;
                }
            }

            @Override
            public Call request(BaseCrawler crawler) {
                return okHttpClient.newCall(processRequestParam(crawler.url, new HashMap<>(), new HashMap<>(), ""));
            }

            @Override
            public void response(BaseCrawler baseCrawler, Response response) {

            }

            @Override
            public void failure(BaseCrawler crawler, Exception ioException) {

            }


            @Override
            public void start() {
                while (true) {
                    if (executorService.getQueue().isEmpty() || executorService.getActiveCount() == 0 || executorService.getActiveCount() < executorService.getCorePoolSize()) {
                        List<BaseCrawler> baseCrawlers = obtainCrawler();
                        if (executorService.getQueue().isEmpty() && executorService.getActiveCount() == 0) {
                            if (baseCrawlers == null) {
                                //退出程序
                                executorService.shutdown();
                                break;
                            }
                        }
                        if (CollUtil.isNotEmpty(baseCrawlers)) {
                            for (BaseCrawler baseCrawler : baseCrawlers) {
                                Call call = request(baseCrawler);
                                call.enqueue(new Callback() {
                                    @Override
                                    public void onFailure(@NotNull Call call, @NotNull IOException ioException) {
                                        try {
                                            log.info(ExceptionUtil.stacktraceToString(ioException));
                                            failure(baseCrawler, ioException);
                                        } catch (Exception exception) {
                                            log.info(exception.getMessage());
                                        }
                                    }

                                    @Override
                                    public void onResponse(@NotNull Call call, @NotNull Response response) {
                                        try {
                                            if (response.code() == 200) {
                                                ResponseBody body = response.body();
                                                System.out.println(body.string());
                                                response(baseCrawler, response);
                                            } else {
                                                //请求出错
                                            }
                                        } catch (Exception exception) {
                                            log.info(exception.getMessage());
                                        } finally {
                                            IoUtil.close(response);
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            }
        }.start();
    }

    public static Request processRequestParam(String url, Map<String, String> lines, Map<String, String> headers, String bodyStr) {
        Request.Builder builder = new Request.Builder();
        if (StrUtil.isBlank(url)) {
            throw new BaseException("请求链接不能为空");
        }
        url = url.trim();
        if ((!url.startsWith("http://")) && (!url.startsWith("https://"))) {
            url = "http://" + url;
        }
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
        String method;
        //请求行中获取请求方式
        if (CollUtil.isEmpty(lines)) {
            method = "GET";
        } else {
            method = lines.get("method");
            if (StrUtil.isBlank(method)) {
                method = "GET";
            } else {
                method = method.toUpperCase();
            }
        }
//        if (CollUtil.isEmpty(headers)) {
//            headers = HttpUtil.headers;
//        }
        if (CollUtil.isNotEmpty(headers)) {
            //添加请求头
            Set<Map.Entry<String, String>> headerEntrySet = headers.entrySet();
            for (Map.Entry<String, String> entry : headerEntrySet) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        switch (method) {
            case "POST":
            case "PUT":
            case "PATCH":
                if (StrUtil.isNotBlank(bodyStr)) {
                    String contentType;
                    if (CollUtil.isEmpty(headers) || StrUtil.isBlank(headers.get("Content-Type"))) {
                        contentType = "application/x-www-form-urlencoded";
                    } else {
                        contentType = headers.get("Content-Type");
                    }
                    if (contentType.startsWith("application/json")) {
                        MediaType mediaType = MediaType.parse("application/json;charset=utf-8");
                        RequestBody requestBody = RequestBody.Companion.create(bodyStr, mediaType);
                        builder.method(method, requestBody);
                    } else if (contentType.startsWith("application/xml")) {
                        MediaType mediaType = MediaType.parse("application/xml;charset=utf-8");
                        RequestBody requestBody = RequestBody.Companion.create(bodyStr, mediaType);
                        builder.method(method, requestBody);
                    } else if ("application/x-www-form-urlencoded".equals(contentType)) {
                        FormBody.Builder formBodyBuilder = new FormBody.Builder();
                        Map<String, String> body = strToMap(bodyStr);
                        //添加请求参数
                        Set<String> keySet = body.keySet();
                        for (String key : keySet) {
                            String value = body.get(key);
                            formBodyBuilder.add(key, value);
                        }
                        builder.method(method, formBodyBuilder.build());
                    } else if ("application/octet-stream".equals(contentType)) {
                        MediaType mediaType = MediaType.parse("application/octet-stream;charset=utf-8");
                        RequestBody requestBody = RequestBody.Companion.create(new File(bodyStr), mediaType);
                        builder.method(method, requestBody);
                    } else {
                        log.debug("不支持的contentType：{}", contentType);
                        throw new BaseException("不支持的contentType：" + contentType);
                    }
                } else {
                    //没有参数
                    builder.method(method, RequestBody.Companion.create(new byte[0]));
                }
                break;
            case "GET":
            case "HEAD":
            case "DELETE":
            default:
                if (StrUtil.isNotBlank(bodyStr)) {
                    Map<String, String> body = strToMap(bodyStr);
                    //GET或HEAD请求添加请求参数
                    Set<String> keySet = body.keySet();
                    for (String key : keySet) {
                        String value = body.get(key);
                        urlBuilder.addQueryParameter(key, value);
                    }
                }
                builder.method(method, null);
        }
        builder.setUrl$okhttp(urlBuilder.build());
        return builder.build();
    }

    private static Map<String, String> strToMap(String jsonStr) {
        try {
            return JSONUtil.parse(jsonStr, new HashMap<>());
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}
