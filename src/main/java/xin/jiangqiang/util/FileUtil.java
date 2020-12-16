package xin.jiangqiang.util;

import java.io.File;

/**
 * @author jiangqiang
 * @date 2020/12/16 9:45
 */
public class FileUtil {

    /**
     * 给定一个路径，如果父级目录不存在则创建父级目录
     * 相对路径也不会有问题
     *
     * @param filePath 文件路径
     * @return
     */
    public static boolean mkParentDirIfNot(String filePath) {
        File file = new File(filePath);
        System.out.println(file.getParentFile());
        System.out.println(file.getAbsolutePath());
        System.out.println(file.getAbsoluteFile().getParentFile());
        File parentFile = file.getParentFile();
        if (parentFile == null) {
            return true;//给定路径没有父级目录，不需要创建
        }
        if (parentFile.exists()) {
            return true;//父级目录已经存在
        } else {
            return parentFile.mkdirs();
        }
    }
}
