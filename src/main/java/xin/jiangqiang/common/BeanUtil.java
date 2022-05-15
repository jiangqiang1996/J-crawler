package xin.jiangqiang.common;

import java.io.*;
import java.util.Map;

/**
 * @Author: JiangQiang
 * @Date: 2022年05月15日 09:25
 */
public class BeanUtil {
    public static <T extends Object> T clone(T obj) {
        T cloneObj = null;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos);
             ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            oos.writeObject(obj);
            oos.close();
            cloneObj = (T) ois.readObject();
            ois.close();
            return cloneObj;
        } catch (Exception e) {
            return cloneObj;
        }

    }

}
