package top.jiangqiang.sample.jiangqiang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * spring.factories注入的顺序在扫描注入之后，所以正常初始化顺序是；
 * 直接扫描的包>通过spring.factories直接注入的包>spring.factories注入的包再进行扫描出来的包
 * 部分控制注入顺序的注解，例如@AutoConfigureBefore、@AutoConfigureAfter、@AutoConfigureOrder都只能控制spring.factories直接注入的包的顺序
 * <a href="https://blog.csdn.net/hanxiaotongtong/article/details/107196905"/>
 * ${qy.info.base-package}.module包下的初始化是最后执行
 */
//@SuppressWarnings("SpringComponentScan") // 忽略 IDEA 无法识别 ${qy.info.base-package}
//@SpringBootApplication(scanBasePackages = {"${qy.info.base-package}.server.config", "${qy.info.base-package}.framework"})
@SpringBootApplication(scanBasePackages = {"top.jiangqiang.sample"})
@EnableCaching
public class StartApp {
    public static void main(String[] args) {
        SpringApplication.run(StartApp.class, args);
    }
}
