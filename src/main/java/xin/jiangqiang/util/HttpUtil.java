package xin.jiangqiang.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

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
}
