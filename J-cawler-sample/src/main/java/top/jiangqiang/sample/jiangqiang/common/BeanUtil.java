package top.jiangqiang.sample.jiangqiang.common;


import top.jiangqiang.util.JSONUtil;

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
        return JSONUtil.parse(JSONUtil.toJsonStr(obj), obj);
    }
}
