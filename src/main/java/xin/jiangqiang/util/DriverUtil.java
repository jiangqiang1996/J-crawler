package xin.jiangqiang.util;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;


/**
 * @author jiangqiang
 * @date 2020/12/18 20:47
 */
public class DriverUtil {
    /**
     * 获取整个html页面
     *
     * @param driver 驱动
     * @return 返回html页面
     */
    public static String getHtml(WebDriver driver) {
        return driver.findElement(By.xpath("//*")).getAttribute("outerHTML");
    }
}
