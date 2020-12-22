package xin.jiangqiang.reflect;

import lombok.AllArgsConstructor;
import xin.jiangqiang.annotation.App;
import xin.jiangqiang.config.Config;
import xin.jiangqiang.entities.Page;

import java.lang.annotation.Annotation;

@AllArgsConstructor
public class CallMethodHelper {
    Config config;

    public void before() {
        call(xin.jiangqiang.annotation.Before.class);
    }

    public void after() {
        call(xin.jiangqiang.annotation.After.class);
    }

    public void deal(Page page) {
        call(xin.jiangqiang.annotation.Deal.class, page);
    }

    public void match(Page page) {
        callMatchMethod(page, xin.jiangqiang.annotation.Match.class, page);
    }

    public void call(Class<? extends Annotation> cla, Object... args) {
        ReflectHelper reflectHelper = new ReflectHelper(config);
        //获取指定包名下的所有类（可根据注解进行过滤）
        try {
            reflectHelper.callMethod(config.getAppClass().getName(), cla, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param page 获取type和match的type值比较
     * @param cla  执行该类上注解对应的方法
     * @param args 方法的参数
     */
    public void callMatchMethod(Page page, Class<? extends Annotation> cla, Object... args) {
        ReflectHelper reflectHelper = new ReflectHelper(config);
        try {
            reflectHelper.callMatchMethod(page, config.getAppClass().getName(), cla, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
