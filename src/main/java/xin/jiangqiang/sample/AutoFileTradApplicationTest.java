package xin.jiangqiang.sample;

import lombok.extern.slf4j.Slf4j;
import xin.jiangqiang.annotation.After;
import xin.jiangqiang.annotation.Before;
import xin.jiangqiang.annotation.Deal;
import xin.jiangqiang.annotation.Match;
import xin.jiangqiang.app.TradApplication;
import xin.jiangqiang.entities.Page;
import xin.jiangqiang.net.RequestMethod;
import xin.jiangqiang.util.FileUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 自动从maven仓库抓取文件并保存，小文件可以直接使用Page对象进行保存，
 * 大文件应该直接从URL中进行下载文件。
 *
 * @author jiangqiang
 * @date 2020/12/18 21:12
 */
@Slf4j
public class AutoFileTradApplicationTest extends TradApplication {

    @Deal
    public void deal(Page page) {
        try {
            FileUtil.saveFile("D:\\tmp\\1.0", page);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @After
    public void after() {
        log.info("after执行了");
    }

    public static void main(String[] args) {
        AutoFileTradApplicationTest test = new AutoFileTradApplicationTest();
        Map<String, String> lines = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        Map<String, String> bodys = new HashMap<>();
        Map<String, String> configs = new HashMap<>();
        test.getConfig().addRegEx("https://.*");//满足正则表达式的所有URL将会自动提取到deal生命周期的Next对象
        test.getConfig().setIsUseDefault(true);//启用默认内置正则表达式过滤，默认会过滤css，js的url
        test.getConfig().setDepth(4);//设置爬取深度，默认就是4，第一个种子URL为1，从种子里面获取的URL为2，从深度为2的URL中获取的URL为3，超过4就不会爬取了
        test.getCrawler().addSeed("https://repo1.maven.org/maven2/xin/jiangqiang/J-crawler/1.0/").setType("home").setLines(lines).setHeaders(headers)
                .setBodys(bodys).setConfigs(configs).setMethod(RequestMethod.GET);
        test.start();
    }
}
