package xin.jiangqiang.util;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.apache.commons.io.FileUtils;
import xin.jiangqiang.entities.Page;

import java.io.*;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

/**
 * @author jiangqiang
 * @date 2020/12/16 9:45
 */
@Slf4j
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
    public static boolean saveFile(String fileDirName, Page page) throws IOException {
        return saveFile(fileDirName, page.getUrl(), page.getContent());
    }

    public static boolean saveFile(String fileDirName, String url, byte[] content) throws IOException {
        if (content == null) {
            log.info("保存失败,字节数组不能为空");
            return false;
        }
        File fileDir = new File(fileDirName);
        if (!fileDir.exists()) {
            boolean mkdirs = fileDir.mkdirs();
        }
        //通过URL截取文件名字
        String[] strings = url.split("/");
        String fileName = strings[strings.length - 1];
        File file = new File(fileDir, fileName);
        OutputStream os = new FileOutputStream(file);
        os.write(content);
        os.flush();
        os.close();
        return true;
    }

    /**
     * 图片如果有身份校验，则不能使用此方法
     *
     * @param fileDirName
     * @param url
     * @return
     */
    @Deprecated
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
            log.info(e.getMessage());
            return false;
        }
    }

    public static boolean saveFileFromURL(String fileDirName, String url, Map<String, String> proxyConfigs, Map<String, String> lines, Map<String, String> headers, Map<String, String> bodys) {
        Response response = HttpUtil.request(url, proxyConfigs, lines, headers, bodys);
        if (response == null) {
            return false;
        }
        if (response.code() != 200) {
            return false;
        }
        try {
            return saveFile(fileDirName, url, response.body().bytes());
        } catch (IOException e) {
            e.printStackTrace();
            log.info(e.getMessage());
            return false;
        }
    }

}
