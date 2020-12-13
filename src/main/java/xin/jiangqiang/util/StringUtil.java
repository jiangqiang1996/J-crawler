package xin.jiangqiang.util;

public class StringUtil {

    public static boolean isNotEmpty(String string) {
        if (string == null || string.length() == 0) {
            return false;
        }
        return true;
    }

    public static boolean isEmpty(String string) {
        return !isNotEmpty(string);
    }
}
