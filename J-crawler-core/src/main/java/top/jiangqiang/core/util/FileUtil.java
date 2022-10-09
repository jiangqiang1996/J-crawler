package top.jiangqiang.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import okhttp3.Response;
import okhttp3.ResponseBody;
import top.jiangqiang.core.entities.Page;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.util.*;

/**
 * @author jiangqiang
 * @date 2022-09-30
 */
public class FileUtil extends cn.hutool.core.io.FileUtil {
    public final static Map<String, String> extNameMimeType;
    public final static Map<String, List<String>> mimeTypeExtName;

    static {
        String jsonStr = ResourceUtil.readUtf8Str("mimeType.json");
        extNameMimeType = JSONUtil.parse(jsonStr, new HashMap<>());
        mimeTypeExtName = new HashMap<>();
        extNameMimeType.forEach((key, value) -> {
            List<String> valueList = mimeTypeExtName.get(value);
            if (valueList == null) {
                valueList = new LinkedList<>();
            }
            valueList.add(key);
            mimeTypeExtName.put(value, valueList);
        });
    }

    /**
     * 将两个字符串按照一定规则混合，防止信息泄露
     *
     * @param str1
     * @param str2
     * @return
     */
    public static String mixinStr(String str1, String str2) {
        if (StrUtil.isBlank(str1)) {
            return str2;
        }
        if (StrUtil.isBlank(str2)) {
            return str2;
        }
        String longStr;
        String shortStr;
        if (str1.length() > str2.length()) {
            longStr = str1;
            shortStr = str2;
        } else {
            longStr = str2;
            shortStr = str1;
        }
        int i = longStr.length() / shortStr.length();
        StringBuilder stringBuilder = new StringBuilder();
        for (int index = 0; index < shortStr.length(); index++) {
            stringBuilder.append(shortStr.charAt(index)).append(longStr, index * i, index * i + i);
        }
        stringBuilder.append(longStr.substring(shortStr.length() * i));
        return stringBuilder.toString();
    }


    /**
     * @param page     需要下载的文件来源
     * @param response okhttp响应
     * @param dirPath  文件下载到指定目录
     * @return 返回下载后保存的文件名，null表示下载失败
     */
    public static String downloadFile(Page page, Response response, String dirPath) {
        //根据mimeType获取扩展名
        String type = getTypeFromMimeType(page.getContentType());
        if (ArrayUtil.isNotEmpty(page.getBodyBytes())) {
            return saveFileFromPage(page, type, dirPath);
        } else {
            return downloadFile(response, type, dirPath);
        }
    }

    private static String getTypeFromMimeType(String contentType) {
        if (StrUtil.isBlank(contentType)) {
            return null;
        } else {
            contentType = contentType.toLowerCase();
            if (contentType.endsWith(";charset=utf-8")) {
                contentType = StrUtil.subBefore(contentType, ";charset=utf-8", true);
            }
            if ("application/octet-stream".equals(contentType)) {
                return null;
            }
            List<String> strings = mimeTypeExtName.get(contentType);
            if (CollUtil.isNotEmpty(strings)) {
                return strings.get(0);
            }
            return null;
        }
    }

    public static String saveFileFromPage(Page page, String type, String dirPath) {
        if (page == null) {
            return null;
        }
        byte[] data = page.getBodyBytes();
        if (ArrayUtil.isEmpty(data)) {
            return null;
        }
        if (StrUtil.isBlank(type)) {
            String contentType = page.getContentType();
            if (StrUtil.isNotBlank(contentType)) {
                type = getTypeFromMimeType(contentType);
            }
            if (StrUtil.isBlank(type)) {
                type = getTypeFromBytes(data);
            }
        }
        String fileName = genFileName(type);
        IoUtil.write(FileUtil.getOutputStream(FileUtil.file(dirPath, fileName)), true, data);
        return fileName;
    }

    /**
     * okhttp使用
     *
     * @param response
     * @param type
     * @param dirPath
     * @return
     */
    public static String downloadFile(Response response, String type, String dirPath) {
        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            return null;
        }
        return downloadFile(responseBody.byteStream(), type, dirPath);
    }

    public static String downloadFile(InputStream inputStream, String type, String dirPath) {
        BufferedOutputStream bufferedOutputStream = null;
        try (BufferedInputStream bufferedInputStream = IoUtil.toBuffered(inputStream);) {
            String fileName = genFileName();
            byte[] buffer = new byte[4096];
            int len;
            boolean flag = true;
            while ((len = bufferedInputStream.read(buffer)) != -1) {
                if (flag) {
                    flag = false;
                    if (StrUtil.isBlank(type)) {
                        type = getTypeFromBytes(buffer);
                    }
                    if (StrUtil.isNotBlank(type)) {
                        fileName = fileName + "." + type;
                    }
                    bufferedOutputStream = IoUtil.toBuffered(FileUtil.getOutputStream(FileUtil.file(dirPath, fileName)));
                }
                bufferedOutputStream.write(buffer, 0, len);
            }
            return fileName;
        } catch (Exception ignored) {
            return null;
        } finally {
            IoUtil.close(bufferedOutputStream);
        }
    }

    private static String getTypeFromBytes(byte[] buffer) {
        String type;
        type = FileTypeUtil.getType(HexUtil.encodeHexStr(buffer));
        return type;
    }

    private static String genFileName(String type) {
        if (StrUtil.isBlank(type)) {
            return DateUtil.format(new Date(), DatePattern.PURE_DATETIME_MS_PATTERN) + RandomUtil.randomString(10);
        } else {
            return DateUtil.format(new Date(), DatePattern.PURE_DATETIME_MS_PATTERN) + RandomUtil.randomString(10) + "." + type;
        }
    }

    private static String genFileName() {
        return genFileName(null);
    }
}

