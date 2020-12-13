package xin.jiangqiang.config;

import lombok.Data;
import xin.jiangqiang.util.StringUtil;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Data
public class Config {
    Charset charset = Charset.defaultCharset();
    Integer threads = 50;
    Integer depth = 2;
    String packageName;
    List<String> regExs = new ArrayList<>();
    List<String> reverseRegExs = new ArrayList<>();
    List<String> defaultReverseRegExs = new ArrayList<>();
    //是否启用默认正则表达式过滤
    Boolean isUseDefault = true;

    {
        defaultReverseRegExs.add(".*\\.(js|css).*");
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
     * @param regEx
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
