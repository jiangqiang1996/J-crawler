package xin.jiangqiang.reflect;

import lombok.AllArgsConstructor;
import xin.jiangqiang.annotation.Match;
import xin.jiangqiang.config.Config;
import xin.jiangqiang.entities.Page;
import xin.jiangqiang.util.RegExpUtil;
import xin.jiangqiang.util.StringUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@AllArgsConstructor
public class ReflectHelper {
    private Config config;

    public void callMethod(Page page, String classString, Class<? extends Annotation> clazz, Object[] args) throws Exception {
        Method[] methods = ReflectHelper.class.getClassLoader().loadClass(classString).getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(clazz)) {
                method.invoke(Class.forName(classString).getDeclaredConstructor().newInstance(), args);
            }
        }
    }

    /**
     * 执行match注解修饰的方法
     *
     * @param page        参数
     * @param classString 方法所属全类名
     * @param clazz       注解的class
     * @param args        参数
     * @throws Exception
     */
    public void callMatchMethod(Page page, String classString, Class<? extends Annotation> clazz, Object[] args) throws Exception {
        Method[] methods = ReflectHelper.class.getClassLoader().loadClass(classString).getMethods();
        for (Method method : methods) {
            /**
             * 保证每一个方法只会执行一次，
             * 满足正则表达式，响应码，或者类型匹配时均会执行
             */
            if (method.isAnnotationPresent(clazz)) {
                Match match = (Match) method.getAnnotation(clazz);
                //响应码匹配
                if (StringUtil.isNotEmpty(match.code()) && Integer.valueOf(match.code()).equals(page.getResponseCode())) {
                    method.invoke(Class.forName(classString).getDeclaredConstructor().newInstance(), args);
                    continue;
                }
                //正则表达式匹配时执行
                if (StringUtil.isNotEmpty(match.regEx()) && RegExpUtil.isMatch(page.getUrl(), match.regEx())) {
                    method.invoke(Class.forName(classString).getDeclaredConstructor().newInstance(), args);
                    continue;
                }
                //类型匹配时执行
                if (match.value().equals(page.getType()) || match.type().equals(page.getType())) {
                    method.invoke(Class.forName(classString).getDeclaredConstructor().newInstance(), args);
                }
            }
        }
    }
}
