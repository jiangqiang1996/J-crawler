package top.jiangqiang.core.interceptor;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.Buffer;
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
        return chain.proceed(chain.request());
    }

}
