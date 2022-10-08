package top.jiangqiang.core.util;

import cn.hutool.core.collection.CollUtil;
import top.jiangqiang.core.base.BaseException;

import java.util.Map;

/**
 * @author Jiangqiang
 * @version 1.0
 * @description
 * @date 2022/9/30 13:45
 */
public class SpringUtil extends cn.hutool.extra.spring.SpringUtil {
    public static <T> T getOneBean(Class<T> clazz) {
        Map<String, T> map;
        try {
            map = getBeansOfType(clazz);
        } catch (Exception e) {
            throw new BaseException(e);
        }
        if (CollUtil.isNotEmpty(map)) {
            for (Map.Entry<String, T> entry : map.entrySet()) {
                return entry.getValue();
            }
        }
        throw new BaseException("没有获取到对应的bean" + clazz.getName());
    }

    public static <T> T getOneBeanDefault(Class<T> clazz, T defaultValue) {
        try {
            return getOneBean(clazz);
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
