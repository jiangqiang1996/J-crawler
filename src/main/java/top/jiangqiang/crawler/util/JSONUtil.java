package top.jiangqiang.crawler.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import top.jiangqiang.crawler.exception.BaseException;

/**
 * @author Jiangqiang
 * @version 1.0
 * @description jackson实现的json解析工具类
 * @date 2022/8/25 10:12
 */
@Slf4j
public class JSONUtil {
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final static ObjectMapper OBJECT_MAPPER_WITH_CLASS = new ObjectMapper();

    static {
        init(OBJECT_MAPPER);
        init(OBJECT_MAPPER_WITH_CLASS);
        //序列化和反序列化带类型信息
        OBJECT_MAPPER_WITH_CLASS.activateDefaultTyping(OBJECT_MAPPER_WITH_CLASS.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_ARRAY);
    }

    /**
     * 初始化ObjectMapper通用配置
     *
     * @param objectMapper ObjectMapper
     */
    private static void init(ObjectMapper objectMapper) {
        //忽略未知属性
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        //忽略null的属性
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * 转为JSON字符串，格式化输出
     *
     * @param object
     * @return JSON字符串
     */
    public static String toJsonPrettyStr(Object object) {
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new BaseException(e);
        }
    }

    /**
     * 转为JSON字符串
     *
     * @param object
     * @return JSON字符串
     */
    public static String toJsonStr(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new BaseException(e);
        }
    }


    /**
     * @param jsonStr json字符串
     * @param tClass  class
     * @param <T>     指定类型
     * @return json转对象
     */
    public static <T> T parse(String jsonStr, Class<T> tClass) {
        try {
            return OBJECT_MAPPER.readValue(jsonStr, tClass);
        } catch (JsonProcessingException e) {
            throw new BaseException(e);
        }
    }

    /**
     * 序列化时保留类型信息
     *
     * @param object
     * @return
     */
    public static String toJsonStrWithClass(Object object) {
        try {
            return OBJECT_MAPPER_WITH_CLASS.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new BaseException(e);
        }
    }

    /**
     * 如果序列化时保留了类型信息，反序列化时可以直接使用此方法，然后强转
     *
     * @param jsonStr json
     * @return
     */
    public static Object parse(String jsonStr) {
        try {
            return OBJECT_MAPPER_WITH_CLASS.readValue(jsonStr, Object.class);
        } catch (JsonProcessingException e) {
            throw new BaseException(e);
        }
    }
}
