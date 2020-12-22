package xin.jiangqiang.sample;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import xin.jiangqiang.net.OkHttpClientHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jiangqiang
 * @date 2020/12/20 15:39
 */
public class TestProxy {
    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();
        //根据自己情况修改
        map.put("IP", "127.0.0.1");
        map.put("port", "8001");
//        map.put("username", "123");
//        map.put("password", "314");
        System.out.println(TestProxy.test(map));
    }

    public static Boolean test(Map<String, String> proxyConfigs) {
        try {
            OkHttpClientHelper okHttpClientHelper = new OkHttpClientHelper(null);
            OkHttpClient client = okHttpClientHelper.processOkHttpClient(proxyConfigs);
            //请求行
            Map<String, String> lines = new HashMap<>();

            //请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("referer", "https://www.pixiv.net/artworks/84047892");
            headers.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.25 Safari/537.36 Core/1.70.3861.400 QQBrowser/10.7.4313.400");
            //请求体，请求参数
            Map<String, String> bodys = new HashMap<>();
            String url = "https://i.pximg.net/img-master/img/2020/08/31/00/02/59/84047892_p0_master1200.jpg";
            Request request = okHttpClientHelper.processRequest(url, lines, headers, bodys);
            Response response = client.newCall(request).execute();
            Integer code = response.code();
            String html = response.body().string();
            System.out.println(code);
//        System.out.println(html);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}