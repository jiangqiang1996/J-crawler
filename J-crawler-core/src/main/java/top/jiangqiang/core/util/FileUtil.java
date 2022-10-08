package top.jiangqiang.core.util;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.IoUtil;
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
        String str = """
                'apk'        'application/vnd.android.package-archive',
                '3gp'        'video/3gpp'
                'ai'        'application/postscript'
                'aif'        'audio/x-aiff'
                'aifc'        'audio/x-aiff'
                'aiff'        'audio/x-aiff'
                'asc'        'text/plain'
                'atom'        'application/atom+xml'
                'au'        'audio/basic'
                'avi'        'video/x-msvideo'
                'bcpio'        'application/x-bcpio'
                'bin'        'application/octet-stream'
                'bmp'        'image/bmp'
                'cdf'        'application/x-netcdf'
                'cgm'        'image/cgm'
                'class'        'application/octet-stream'
                'cpio'        'application/x-cpio'
                'cpt'        'application/mac-compactpro'
                'csh'        'application/x-csh'
                'css'        'text/css'
                'dcr'        'application/x-director'
                'dif'        'video/x-dv'
                'dir'        'application/x-director'
                'djv'        'image/vnd.djvu'
                'djvu'        'image/vnd.djvu'
                'dll'        'application/octet-stream'
                'dmg'        'application/octet-stream'
                'dms'        'application/octet-stream'
                'doc'        'application/msword'
                'dtd'        'application/xml-dtd'
                'dv'        'video/x-dv'
                'dvi'        'application/x-dvi'
                'dxr'        'application/x-director'
                'eps'        'application/postscript'
                'etx'        'text/x-setext'
                'exe'        'application/octet-stream'
                'ez'        'application/andrew-inset'
                'flv'        'video/x-flv'
                'gif'        'image/gif'
                'gram'        'application/srgs'
                'grxml'        'application/srgs+xml'
                'gtar'        'application/x-gtar'
                'gz'        'application/x-gzip'
                'hdf'        'application/x-hdf'
                'hqx'        'application/mac-binhex40'
                'htm'        'text/html'
                'html'        'text/html'
                'ice'        'x-conference/x-cooltalk'
                'ico'        'image/x-icon'
                'ics'        'text/calendar'
                'ief'        'image/ief'
                'ifb'        'text/calendar'
                'iges'        'model/iges'
                'igs'        'model/iges'
                'jnlp'        'application/x-java-jnlp-file'
                'jp2'        'image/jp2'
                'jpe'        'image/jpeg'
                'jpeg'        'image/jpeg'
                'jpg'        'image/jpeg'
                'js'        'application/x-javascript'
                'kar'        'audio/midi'
                'latex'        'application/x-latex'
                'lha'        'application/octet-stream'
                'lzh'        'application/octet-stream'
                'm3u'        'audio/x-mpegurl'
                'm4a'        'audio/mp4a-latm'
                'm4p'        'audio/mp4a-latm'
                'm4u'        'video/vnd.mpegurl'
                'm4v'        'video/x-m4v'
                'mac'        'image/x-macpaint'
                'man'        'application/x-troff-man'
                'mathml'        'application/mathml+xml'
                'me'        'application/x-troff-me'
                'mesh'        'model/mesh'
                'mid'        'audio/midi'
                'midi'        'audio/midi'
                'mif'        'application/vnd.mif'
                'mov'        'video/quicktime'
                'movie'        'video/x-sgi-movie'
                'mp2'        'audio/mpeg'
                'mp3'        'audio/mpeg'
                'mp4'        'video/mp4'
                'mpe'        'video/mpeg'
                'mpeg'        'video/mpeg'
                'mpg'        'video/mpeg'
                'mpga'        'audio/mpeg'
                'ms'        'application/x-troff-ms'
                'msh'        'model/mesh'
                'mxu'        'video/vnd.mpegurl'
                'nc'        'application/x-netcdf'
                'oda'        'application/oda'
                'ogg'        'application/ogg'
                'ogv'        'video/ogv'
                'pbm'        'image/x-portable-bitmap'
                'pct'        'image/pict'
                'pdb'        'chemical/x-pdb'
                'pdf'        'application/pdf'
                'pgm'        'image/x-portable-graymap'
                'pgn'        'application/x-chess-pgn'
                'pic'        'image/pict'
                'pict'        'image/pict'
                'png'        'image/png'
                'pnm'        'image/x-portable-anymap'
                'pnt'        'image/x-macpaint'
                'pntg'        'image/x-macpaint'
                'ppm'        'image/x-portable-pixmap'
                'ppt'        'application/vnd.ms-powerpoint'
                'ps'        'application/postscript'
                'qt'        'video/quicktime'
                'qti'        'image/x-quicktime'
                'qtif'        'image/x-quicktime'
                'ra'        'audio/x-pn-realaudio'
                'ram'        'audio/x-pn-realaudio'
                'ras'        'image/x-cmu-raster'
                'rdf'        'application/rdf+xml'
                'rgb'        'image/x-rgb'
                'rm'        'application/vnd.rn-realmedia'
                'roff'        'application/x-troff'
                'rtf'        'text/rtf'
                'rtx'        'text/richtext'
                'sgm'        'text/sgml'
                'sgml'        'text/sgml'
                'sh'        'application/x-sh'
                'shar'        'application/x-shar'
                'silo'        'model/mesh'
                'sit'        'application/x-stuffit'
                'skd'        'application/x-koan'
                'skm'        'application/x-koan'
                'skp'        'application/x-koan'
                'skt'        'application/x-koan'
                'smi'        'application/smil'
                'smil'        'application/smil'
                'snd'        'audio/basic'
                'so'        'application/octet-stream'
                'spl'        'application/x-futuresplash'
                'src'        'application/x-wais-source'
                'sv4cpio'        'application/x-sv4cpio'
                'sv4crc'        'application/x-sv4crc'
                'svg'        'image/svg+xml'
                'swf'        'application/x-shockwave-flash'
                't'        'application/x-troff'
                'tar'        'application/x-tar'
                'tcl'        'application/x-tcl'
                'tex'        'application/x-tex'
                'texi'        'application/x-texinfo'
                'texinfo'        'application/x-texinfo'
                'tif'        'image/tiff'
                'tiff'        'image/tiff'
                'tr'        'application/x-troff'
                'tsv'        'text/tab-separated-values'
                'txt'        'text/plain'
                'ustar'        'application/x-ustar'
                'vcd'        'application/x-cdlink'
                'vrml'        'model/vrml'
                'vxml'        'application/voicexml+xml'
                'wav'        'audio/x-wav'
                'wbmp'        'image/vnd.wap.wbmp'
                'wbxml'        'application/vnd.wap.wbxml'
                'webm'        'video/webm'
                'wml'        'text/vnd.wap.wml'
                'wmlc'        'application/vnd.wap.wmlc'
                'wmls'        'text/vnd.wap.wmlscript'
                'wmlsc'        'application/vnd.wap.wmlscriptc'
                'wmv'        'video/x-ms-wmv'
                'wrl'        'model/vrml'
                'xbm'        'image/x-xbitmap'
                'xht'        'application/xhtml+xml'
                'xhtml'        'application/xhtml+xml'
                'xls'        'application/vnd.ms-excel'
                'xml'        'application/xml'
                'xpm'        'image/x-xpixmap'
                'xsl'        'application/xml'
                'xslt'        'application/xslt+xml'
                'xul'        'application/vnd.mozilla.xul+xml'
                'xwd'        'image/x-xwindowdump'
                'xyz'        'chemical/x-xyz'
                'zip'        'application/zip'
                """;
        String[] split = str.split("\n");
        List<String> strings = Arrays.stream(split).toList();
        List<String> strings1 = strings.stream().map(s -> s.trim()).filter(StrUtil::isNotBlank).toList();
        Map<String, String> map = new HashMap<>();
        strings1.stream().forEach(s -> {
            String[] split1 = s.split("\\s+");
            if (split1.length == 2) {
                String key = split1[0].trim();
                String value = split1[1].trim();
                if (key.startsWith("'")) {
                    key = key.substring(1);
                }
                if (key.endsWith("'")) {
                    key = key.substring(0, key.length() - 1);
                }

                if (value.startsWith("'")) {
                    value = value.substring(1);
                }
                if (value.endsWith("'")) {
                    value = value.substring(0, value.length() - 1);
                }
                if (StrUtil.isNotBlank(key) && StrUtil.isNotBlank(value)) {
                    map.put(key, value);
                }
            }
        });
        System.out.println(JSONUtil.toJsonPrettyStr(map));
        if (StrUtil.isBlank(contentType)) {
            return null;
        } else {
            return null;
        }
    }


    public static void main(String[] args) {
        System.out.println(getTypeFromMimeType(""));
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
        try (
                BufferedInputStream bufferedInputStream = IoUtil.toBuffered(inputStream);
        ) {
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

