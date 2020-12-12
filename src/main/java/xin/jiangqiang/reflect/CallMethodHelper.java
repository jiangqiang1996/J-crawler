package xin.jiangqiang.reflect;

import lombok.AllArgsConstructor;
import xin.jiangqiang.annotation.App;
import xin.jiangqiang.config.Config;
import xin.jiangqiang.entities.Next;
import xin.jiangqiang.entities.Page;

import java.lang.annotation.Annotation;
import java.util.List;

@AllArgsConstructor
public class CallMethodHelper {
    Config config;

    public Object before() {
        return call(null, xin.jiangqiang.annotation.Before.class);
    }

    public Object after() {
        return call(null, xin.jiangqiang.annotation.After.class);
    }

    public Object deal(Page page, Next next) {
        return call(page, xin.jiangqiang.annotation.Deal.class, page, next);
    }

    public Object match(Page page, Next next) {
        return callMatchMethod(page, xin.jiangqiang.annotation.Match.class, page, next);
    }

    public Object call(Page page, Class<? extends Annotation> cla, Object... args) {
        ReflectHelper reflectHelper = new ReflectHelper(config);
        //获取指定包名下的所有类（可根据注解进行过滤）
        List<Class<?>> classListByAnnotation = ClassUtil.getClassListByAnnotation(config.getPackageName(), App.class);
        for (Class<?> clazz : classListByAnnotation) {
            try {
                return reflectHelper.callMethod(page, clazz.getName(), cla, args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Object callMatchMethod(Page page, Class<? extends Annotation> cla, Object... args) {
        ReflectHelper reflectHelper = new ReflectHelper(config);
        //获取指定包名下的所有类（可根据注解进行过滤）
        List<Class<?>> classListByAnnotation = ClassUtil.getClassListByAnnotation(config.getPackageName(), App.class);
        for (Class<?> clazz : classListByAnnotation) {
            try {
                return reflectHelper.callMatchMethod(page, clazz.getName(), cla, args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
