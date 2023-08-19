package top.jiangqiang.crawler.entities;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import top.jiangqiang.crawler.util.JSONUtil;
import top.jiangqiang.crawler.exception.BaseException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
public class RequestEntity {
    private final String url;
    private String method = "GET";
    private final Map<String, String> headersMap = new HashMap<>();
    private final Map<String, String> bodyMap = new HashMap<>();

    {
        //解决拿不到响应报文长度问题
        addHeaders("Accept-Encoding", "identity");
    }

    public RequestEntity(String url) {
        if (StrUtil.isBlank(url)) {
            throw new BaseException("请求链接不能为空");
        }
        this.url = url.trim();
    }

    public void setMethod(String method) {
        if (method == null) {
            this.method = "GET";
        } else {
            this.method = method.toUpperCase();
        }
    }

    public void addHeaders(String name, String value) {
        headersMap.put(name, value);
    }

    public Request buildRequest() {
        Request.Builder builder = new Request.Builder();
        //添加请求头
        Set<Map.Entry<String, String>> headerEntrySet = headersMap.entrySet();
        for (Map.Entry<String, String> entry : headerEntrySet) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
        switch (method) {
            case "POST":
            case "PUT":
            case "PATCH":
                if (CollUtil.isEmpty(bodyMap)) {
                    String contentType = headersMap.get("Content-Type");
                    if (StrUtil.isBlank(contentType)) {
                        contentType = "application/x-www-form-urlencoded";
                    }
                    if (contentType.startsWith("application/json")) {
                        MediaType mediaType = MediaType.parse("application/json;charset=utf-8");
                        RequestBody requestBody = RequestBody.Companion.create(JSONUtil.toJsonStr(bodyMap), mediaType);
                        builder.method(method, requestBody);
                    } else if (contentType.startsWith("application/xml")) {
                        MediaType mediaType = MediaType.parse("application/xml;charset=utf-8");
                        RequestBody requestBody = RequestBody.Companion.create(XmlUtil.mapToXmlStr(bodyMap), mediaType);
                        builder.method(method, requestBody);
                    } else if ("application/x-www-form-urlencoded".equals(contentType)) {
                        FormBody.Builder formBodyBuilder = new FormBody.Builder();
                        //添加请求参数
                        for (Map.Entry<String, String> bodyEntry : bodyMap.entrySet()) {
                            formBodyBuilder.add(bodyEntry.getKey(), bodyEntry.getValue());
                        }
                        builder.method(method, formBodyBuilder.build());
                    } else if ("application/octet-stream".equals(contentType)) {
                        //提交文件，只允许提交一个，map存文件路径名，使用此方式提交参数可能有缺陷，一般情况下不要使用此方法。
                        MediaType mediaType = MediaType.parse("application/octet-stream;charset=utf-8");
                        for (Map.Entry<String, String> bodyEntry : bodyMap.entrySet()) {
                            String value = bodyEntry.getValue();
                            RequestBody requestBody = RequestBody.Companion.create(new File(value), mediaType);
                            builder.method(method, requestBody);
                        }
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
                //GET或HEAD请求添加请求参数
                Set<String> keySet = bodyMap.keySet();
                for (String key : keySet) {
                    String value = bodyMap.get(key);
                    urlBuilder.addQueryParameter(key, value);
                }
                builder.method(method, null);
        }
        builder.setUrl$okhttp(urlBuilder.build());
        return builder.build();
    }

}
