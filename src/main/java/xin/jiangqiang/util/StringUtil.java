package xin.jiangqiang.util;

public class StringUtil {

    public static boolean isNotEmpty(String string) {
        if (string == null || string.length() == 0) {
            return false;
        }
        return true;
    }
}
