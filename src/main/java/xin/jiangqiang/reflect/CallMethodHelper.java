package xin.jiangqiang.reflect;

import cn.hutool.core.lang.Singleton;
import cn.hutool.core.util.ClassLoaderUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import xin.jiangqiang.common.RegExpUtil;
import xin.jiangqiang.common.StringUtil;
import xin.jiangqiang.core.config.Config;
import xin.jiangqiang.core.entities.Page;
import xin.jiangqiang.core.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@AllArgsConstructor
public class CallMethodHelper {

    public void before() {
        callMethod(Singleton.get(Config.class).getAppClass().getName(), Before.class);
    }

    public void after() {
        callMethod(Singleton.get(Config.class).getAppClass().getName(), After.class);
    }

    public void deal(Page page) {
        callMethod(Singleton.get(Config.class).getAppClass().getName(), Deal.class, page);
    }

    public void match(Page page) {
        callMatchMethod(page, Match.class, page);
    }

    /**
     * @param page 获取type和match的type值比较
     * @param cla  执行该类上注解对应的方法
     * @param args 方法的参数
     */
    public void callMatchMethod(Page page, Class<? extends Annotation> cla, Object... args) {
        try {
            callMatchMethod(page, Singleton.get(Config.class).getAppClass().getName(), cla, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行指定类中所有带match注解修饰的方法，并且会匹配match的值
     *
     * @param page        爬虫对象
     * @param classString 方法所属全类名
     * @param clazz       注解的class
     * @param args        参数
     */
    public void callMatchMethod(Page page, String classString, Class<? extends Annotation> clazz, Object... args) {
        Class<?> aClass = ClassLoaderUtil.loadClass(classString);
        Method[] methods = aClass.getMethods();
        try {
            Object o = aClass.getDeclaredConstructor().newInstance();
            for (Method method : methods) {
                if (method.isAnnotationPresent(clazz)) {
                    Match match = (Match) method.getAnnotation(clazz);
                    //match上的各种属性值和type全部为空则执行，并跳出当前循环
                    if (StringUtil.isEmpty(match.code(), match.value(), match.regEx(), match.type(), page.getType())) {
                        method.invoke(o, args);
                        continue;
                    }
                    //响应码匹配
                    if (StrUtil.isNotEmpty(match.code()) && Integer.valueOf(match.code()).equals(page.getResponseCode())) {
                        method.invoke(o, args);
                        continue;
                    }
                    //正则表达式匹配时执行
                    if (StrUtil.isNotEmpty(match.regEx()) && RegExpUtil.isMatch(page.getUrl(), match.regEx())) {
                        method.invoke(o, args);
                        continue;
                    }
                    //类型匹配时执行
                    if ((StrUtil.isNotEmpty(match.value()) && match.value().equals(page.getType())) ||
                            (StrUtil.isNotEmpty(match.type()) && match.type().equals(page.getType()))) {
                        method.invoke(o, args);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行指定类中所有带有指定类型注解的方法
     *
     * @param classString 需要执行方法的类名
     * @param clazz       方法注解的class
     * @param args        执行方法的参数
     */
    public void callMethod(String classString, Class<? extends Annotation> clazz, Object... args) {
        Class<?> aClass = ClassLoaderUtil.loadClass(classString);
        Method[] methods = aClass.getMethods();
        try {
            Object o = aClass.getDeclaredConstructor().newInstance();
            for (Method method : methods) {
                if (method.isAnnotationPresent(clazz)) {
                    method.invoke(o, args);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
