package top.jiangqiang.crawler.log;

import lombok.extern.slf4j.Slf4j;
import okhttp3.logging.HttpLoggingInterceptor;

@Slf4j
public class HttpLogger implements HttpLoggingInterceptor.Logger {
    @Override
    public void log(String s) {
        if (s.length() > 300) {
            log.debug("\n" + s);
        } else {
            log.debug(s);
        }
    }

}