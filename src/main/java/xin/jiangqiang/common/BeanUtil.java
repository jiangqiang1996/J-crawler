package xin.jiangqiang.common;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;

/**
 * @author jiangqiang
 * @date 2022-05-27
 */
public class BeanUtil<T> {
    /**
     * 深克隆对象
     *
     * @param obj
     * @param <T>
     * @return
     */
    public static <T> T cloneObj(T obj) {
        TypeReference<T> typeReference = new TypeReference<>() {
        };
        return JSONUtil.parseObj(JSONUtil.toJsonStr(obj)).toBean(typeReference.getType());
    }
}
