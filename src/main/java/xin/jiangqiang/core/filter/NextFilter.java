package xin.jiangqiang.core.filter;

import cn.hutool.core.lang.Singleton;
import xin.jiangqiang.core.config.Config;
import xin.jiangqiang.core.entities.Crawler;
import xin.jiangqiang.core.recoder.Recorder;

import java.util.Set;

/**
 * 过滤爬虫
 */
public class NextFilter implements Filter {
    Config config = Singleton.get(Config.class);
    Recorder recorder = Singleton.get(config.getRecorderClass());

    @Override
    public void filter(Set<Crawler> crawlerSet) {
        for (Crawler crawler : crawlerSet) {
            if (!recorder.exist(crawler)) {
                recorder.add(crawler);
            }
        }

    }
}
