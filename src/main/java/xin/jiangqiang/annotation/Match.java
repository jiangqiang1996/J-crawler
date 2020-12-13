package xin.jiangqiang.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 处理每一个Page对象，值和Crawler.type相同时的执行逻辑。
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Match {
    String value() default "";
    String type() default "";


    String code() default "";

    String regEx() default "";

}
