package top.jiangqiang.sample.jiangqiang.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 爬取之前执行，用于定义初始化配置，执行方式在init方法之后，如果要实现续爬功能应该重写init方法
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Before {

}
