package top.jiangqiang.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jiangqiang
 * @date 2022-09-30
 */
public class FileUtil extends cn.hutool.core.io.FileUtil {
    /**
     * 从url获取文件mimeType
     *
     * @param url url地址
     * @return
     */
    public static String getMimeTypeFromUrl(String url) {
        return getMimeType("." + getExtName(url));
    }

    /**
     * 获取文件后缀名
     *
     * @param url
     * @return
     */
    public static String getExtName(String url) {
        return FileTypeUtil.getType(URLUtil.getStream(URLUtil.url(url)));
    }

    public static List<String> downloadFilesFromUrlList(List<String> urlList, File destDir, Integer size, String... allowDownloadList) {
        List<String> fileNameList = new ArrayList<>();
        if (CollUtil.isNotEmpty(urlList)) {
            for (String url : urlList) {
                String fileName = downloadFileFromUrl(url, destDir, size, allowDownloadList);
                if (StrUtil.isNotBlank(fileName)) {
                    fileNameList.add(fileName);
                }
            }
        }
        return fileNameList;
    }

    public static Integer getSizeFromUrl(String url) {
        try {
            return URLUtil.url(url).openConnection().getContentLength();
        } catch (IOException e) {
            return -1;
        }
    }

    /**
     * @param url               文件地址
     * @param destDir           目录名
     * @param allowDownloadList 允许的文件格式，扩展名或mimeType
     * @return
     */
    public static String downloadFileFromUrl(String url, File destDir, Integer minSize, String... allowDownloadList) {
        try {
            int fileSize = getSizeFromUrl(url);
            if (fileSize < minSize) {
                return null;
            }
            String extName = getExtName(url);
            String destFileName = genFileKey();
            if (StrUtil.isBlank(extName)) {
                extName = "";
            } else {
                destFileName = destFileName + "." + extName;
            }
            String mimeType = getMimeType(destFileName);
            if (mimeType == null) {
                mimeType = "";
            }
            if (ArrayUtil.isEmpty(allowDownloadList)) {
                HttpUtil.downloadFileFromUrl(url, FileUtil.file(destDir, destFileName), null);
                return destFileName;
            } else {
                for (String extNameOrMimeType : allowDownloadList) {
                    if (extName.equals(extNameOrMimeType) || mimeType.startsWith(extNameOrMimeType)) {
                        HttpUtil.downloadFileFromUrl(url, FileUtil.file(destDir, destFileName), null);
                        return destFileName;
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    /**
     * 根据文件名和sha256生成文件唯一标识
     *
     * @param fileName 文件名或key
     * @param sha256
     * @return
     */
    public static String genFileKey(String fileName, String sha256) {
        String extName = FileNameUtil.extName(fileName);
        return genFileKeyByExtName(extName, sha256);
    }

    public static String genFileKeyByExtName(String extName, String randomStr) {
        String tmpStr = mixinStr(String.valueOf(System.currentTimeMillis()), randomStr);
        if (StrUtil.isBlank(extName)) {
            return tmpStr;
        } else {
            return tmpStr + "." + extName;
        }
    }

    /**
     * key
     *
     * @return
     */
    public static String genFileKey() {
        return mixinStr(String.valueOf(System.currentTimeMillis()), RandomUtil.randomString(64));
    }

    private static String getFileNameFromUrl(String url) {
        if (StrUtil.isBlank(url)) {
            return "";
        } else {
            String tmpStr = url;
            if (tmpStr.contains("?")) {
                tmpStr = tmpStr.substring(0, tmpStr.indexOf('?'));
            }
            tmpStr = StrUtil.subAfter(tmpStr, '/', true);
            if (tmpStr.contains("=")) {
                tmpStr = tmpStr.substring(0, tmpStr.indexOf('='));
            }
            return tmpStr;
        }
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

}
