package xin.jiangqiang.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import xin.jiangqiang.util.StringUtil;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Config {
    Charset charset = Charset.defaultCharset();
    Integer threads = 50;
    Integer depth = 4;//注意，如果上一次爬取的最大深度为4，程序意外终止后，修改最大深度为2，继续上次爬取保存的结果继续爬取，那么无论如何至少会爬取一次。
    Class<?> appClass;//项目启动类,决定了执行哪一个类下的@Befor,@Match等注解
    List<String> regExs = new ArrayList<>();//满足此正则表达式列表的URL会被提取
    List<String> reverseRegExs = new ArrayList<>();//满足此正则表达式列表的会被过滤,不作为种子进行下次爬取
    List<String> defaultReverseRegExs = new ArrayList<>();//满足此正则表达式列表的也会被过滤,这是系统默认过滤规则,会过滤掉css,js
    //是否启用默认正则表达式过滤,如果不启用则defaultReverseRegExs无效
    Boolean isUseDefault = true;

    //下面两个属性只对内存记录器有效，对数据库记录器无效
    String savePath = "";//结束时没有爬取的种子会保存到此路径，用于断点续爬。路径不能为空，且未爬取的种子数不能为0，才会保存
    Boolean isContinue = false;//是否继续上次爬取，保存路径为空或此属性值为false时均不会继续爬取

    //下面是selenium专属配置
    String driverPath;//驱动路径
    String binaryPath;//浏览器可执行文件路径
    String browserType = "chrome";//浏览器类型
    Integer delaytime = 5;//单位:秒,使用selenium时,需要等待js解析完成,否则拿不到单页面项目的最终页面,可以根据机器适当延长
    Boolean isHeadLess = true;//默认使用无界面浏览器模式，如果调试时建议设置false，只有edge，火狐，谷歌浏览器有无头模式

    {
        defaultReverseRegExs.add(".*\\.(js|css).*");
    }

    public Config(Class<?> appClassName) {
        this.appClass = appClassName;
    }

    /**
     * 加号开头，去掉加号放正正则列表
     * 减号开头，去掉减号放逆正则列表
     * 否则直接放正正则列表
     *
     * @param regEx 正则表达式
     */
    public void addRegEx(String regEx) {
        if (StringUtil.isNotEmpty(regEx)) {
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
        if (StringUtil.isNotEmpty(regEx)) {
            if (regEx.startsWith("-") || regEx.startsWith("+")) {
                defaultReverseRegExs.add(regEx.substring(1));
            } else {
                defaultReverseRegExs.add(regEx);
            }
        }
    }
}
