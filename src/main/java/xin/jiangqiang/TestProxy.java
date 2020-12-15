package xin.jiangqiang;

import okhttp3.*;
import xin.jiangqiang.net.OkHttpClientHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jiangqiang
 * @date 2020/12/15 9:38
 */
public class TestProxy {
    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();
        //根据自己情况修改
        map.put("IP", "14.18.49.22");
        map.put("port", "233");
        map.put("username", "123");
        map.put("password", "314");
        System.out.println(TestProxy.test(map));
    }

    public static Boolean test(Map<String, String> map) {
        try {
            OkHttpClientHelper okHttpClientHelper = new OkHttpClientHelper(null, null);
            OkHttpClient client = okHttpClientHelper.processOkHttpClient(map);
            Request.Builder builder = new Request.Builder().method("GET", null).url("http://www.baidu.com");//没有参数
            Response response = client.newCall(builder.build()).execute();
            Integer code = response.code();
            String html = response.body().string();
            System.out.println(code);
//        System.out.println(html);
            return true;
        } catch (Exception e) {
//            e.printStackTrace();
            return false;
        }
    }
}
