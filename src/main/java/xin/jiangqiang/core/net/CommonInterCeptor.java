package xin.jiangqiang.core.net;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author jiangqiang
 * @date 2020/12/14 15:51
 */
@Slf4j
public class CommonInterCeptor implements Interceptor {

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        log.debug("URL:\n" + request.url());
        log.debug("RequestHeaders:\n" + request.headers());
        log.debug("RequestBody:\n" + request.body());
        log.debug("ResponseHeaders:\n" + response.headers());
        log.debug("RequestBody:\n" + response.body());
        return response;
    }
}
