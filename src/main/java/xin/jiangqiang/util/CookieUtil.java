package xin.jiangqiang.util;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

import java.io.*;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * @author jiangqiang
 * @date 2020/12/19 16:07
 */
public class CookieUtil {
    /**
     * 保存cookie到文件，保存成功返回true
     *
     * @param driver
     * @return 是否成功
     */
    public static boolean saveCookie(WebDriver driver, String filePath) {
        File cookieFile = new File(filePath);
        if (cookieFile.exists()) {
            cookieFile.delete();
        }
        try (
                FileWriter fileWriter = new FileWriter(cookieFile);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        ) {
            cookieFile.createNewFile();
            for (Cookie cookie : driver.manage().getCookies()) {
                bufferedWriter.write(
                        cookie.getName() + ";" +
                                cookie.getValue() + ";" +
                                cookie.getDomain() + ";" +
                                cookie.getPath() + ";" +
                                cookie.getExpiry() + ";" +
                                cookie.isSecure()
                );
                bufferedWriter.newLine();
            }
            bufferedWriter.flush();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void getCookies(WebDriver webDriver) {
        BufferedReader bufferedReader;
        webDriver.get("https://passport.csdn.net/account/login?from=http://my.csdn.net/my/mycsdn");
        try {
            File cookieFile = new File("csdn.cookie.txt");
            FileReader fileReader = new FileReader(cookieFile);
            bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                StringTokenizer stringTokenizer = new StringTokenizer(line, ";");
                while (stringTokenizer.hasMoreTokens()) {
                    String name = stringTokenizer.nextToken();
                    String value = stringTokenizer.nextToken();
                    String domain = stringTokenizer.nextToken();
                    String path = stringTokenizer.nextToken();
                    Date expiry = null;
                    String dt;

                    if (!(dt = stringTokenizer.nextToken()).equals("null")) {
                        expiry = new Date(dt);
                    }
                    boolean isSecure = new Boolean(stringTokenizer.nextToken()).booleanValue();
                    Cookie cookie = new Cookie(name, value, domain, path, expiry, isSecure);
                    webDriver.manage().addCookie(cookie);
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        webDriver.get("http://blog.csdn.net/");
    }
}
