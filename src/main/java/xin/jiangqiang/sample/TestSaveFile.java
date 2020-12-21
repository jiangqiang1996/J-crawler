package xin.jiangqiang.sample;

import xin.jiangqiang.util.FileUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 从网络中保存图片演示
 *
 * @author jiangqiang
 * @date 2020/12/20 16:22
 */
public class TestSaveFile {
    public static void main(String[] args) {
        String url = "https://i.pximg.net/img-master/img/2020/08/31/00/02/59/84047892_p0_master1200.jpg";

        Map<String, String> proxyConfigs = new HashMap<>();
        //根据自己情况修改
        proxyConfigs.put("IP", "127.0.0.1");
        proxyConfigs.put("port", "8001");

        Map<String, String> headers = new HashMap<>();
        headers.put("referer", "https://www.pixiv.net/artworks/84047892");
        headers.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.25 Safari/537.36 Core/1.70.3861.400 QQBrowser/10.7.4313.400");

        FileUtil.saveFileFromURL("D:\\tmp\\20201219", url, proxyConfigs, null, headers, null);
    }
}
