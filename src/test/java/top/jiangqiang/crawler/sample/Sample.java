package top.jiangqiang.crawler.sample;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import top.jiangqiang.crawler.app.AbstractStarter;
import top.jiangqiang.crawler.recorder.RamRecorder;

import java.io.IOException;
import java.util.List;

@Slf4j
public class Sample {
    public static void main(String[] args) {
        RamRecorder<Crawler> ramRecorder = new RamRecorder<Crawler>();
        ramRecorder.add(new Crawler("https://candinya.com/"));
        new AbstractStarter<Crawler>() {
            @Override
            public List<Crawler> getCrawlers() {
                return ramRecorder.get();
            }

            @Override
            public void doResponse(Crawler crawler, Response response) {
                if (response.code() == 200) {
                    try (ResponseBody body = response.body();
                    ) {
                        //处理响应内容，解析dom，获取新的种子
                        String string = body.string();
                        System.out.println(string);

                        //解析出的新种子
                        //Crawler baseCrawler = new Crawler("https://www.baidu.com/");
                        //baseCrawler.setSourceCrawler(crawler);
                        //ramRecorder.add(baseCrawler);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void doFailure(Crawler crawler, Exception ioException) {

            }
        }.start();
    }

}
