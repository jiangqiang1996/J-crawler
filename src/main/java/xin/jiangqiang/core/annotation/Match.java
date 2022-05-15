package xin.jiangqiang.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 针对匹配的页面进行处理
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Match {
    String value() default "";//匹配页面标签

    String regEx() default "";//正则匹配URL

    String type() default "";//匹配页面类型

    String code() default "";//匹配响应码

}
