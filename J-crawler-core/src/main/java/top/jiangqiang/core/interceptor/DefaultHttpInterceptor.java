package top.jiangqiang.core.interceptor;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author jiangqiang
 * @date 2020/12/14 15:51
 */
@Slf4j
public class DefaultHttpInterceptor implements Interceptor {

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
//        log.debug("URL:\n" + request.url());

        log.debug("RequestHeaders:\n" + request.headers());

        RequestBody requestBody = request.body();
        String requestContent = null;
        if (requestBody != null) {
            requestContent = requestBody.toString();
        }
        log.debug("RequestBody:\n" + requestContent);

//        log.debug("ResponseHeaders:\n" + response.headers());

        ResponseBody body = response.body();
        String responseContent = null;
        if (body != null) {
            responseContent = body.string();
        }
        log.debug("ResponseBody:\n" + responseContent);
        return response;
    }
}
