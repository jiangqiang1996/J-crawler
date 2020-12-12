package xin.jiangqiang.reflect;

import lombok.AllArgsConstructor;
import xin.jiangqiang.annotation.Match;
import xin.jiangqiang.config.Config;
import xin.jiangqiang.entities.Page;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@AllArgsConstructor
public class ReflectHelper {
    private Config config;

    public Object callMethod(Page page, String classString, Class<? extends Annotation> clazz, Object[] args) throws Exception {
        Method[] methods = ReflectHelper.class.getClassLoader().loadClass(classString).getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(clazz)) {
                return method.invoke(Class.forName(classString).getDeclaredConstructor().newInstance(), args);
            }
        }
        return null;
    }

    public Object callMatchMethod(Page page, String classString, Class<? extends Annotation> clazz, Object[] args) throws Exception {
        Method[] methods = ReflectHelper.class.getClassLoader().loadClass(classString).getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(clazz)) {
                Match match = (Match) method.getAnnotation(clazz);
                if (match.value().equals(page.getType())) {
                    return method.invoke(Class.forName(classString).getDeclaredConstructor().newInstance(), args);
                }
            }
        }
        return null;
    }
}
