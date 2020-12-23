package xin.jiangqiang.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 处理每一个Page对象，值和Crawler.type相同时的执行逻辑。
 * 正则表达式，响应码，为空时不会进行正则表达式以及响应码的匹配
 * 只有类型为空时会匹配
 * match什么都不写也会匹配page.type为空的
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Match {
    String value() default "";

    String type() default "";

    String code() default "";

    String regEx() default "";

}
