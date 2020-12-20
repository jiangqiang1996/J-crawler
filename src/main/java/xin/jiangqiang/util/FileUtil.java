package xin.jiangqiang.util;

import org.apache.commons.io.FileUtils;
import xin.jiangqiang.entities.Page;

import java.io.*;
import java.net.URL;

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

    /**
     * 小文件对应的URL可以直接使用框架请求后，通过page对象中的content保存（也就是此方法保存）
     * 如果是大文件，建议过滤掉框架自动的请求。单独使用相关工具类从网络中下载文件
     *
     * @param fileDirName 保存的目录名字
     * @param page        当前爬虫对象
     * @throws IOException 保存文件失败
     */
    public static void saveFile(String fileDirName, Page page) throws IOException {
        File fileDir = new File(fileDirName);
        if (!fileDir.exists()) {
            boolean mkdirs = fileDir.mkdirs();
        }
        //通过URL截取文件名字
        String[] strings = page.getUrl().split("/");
        String fileName = strings[strings.length - 1];
        File file = new File(fileDir, fileName);
        OutputStream os = new FileOutputStream(file);
        os.write(page.getContent());
        os.flush();
        os.close();
    }

    public static boolean saveFileFromURL(String fileDirName, String url) {
        try {
            File fileDir = new File(fileDirName);
            if (!fileDir.exists()) {
                boolean mkdirs = fileDir.mkdirs();
            }
            //通过URL截取文件名字
            String[] strings = url.split("/");
            String fileName = strings[strings.length - 1];
            File file = new File(fileDir, fileName);
            FileUtils.copyURLToFile(new URL(url), file);
            return true;
        } catch (IOException e) {
            return false;
        }

    }
}
