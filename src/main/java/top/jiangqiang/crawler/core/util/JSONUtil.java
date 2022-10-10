package top.jiangqiang.crawler.core.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Jiangqiang
 * @version 1.0
 * @description fastjson2实现的json解析工具类
 * @date 2022/8/25 10:12
 */
@Slf4j
public class JSONUtil {

    /**
     * 转为JSON字符串，格式化输出
     *
     * @param object
     * @return JSON字符串
     */
    public static String toJsonPrettyStr(Object object, JSONWriter.Feature... features) {
        ArrayList<JSONWriter.Feature> tmpFeatures = new ArrayList<>(Arrays.stream(features).toList());
        tmpFeatures.add(JSONWriter.Feature.PrettyFormat);
        return JSON.toJSONString(object, tmpFeatures.toArray(new JSONWriter.Feature[0]));
    }

    /**
     * 转为JSON字符串
     *
     * @param object
     * @return JSON字符串
     */
    public static String toJsonStr(Object object, JSONWriter.Feature... features) {
        return JSON.toJSONString(object, features);
    }

    /**
     * 转换为json字符串，带类型
     *
     * @param object
     * @param features
     * @return
     */
    public static String toJsonStrWithClass(Object object, JSONWriter.Feature... features) {
        ArrayList<JSONWriter.Feature> tmpFeatures = new ArrayList<>(Arrays.stream(features).toList());
        tmpFeatures.add(JSONWriter.Feature.WriteClassName);
        return JSON.toJSONString(object, tmpFeatures.toArray(new JSONWriter.Feature[0]));
    }

    /**
     * 解析为指定的类型，如果解析为集合，并且json字符串不附带类型信息时，集合内部全是JSONObject
     *
     * @param jsonStr 字符串
     * @param tClass  类型
     * @return
     */
    public static <T> T parse(String jsonStr, Class<T> tClass, JSONReader.Feature... features) {
        ArrayList<JSONReader.Feature> tmpFeatures = new ArrayList<>(Arrays.stream(features).toList());
        tmpFeatures.add(JSONReader.Feature.SupportAutoType);
        return JSON.parseObject(jsonStr, (Type) tClass, tmpFeatures.toArray(JSONReader.Feature[]::new));
    }

    /**
     * 解析为指定的类型，如果解析为集合，并且json字符串不附带类型信息时，集合内部全是JSONObject
     *
     * @param jsonStr
     * @param features
     * @return
     */
    public static <T> T parse(String jsonStr, T t, JSONReader.Feature... features) {
        ArrayList<JSONReader.Feature> tmpFeatures = new ArrayList<>(Arrays.stream(features).toList());
        tmpFeatures.add(JSONReader.Feature.SupportAutoType);
        return JSON.parseObject(jsonStr, (Type) t.getClass(), tmpFeatures.toArray(JSONReader.Feature[]::new));
    }

    /**
     * 解析为JSONObject或JSONArray，如果原本附带类型时会转换为具体类型
     *
     * @param jsonStr
     * @param features
     * @return
     */
    public static Object parse(String jsonStr, JSONReader.Feature... features) {
        ArrayList<JSONReader.Feature> tmpFeatures = new ArrayList<>(Arrays.stream(features).toList());
        tmpFeatures.add(JSONReader.Feature.SupportAutoType);
        return JSON.parseObject(jsonStr, Object.class, tmpFeatures.toArray(JSONReader.Feature[]::new));
    }
}
