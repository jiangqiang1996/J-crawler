package top.jiangqiang.sample;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"top.jiangqiang.sample"})
public class StartApp {
    public static void main(String[] args) {
        SpringApplication.run(StartApp.class, args);
    }
}
