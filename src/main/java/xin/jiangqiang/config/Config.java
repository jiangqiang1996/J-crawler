package xin.jiangqiang.config;

import lombok.Data;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Data
public class Config {
    Charset charset = Charset.defaultCharset();
    Integer threads = 50;
    Integer depth = 4;
    String packageName;
    List<String> regExs = new ArrayList<>();

    public void addRegEx(String regEx) {
        regExs.add(regEx);
    }
}
