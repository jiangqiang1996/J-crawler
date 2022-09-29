package top.jiangqiang.test;

import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.net.URLEncodeUtil;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import top.jiangqiang.http.HttpUtil;
import top.jiangqiang.interceptor.HttpInterceptorr;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jiangqiang
 * @date 2022-09-29
 */
public class Test {
    public static void main(String[] args) throws IOException {
        String url;
        url = "https://mp.weixin.qq.com/s?__biz=MzIxMjgzMDUyNw==&mid=2247489048&idx=1&sn=072866b456945d297ec2516dd72e5a41&chksm=97414648a036cf5eba9ddf88c7cf7a27809ae414b4ce43d5595c7351172d04d70664eab25761&scene=90&subscene=93&sessionid=1664460391&clicktime=1664460397&enterid=1664460397&ascene=56&fasttmpl_type=0&fasttmpl_fullversion=6351034-zh_CN-zip&fasttmpl_flag=0&realreporttime=1664460397767&devicetype=android-31&version=28001c3b&nettype=WIFI&abtest_cookie=AAACAA%3D%3D&lang=zh_CN&session_us=gh_391abad800db&exportkey=A01mvM0fP%2BtbjfOBlrDdga8%3D&pass_ticket=fRKCL5vF5nmJEU4Y0DJ60ftOP9hbDgcI5Syn9wR%2BP26sjnBzcmbbozXA3pV42cES&wx_header=3";
//        url = "https://www.qianyi.xin";
        HttpUtil httpUtil = new HttpUtil();
        Map<String, String> lines = new HashMap<>();
//        lines.put("method", "POST");
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "apifox/1.0.0 (https://www.apifox.cn)");
        headers.put("Accept", "*/*");
        headers.put("Host", "mp.weixin.qq.com");
        headers.put("Connection", "keep-alive");
        Map<String, String> body = new HashMap<>();
        Request request = httpUtil.processRequest(url, lines, headers, body);

        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new HttpInterceptorr()).build();//拦截器;
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

                System.out.println(call);
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
//                String s1 = new String(URLDecoder.decode(response.body().bytes()));
//                System.out.println(s1);
                System.out.println(response.body().string());
//                byte[] bytes = response.body().bytes();
//                System.out.println(new String(bytes,Charset.forName("iso8859-1")));
//                System.out.println(new String(bytes, Charset.defaultCharset()));
//                System.out.println(response.code());
            }
        });
    }
}
