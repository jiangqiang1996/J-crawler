package top.jiangqiang.sample.jiangqiang.common;

/**
 * @Author: JiangQiang
 * @Date: 2022年05月14日 10:49
 */
public class StringUtil {
    /**
     * 判断字符串是否不为空
     *
     * @param string 字符串
     * @return 不为空返回true
     */
    public static boolean isNotEmpty(String string) {
        if (string == null || string.length() == 0) {
            return false;
        }
        return true;
    }

    /**
     * 判断多个字符串是否同时不为空
     *
     * @param strings 字符串列表
     * @return 同时不为空返回true
     */
    public static boolean isNotEmpty(String... strings) {
        for (String str : strings) {
            if (isEmpty(str)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断多个字符串是否同时为空
     *
     * @param strings 字符串列表
     * @return 同时为空返回true
     */
    public static boolean isEmpty(String... strings) {
        for (String str : strings) {
            if (isNotEmpty(str)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断字符串是否为空
     *
     * @param string 字符串
     * @return 为空返回true
     */
    public static boolean isEmpty(String string) {
        return !isNotEmpty(string);
    }
}
