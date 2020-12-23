package xin.jiangqiang.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 处理每一个Page对象，公共逻辑，
 * 和match的区别是：会匹配任意对象
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Deal {

}
