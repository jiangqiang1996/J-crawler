package xin.jiangqiang.test;

import lombok.extern.slf4j.Slf4j;
import xin.jiangqiang.annotation.After;
import xin.jiangqiang.annotation.Before;
import xin.jiangqiang.annotation.Deal;
import xin.jiangqiang.app.Application;
import xin.jiangqiang.entities.Next;
import xin.jiangqiang.entities.Page;
import xin.jiangqiang.net.RequestMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * 最基本的爬虫实例
 * 基本流程:
 * 1.首先需要继承Application类
 * 2.然后创建本类实例
 * 3.加入爬虫种子
 * 4.配置对该种子请求时的请求行，请求头，请求体（参数），以及HTTP代理，以及其他自定义信息
 * 5.调用start方法
 * 6.对爬取结果进行处理，主要包括将数据流保存为图片等静态资源，从html中获取文本信息存入数据库，从html中获取URL存入next，为下次爬取做准备
 * <p>
 * 执行本程序可以看见完整的生命周期：
 * 我们需要关注的只有三个：
 * 1.before
 * 2.deal
 * 3.after
 * 最常用到的也就只有deal一个，我们保存数据，以及提取URL，为下次爬取做的一切准备均在此处处理
 * <p>
 * deal方法可以任意，但是注解必须为@Deal，我们也可以使用@Match注解。@Match是加强版的@Deal注解：
 * 可以使用Match注解匹配正则表达式，匹配type类型，匹配响应码。比如：
 * 一系列URL有相同特性，比如都是文章内容，他们的HTML结构都一样，因此可以走相同逻辑进行解析，此时可以使用type分类
 * 同时，文章的URL可能有类似之处，可以使用正则表达式直接分类。
 * 此外，如果遇到302响应码的请求，我们可以统一处理让他重新发起请求等等。
 *
 * @author jiangqiang
 * @date 2020/12/15 17:19
 */
@Slf4j
public class BaseApplicationTest extends Application {
    @Before
    public void before() {
        log.info("before执行了");
    }

    @Deal
    public void deal(Page page, Next next) {
        log.info("deal执行了");
    }

    @After
    public void after() {
        log.info("after执行了");
    }

    public static void main(String[] args) {
        BaseApplicationTest baseApplicationTest = new BaseApplicationTest();
        Map<String, String> lines = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        Map<String, String> bodys = new HashMap<>();
        Map<String, String> configs = new HashMap<>();
        baseApplicationTest.getCrawler().addSeed("https://blog.jiangqiang.xin").setLines(lines).setHeaders(headers)
                .setBodys(bodys).setConfigs(configs).setMethod(RequestMethod.POST);
        baseApplicationTest.start();
    }
}
