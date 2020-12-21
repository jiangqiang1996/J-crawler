package xin.jiangqiang.util;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import xin.jiangqiang.net.OkHttpClientHelper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jiangqiang
 * @date 2020/12/19 22:44
 */
@Slf4j
public class HttpUtil {
    public static String getContentType(String urlString) {
        return getContentType(urlString, "", null);
    }

    public static String getContentType(String urlString, String IP, Integer port) {
        try {
            Proxy proxy = null;
            if (StringUtil.isNotEmpty(IP) && port != null) {
                InetSocketAddress addr = new InetSocketAddress(IP, port);
                proxy = new Proxy(Proxy.Type.HTTP, addr);
            }
            URL url = new URL(urlString);
            if (proxy != null) {
                String contentType = url.openConnection(proxy).getContentType();
                return contentType != null ? contentType : "";
            } else {
                String contentType = url.openConnection().getContentType();
                return contentType != null ? contentType : "";
            }
        } catch (IOException e) {
            log.info(e.getMessage());
            return "";
        }
    }

    public static Response request(String url, Map<String, String> proxyConfigs, Map<String, String> lines, Map<String, String> headers, Map<String, String> bodys) {
        try {
            if (proxyConfigs == null) {
                proxyConfigs = new HashMap<>();
            }
            if (lines == null) {
                lines = new HashMap<>();
            }
            if (headers == null) {
                headers = new HashMap<>();
            }
            if (bodys == null) {
                bodys = new HashMap<>();
            }
            OkHttpClientHelper okHttpClientHelper = new OkHttpClientHelper(null, null);
            OkHttpClient client = okHttpClientHelper.processOkHttpClient(proxyConfigs);
            Request request = okHttpClientHelper.processRequest(url, lines, headers, bodys);
            Response response = client.newCall(request).execute();
            Integer code = response.code();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
