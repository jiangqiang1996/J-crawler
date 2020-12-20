package xin.jiangqiang.util;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

import java.io.*;
import java.util.Date;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * @author jiangqiang
 * @date 2020/12/19 16:07
 */
@Slf4j
public class CookieUtil {
    /**
     * 保存cookie到文件，保存成功返回true
     *
     * @param driver   从driver获取cookie保存到文件
     * @param filePath 文件保存路径
     * @return 成功返回true
     */
    public static boolean saveCookie(WebDriver driver, String filePath) {
        File file = new File(filePath);
        if (file.exists()) {//文件存在则先删除
            file.delete();
        } else {
            FileUtil.mkParentDirIfNot(filePath);
        }
        try (
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)
        ) {
            Set<Cookie> cookies = driver.manage().getCookies();
            objectOutputStream.writeObject(cookies);
            return true;
        } catch (Exception e) {
            log.info(e.getMessage());
            return false;
        }
    }

    /**
     * 从文件获取cookie添加到driver
     *
     * @param driver   需要设置cookie的driver
     * @param filePath 文件保存路径
     * @param url      cookie对应的域名
     * @return 成功返回true
     */
    public static boolean getCookie(WebDriver driver, String filePath, String url) {
        File file = new File(filePath);
        if (!file.exists()) {//文件不存在直接返回false
            return false;
        }
        try (
                FileInputStream fileInputStream = new FileInputStream(new File(filePath));
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)
        ) {
            driver.get(url);
            Thread.sleep(3000);
            Set<Cookie> cookies = (Set<Cookie>) objectInputStream.readObject();
            driver.manage().deleteAllCookies();
            for (Cookie cookie : cookies) {
                driver.manage().addCookie(cookie);
            }
            return true;
        } catch (Exception e) {
            log.info(e.getMessage());
            return false;
        }
    }
}
