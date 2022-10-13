package top.jiangqiang.crawler.core.sample;

import top.jiangqiang.crawler.core.entities.Crawler;
import top.jiangqiang.crawler.core.util.JSONUtil;

/**
 * @author Jiangqiang
 * @version 1.0
 * @description TODO
 * @date 2022/10/13 11:31
 */
public class TestJson {
    public static void main(String[] args) {
        Crawler crawler = JSONUtil.parse("""
                {
                  "@type": "top.jiangqiang.crawler.core.entities.Crawler",
                  "body": {
                    "@type": "java.util.HashMap"
                  },
                  "crawlers": [],
                  "depth": 3,
                  "headers": {
                    "@type": "java.util.HashMap",
                    "referer": "https://www.pixiv.net/artworks/101827111",
                    "Accept-Encoding": "identity"
                  },
                  "httpConfig": {
                    "body": {
                      "@type": "java.util.HashMap"
                    },
                    "headers": {
                      "@type": "java.util.HashMap",
                      "referer": "https://www.pixiv.net/artworks/101827111",
                      "Accept-Encoding": "identity"
                    },
                    "lines": {
                      "@type": "java.util.HashMap"
                    },
                    "proxyConfig": {
                      "@type": "java.util.HashMap"
                    }
                  },
                  "lines": {
                    "@type": "java.util.HashMap"
                  },
                  "proxyConfig": {
                    "@type": "java.util.HashMap"
                  },
                  "url": "https://www.pixiv.net/artworks/10182711"
                }

                                """, Crawler.class);
        System.out.println(JSONUtil.toJsonPrettyStr(crawler));
    }
}
