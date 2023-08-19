package top.jiangqiang.crawler.sample;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import top.jiangqiang.crawler.app.AbstractStarter;
import top.jiangqiang.crawler.entities.BaseCrawler;
import top.jiangqiang.crawler.recorder.RamRecorder;

import java.util.List;

@Slf4j
public class Sample {
    public static void main(String[] args) {
        RamRecorder<BaseCrawler> ramRecorder = new RamRecorder<BaseCrawler>();
        ramRecorder.add(new BaseCrawler("https://candinya.com/") {
        });

        new AbstractStarter<>() {
            @Override
            public List<BaseCrawler> getCrawlers() {
                return ramRecorder.get();
            }

            @Override
            public void doResponse(BaseCrawler crawler, Response response) {

            }

            @Override
            public void doFailure(BaseCrawler crawler, Exception ioException) {

            }
        }.start();
    }


}
