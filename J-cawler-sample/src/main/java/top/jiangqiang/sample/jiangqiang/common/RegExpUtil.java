package top.jiangqiang.sample.jiangqiang.common;

import cn.hutool.core.util.ReUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExpUtil {
    public static Boolean isMatch(String str, String regEx) {
        return ReUtil.isMatch(regEx, str);
    }
}
