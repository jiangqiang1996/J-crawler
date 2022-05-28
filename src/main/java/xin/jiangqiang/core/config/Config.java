package xin.jiangqiang.core.config;

import cn.hutool.core.util.StrUtil;
import lombok.*;
import xin.jiangqiang.core.app.Starter;
import xin.jiangqiang.core.filter.Filter;
import xin.jiangqiang.core.interfaces.HttpConfig;
import xin.jiangqiang.core.recoder.Recorder;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Data
public class Config implements HttpConfig<Config>, Serializable {
    private Charset charset = Charset.defaultCharset();
    private Integer threads = 50;
    /**
     * 注意，如果上一次爬取的最大深度为4，程序意外终止后，修改最大深度为2，继续上次爬取保存的结果继续爬取，那么无论如何至少会爬取一次。
     */
    private Integer depth = 4;
    /**
     * 项目启动类,决定了执行哪一个类下的@Befor,@Match等注解
     */
    private Class<? extends Starter> appClass;
    private Class<? extends Recorder> recorderClass;
    private Class<? extends Filter> filterClass;
    /**
     * 满足此正则表达式列表的URL会被提取
     */
    @Setter(AccessLevel.NONE)
    private List<String> regExs = new ArrayList<>();
    /**
     * 满足此正则表达式列表的会被过滤,不作为种子进行下次爬取
     */
    @Setter(AccessLevel.NONE)
    private List<String> reverseRegExs = new ArrayList<>();
    /**
     * 满足此正则表达式列表的也会被过滤,这是系统默认过滤规则,会过滤掉css,js
     */
    @Setter(AccessLevel.NONE)
    private List<String> defaultReverseRegExs = new ArrayList<>();


    /**
     * 是否启用默认正则表达式过滤,如果不启用则defaultReverseRegExs无效
     */
    private Boolean isUseDefault = true;

    /**
     * 下面两个属性只对内存记录器有效，对数据库记录器无效
     * 结束时没有爬取的种子会保存到此路径，用于断点续爬。路径不能为空，且未爬取的种子数不能为0，才会保存
     */
    private String savePath = "";
    /**
     * 是否继续上次爬取，保存路径为空或此属性值为false时均不会继续爬取
     */
    private Boolean isContinue = true;


    /**
     * http请求代理参数设置
     */
    private final Map<String, String> httpConfig = new HashMap<>();

    /**
     * http请求参数设置
     */
    private final Map<String, Map<String, String>> httpParams = new HashMap<>();

    /**
     * 请求客户端是否使用代理
     */
    private Boolean useProxy = false;
    /**
     * 使用okhttp时是否使用异步方式发请求，使用异步方式在任务都完毕之后需要等待一分钟才会结束程序
     */
    private Boolean async = true;

    {
        defaultReverseRegExs.add(".*\\.(js|css).*");
    }

    public Map<String, String> getLines() {
        return httpParams.computeIfAbsent("lines", k -> new HashMap<>());
    }

    public Config addLines(String key, String value) {
        Map<String, String> lines = getLines();
        lines.put(key, value);
        return this;
    }

    public Map<String, String> getHeaders() {
        return httpParams.computeIfAbsent("headers", k -> new HashMap<>());
    }

    public Config addHeaders(String key, String value) {
        Map<String, String> headers = getHeaders();
        headers.put(key, value);
        return this;
    }

    public Map<String, String> getBodys() {
        return httpParams.computeIfAbsent("bodys", k -> new HashMap<>());
    }

    public Config addBodys(String key, String value) {
        Map<String, String> bodys = getBodys();
        bodys.put(key, value);
        return this;
    }

    /**
     * 加号开头，去掉加号放正正则列表
     * 减号开头，去掉减号放逆正则列表
     * 否则直接放正正则列表
     *
     * @param regEx 正则表达式
     */
    public void addRegEx(String regEx) {
        if (StrUtil.isNotEmpty(regEx)) {
            if (regEx.startsWith("-")) {
                reverseRegExs.add(regEx.substring(1));
            } else if (regEx.startsWith("+")) {
                regExs.add(regEx.substring(1));
            } else {
                regExs.add(regEx);
            }
        }
    }

    /**
     * 添加默认正则表达式
     * 默认正则表达式没有正正则和逆正则之分
     *
     * @param regEx 满足该正则表达式的URL会被过滤
     */
    public void addDefaultRegEx(String regEx) {
        if (StrUtil.isNotEmpty(regEx)) {
            if (regEx.startsWith("-") || regEx.startsWith("+")) {
                defaultReverseRegExs.add(regEx.substring(1));
            } else {
                defaultReverseRegExs.add(regEx);
            }
        }
    }
}
