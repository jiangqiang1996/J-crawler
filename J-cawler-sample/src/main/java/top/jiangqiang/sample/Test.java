package top.jiangqiang.sample;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import top.jiangqiang.core.app.GenericStarter;
import top.jiangqiang.core.config.CrawlerGlobalConfig;
import top.jiangqiang.core.entities.Crawler;
import top.jiangqiang.core.entities.Page;
import top.jiangqiang.core.handler.ResultHandler;
import top.jiangqiang.core.recorder.RamRecorder;
import top.jiangqiang.core.recorder.Recorder;
import top.jiangqiang.core.util.FileUtil;
import top.jiangqiang.core.util.JSONUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Jiangqiang
 * @version 1.0
 * @description TODO
 * @date 2022/10/10 9:39
 */
@Slf4j
public class Test {
    private void fetchPicture1() {
        RamRecorder ramRecorder = new RamRecorder();
        CrawlerGlobalConfig crawlerGlobalConfig = new CrawlerGlobalConfig();
        //下面写法会在任务开始前执行
        ramRecorder.setInitCallback(recorder -> {
            /**
             * 这里可以从数据库中读取上次保存的种子。。。
             */
            //https://www.pixiv.net/artworks/101741272
            /**
             * mode 按天或按周 daily按天
             * content illust
             * date 按天时的日期
             * format 返回的格式
             * p 页数 1-10
             * https://www.pixiv.net/ranking.php?mode=daily&content=illust&date=20221007&p=1&format=json
             */
            Date date = new Date();
            int days = 30;
            for (int day = 1; day <= days; day++) {
                String dateStr = DateUtil.format(date, DatePattern.PURE_DATE_PATTERN);
                date = DateUtil.offsetDay(date, -1);
                int pages = 10;
                for (int page = 1; page <= pages; page++) {
                    String url;
                    if (day == 1) {
                        url = "https://www.pixiv.net/ranking.php?mode=daily&content=illust" + "&p=" + page + "&format=json";
                    } else {
                        url = "https://www.pixiv.net/ranking.php?mode=daily&content=illust&date=" + dateStr + "&p=" + page + "&format=json";
                    }
//                    System.out.println(url);
                    Crawler crawler = new Crawler(url);
                    recorder.add(crawler);
                }
            }
        });
//        crawlerGlobalConfig.addRegEx("(http|https)://.*");
        crawlerGlobalConfig.setAllowEnd(true);
        crawlerGlobalConfig.setForceEnd(false);
        crawlerGlobalConfig.setDepth(3);
        crawlerGlobalConfig.setMaxSize((long) 1024 * 1024);
        crawlerGlobalConfig.setUseProxy(true);
        crawlerGlobalConfig.addProxyIp("127.0.0.1");
        crawlerGlobalConfig.addProxyPort("7890");
        crawlerGlobalConfig.addProxyProtocol("HTTP");
        crawlerGlobalConfig.setLogLevel(HttpLoggingInterceptor.Level.BODY);
        new GenericStarter(crawlerGlobalConfig, ramRecorder, new ResultHandler() {
            public Set<Crawler> doSuccess(Recorder recorder, Crawler crawler, Page page, Response response) {
                // 储存下载文件的目录
                File dir = FileUtil.file("D:/cache/cache");
//                FileUtil.downloadFile(page, response, dir.getAbsolutePath());
                String mimeType = FileUtil.subMimeType(page.getContentType());
                if ("application/json".equals(mimeType) && page.getUrl().startsWith("https://www.pixiv.net/ranking.php")) {
                    String content = page.getContent();
                    if (StrUtil.isNotBlank(content)) {
                        HashMap<String, Object> hashMap = JSONUtil.parse(content, new HashMap<>());
                        JSONArray contents = (JSONArray) hashMap.get("contents");
                        if (CollUtil.isNotEmpty(contents)) {
                            List<JSONObject> jsonObjects = contents.stream().map(o -> (JSONObject) o).toList();
                            List<String> urlList = jsonObjects.stream().map(jsonObject -> {
                                Object illust_id = jsonObject.get("illust_id");
                                if (illust_id != null) {
                                    return "https://www.pixiv.net/artworks/" + illust_id;
                                } else return null;
                            }).filter(StrUtil::isNotBlank).toList();
                            page.addSeeds(urlList);
                        }
                    }

                }
                if (page.getUrl().startsWith("https://www.pixiv.net/artworks/")) {
//                    "original":"https://i.pximg.net/img-original/img/2022/10/05/12/00/01/101699259_p0.png";

                    String regEx = """
                                  "original":"https://[\\w./-]+
                            """;
                    String group0 = ReUtil.getGroup0(regEx.trim(), page.getContent());
                    if (StrUtil.isNotBlank(group0)) {
                        String url = StrUtil.subAfter(group0, "\"original\":\"", false);
                        page.addSeed(url);
                    }
                }
                if (page.getUrl().startsWith("https://i.pximg.net/img-original/img/") && mimeType.startsWith("image")) {
                    FileUtil.downloadFile(page, response, dir.getAbsolutePath());
                }
                return page.getCrawlers();
            }
        }).start();
    }

    public static void main(String[] args) {
        String regEx = """
                "original":"https://[\\w./-]+
                   """;
        String group0 = ReUtil.getGroup0(regEx.trim(), """
                                
                <!DOCTYPE html>
                <html lang="zh-CN"xmlns:wb="http://open.weibo.com/wb"><head><meta name="viewport" content="width=1366"><link rel="shortcut icon"  href="https://www.pixiv.net/favicon.ico"><title>#女の子 ♥ - Nekojira的插画 - pixiv</title><link rel="canonical" href="https://www.pixiv.net/artworks/101739861"><link rel="alternate" hreflang="ja" href="https://www.pixiv.net/artworks/101739861"><link rel="alternate" hreflang="en" href="https://www.pixiv.net/en/artworks/101739861"><meta property="twitter:card" content="summary_large_image"><meta property="twitter:site" content="@pixiv"><meta property="twitter:url" content="https://www.pixiv.net/artworks/101739861?ref=twitter_photo_card"><meta property="twitter:title" content="♥"><meta property="twitter:description" content="♥ by Nekojira"><meta property="twitter:image" content="https://embed.pixiv.net/artwork.php?illust_id=101739861"><meta property="twitter:app:name:iphone" content="pixiv"><meta property="twitter:app:id:iphone" content="337248563"><meta property="twitter:app:url:iphone" content="pixiv://illusts/101739861"><meta property="twitter:app:name:ipad" content="pixiv"><meta property="twitter:app:id:ipad" content="337248563"><meta property="twitter:app:url:ipad" content="pixiv://illusts/101739861"><meta property="twitter:app:name:googleplay" content="pixiv"><meta property="twitter:app:id:googleplay" content="jp.pxv.android"><meta property="twitter:app:url:googleplay" content="pixiv://illusts/101739861"><meta property="og:site_name" content="pixiv"><meta property="fb:app_id" content="140810032656374"><meta property="og:title" content="#女の子 ♥ - Nekojira的插画 - pixiv"><meta property="og:type" content="article"><meta property="og:image" content="https://embed.pixiv.net/artwork.php?illust_id=101739861"><meta property="og:description" content=""><meta name="robots" content="max-image-preview:large"><meta name="description" content="この作品 「♥」 は 「女の子」「白髪」 等のタグがつけられた「Nekojira」さんのイラストです。"><script async src="https://stats.g.doubleclick.net/dc.js"></script><script>var _gaq = _gaq || [];_gaq.push(['_setAccount', 'UA-1830249-3']);_gaq.push(['_setDomainName', 'pixiv.net']);_gaq.push(['_setCustomVar', 1, 'login', 'yes', 3]);_gaq.push(['_setCustomVar', 3, 'plan', 'normal', 1]);_gaq.push(['_setCustomVar', 5, 'gender', 'male', 1]);_gaq.push(['_setCustomVar', 6, 'user_id', "60903890", 1]);_gaq.push(['_setCustomVar', 11, 'lang', "zh", 1]);_gaq.push(['_setCustomVar', 12, 'illustup_flg', 'not_uploaded', 3]);_gaq.push(['_setCustomVar', 13, 'user_id_per_pv', "60903890", 3]);_gaq.push(['_setCustomVar', 27, 'p_ab_d_id', "2134615577", 3]);_gaq.push(['_setCustomVar', 29, 'default_service_is_touch', 'no', 3]);</script><meta id="meta-pixiv-tests" name="pixiv-tests" content='{"accounts_pigya_apple":true,"accounts_pigya_facebook":true,"accounts_pigya_google":true,"accounts_pigya_twitter":true,"accounts_pigya_weibo":true,"ads_aps_email_hash":true,"anniversary15_guideline_release":true,"anniversary15_release":true,"ab_illlust_series_spa_dev":true,"ab_illlust_series_mobile_spa_dev":true,"profile_genre_page_novel_series_component":true,"illust_reply_tree":true,"ab_manga_new_viewer":true,"nagisa":true,"novel_12th_premium_covers":true,"novel_never_hide_overlay_ads_on_viewer_h100":true,"touch_novel_follow_watchlist_tab":true,"recommend_tutorial_20191213":true,"show_age_on_profile":true,"show_prefecture_on_profile":true,"touch_top_jack":true,"touch_premium_popular_search_modal":true,"www_profile_edit_spa":true,"toggles":{"chatbot_settings_page_enable":true,"toggle_illust_manga_reupload":true,"toggle_lemon_prepare":true,"toggle_lemon_stop_new":true,"toggle_lemon_stop_all":true,"toggle_manga_genre_tag":true,"toggle_manga_series_reserve":true,"toggle_novel_close_bungei":true,"toggle_novel_editors_choise":true,"toggle_novel_hide_overlay_ads_on_viewer":true,"toggle_novel_word_count":true,"set_language_at_upload":true,"comment_off":true}}'><link rel="stylesheet" href="https://s.pximg.net/www/js/build/vendors~spa.5bd0246d2d7aec9c9238.css" crossorigin="anonymous"><link rel="stylesheet" href="https://s.pximg.net/www/js/build/21.4af3be5ba6ffa2c78bdf.css" crossorigin="anonymous"><link rel="stylesheet" href="https://s.pximg.net/www/js/build/spa.84ddbf24e98807ddd31f.css" crossorigin="anonymous"><script src="https://s.pximg.net/www/js/build/runtime.60610e396d184534f8ee.js" charset="utf8" crossorigin="anonymous"defer></script><script src="https://s.pximg.net/www/js/build/vendors~spa.a7368647e31d4a8b0a81.js" charset="utf8" crossorigin="anonymous"defer></script><script src="https://s.pximg.net/www/js/build/21.5ef81f8e6f700c5e800f.js" charset="utf8" crossorigin="anonymous"defer></script><script src="https://s.pximg.net/www/js/build/spa.dfe0d03f6be5f5c4ee0f.js" charset="utf8" crossorigin="anonymous"defer></script><link rel="preload" as="image" href="https://i.pximg.net/img-master/img/2022/10/07/09/43/14/101739861_p0_master1200.jpg"><link rel="preload" as="script" href="https://s.pximg.net/www/js/build/moment-zh.5035186e5e3cecc5d0db.js" crossorigin="anonymous"><script>
                        console.log("%c"+"/* pixiv Bug Bounty Program */","color: #0096fa; font-weight: bold;");
                    console.log("We have a bug bounty program on HackerOne. \\nIf you find a vulnerability in our scope, please report it to us.");
                    console.log("https://hackerone.com/pixiv");
                </script><link rel="apple-touch-icon" sizes="180x180" href="https://s.pximg.net/common/images/apple-touch-icon.png?20200601"><link rel="manifest" href="/manifest.json"><link rel="alternate" type="application/json+oembed" href="https://embed.pixiv.net/oembed.php?url=https%3A%2F%2Fwww.pixiv.net%2Fartworks%2F101739861"><meta name="global-data" id="meta-global-data" content='{"token":"e164a7b3cc470e5e083d5509ca1c1d00","services":{"booth":"https://api.booth.pm","sketch":"https://sketch.pixiv.net","vroidHub":"https://hub.vroid.com","accounts":"https://accounts.pixiv.net/"},"oneSignalAppId":"b2af994d-2a00-40ba-b1fa-684491f6760a","publicPath":"https://s.pximg.net/www/js/build/","commonResourcePath":"https://s.pximg.net/common/","development":false,"userData":{"id":"60903890","pixivId":"user_grer8727","name":"随心","profileImg":"https://s.pximg.net/common/images/no_profile_s.png","profileImgBig":"https://s.pximg.net/common/images/no_profile.png","premium":false,"xRestrict":0,"adult":true,"safeMode":false,"illustCreator":false,"novelCreator":false},"adsData":null,"miscData":{"consent":{"gdpr":true},"policyRevision":false,"grecaptcha":{"recaptchaEnterpriseScoreSiteKey":"6LfF1dcZAAAAAOHQX8v16MX5SktDwmQINVD_6mBF"},"info":{"id":"8631","title":"已支持在投稿画面中选择最适合ToonScroll漫画的显示方式","createDate":"2022-10-07 15:00:00"},"isSmartphone":false},"premium":{"novelCoverReupload":true},"mute":[]}'><meta name="preload-data" id="meta-preload-data" content='{"timestamp":"2022-10-09T22:49:10+09:00","illust":{"101739861":{"illustId":"101739861","illustTitle":"♥","illustComment":"","id":"101739861","title":"♥","description":"","illustType":0,"createDate":"2022-10-07T00:43:14+00:00","uploadDate":"2022-10-07T00:43:14+00:00","restrict":0,"xRestrict":0,"sl":2,"urls":{"mini":"https://i.pximg.net/c/48x48/img-master/img/2022/10/07/09/43/14/101739861_p0_square1200.jpg","thumb":"https://i.pximg.net/c/250x250_80_a2/img-master/img/2022/10/07/09/43/14/101739861_p0_square1200.jpg","small":"https://i.pximg.net/c/540x540_70/img-master/img/2022/10/07/09/43/14/101739861_p0_master1200.jpg","regular":"https://i.pximg.net/img-master/img/2022/10/07/09/43/14/101739861_p0_master1200.jpg","original":"https://i.pximg.net/img-original/img/2022/10/07/09/43/14/101739861_p0.jpg"},"tags":{"authorId":"4526277","isLocked":false,"tags":[{"tag":"女の子","locked":true,"deletable":false,"userId":"4526277","translation":{"en":"女孩子"},"userName":"Nekojira"},{"tag":"白髪","locked":true,"deletable":false,"userId":"4526277","translation":{"en":"白发"},"userName":"Nekojira"},{"tag":"オリジナル","locked":true,"deletable":false,"userId":"4526277","translation":{"en":"原创"},"userName":"Nekojira"},{"tag":"巨乳","locked":true,"deletable":false,"userId":"4526277","translation":{"en":"large breasts"},"userName":"Nekojira"},{"tag":"ふともも","locked":false,"deletable":true,"translation":{"en":"大腿"}},{"tag":"足裏","locked":false,"deletable":true,"translation":{"en":"脚底"}},{"tag":"オリジナル10000users入り","locked":false,"deletable":true,"translation":{"en":"原创10000users加入书籤"}}],"writable":true},"alt":"#女の子 ♥ - Nekojira的插画","storableTags":["Lt-oEicbBr","7WfWkHyQ76","RTJMXD26Ak","5oPIfUbtd6","pnCQRVigpy","jk9IzfjZ6n","jH0uD88V6F"],"userId":"4526277","userName":"Nekojira","userAccount":"akiha1234","userIllusts":{"101739861":{"id":"101739861","title":"♥","illustType":0,"xRestrict":0,"restrict":0,"sl":2,"url":"https://i.pximg.net/c/250x250_80_a2/img-master/img/2022/10/07/09/43/14/101739861_p0_square1200.jpg","description":"","tags":["女の子","白髪","オリジナル","巨乳","ふともも","足裏","オリジナル10000users入り"],"userId":"4526277","userName":"Nekojira","width":3430,"height":2112,"pageCount":1,"isBookmarkable":true,"bookmarkData":null,"alt":"#女の子 ♥ - Nekojira的插画","titleCaptionTranslation":{"workTitle":null,"workCaption":null},"createDate":"2022-10-07T09:43:14+09:00","updateDate":"2022-10-07T09:43:14+09:00","isUnlisted":false,"isMasked":false},"99633552":{"id":"99633552","title":"Inner","illustType":0,"xRestrict":0,"restrict":0,"sl":6,"url":"https://i.pximg.net/c/250x250_80_a2/img-master/img/2022/07/10/19/24/03/99633552_p0_square1200.jpg","description":"","tags":["オリジナル","女の子","白髪","巨乳","背中","女悪魔","黒下着","お尻","オリジナル30000users入り"],"userId":"4526277","userName":"Nekojira","width":2453,"height":1670,"pageCount":4,"isBookmarkable":true,"bookmarkData":null,"alt":"#オリジナル Inner - Nekojira的插画","titleCaptionTranslation":{"workTitle":null,"workCaption":null},"createDate":"2022-07-10T19:24:03+09:00","updateDate":"2022-07-10T19:24:03+09:00","isUnlisted":false,"isMasked":false,"profileImageUrl":"https://i.pximg.net/user-profile/img/2021/09/02/00/03/49/21339747_704230a8a408159fcfac3a840a2e4e78_50.jpg"},"97045592":null,"95773977":null,"94188857":null,"93320570":null,"93223824":null,"93024895":null,"92374369":null,"91618774":null,"91049649":null,"90253529":null,"89293537":null,"88922153":null,"87739792":null,"87598712":null,"87406935":null,"86900691":null,"86438827":null,"84971563":null,"84815444":null,"83019039":null,"82397675":null,"81937964":null,"81003218":null,"80406182":null,"79610658":null,"79233102":null,"78848761":null,"78280769":null,"78136345":null,"77213682":null,"76909995":null,"75012683":null,"73996679":null,"72582344":null,"72003512":null,"70883250":null,"70709903":null,"70045722":null,"68924985":null,"67937112":null,"67451479":null,"67272091":null,"66365812":null,"64360541":null,"64183060":null,"64102039":null,"63498387":null,"62109837":null,"60189438":null,"58892748":null,"56782789":null,"56445352":null,"56401507":null,"49718591":null,"49293088":null,"48810191":null,"44918681":null},"likeData":false,"width":3430,"height":2112,"pageCount":1,"bookmarkCount":15419,"likeCount":11168,"commentCount":56,"responseCount":0,"viewCount":39978,"bookStyle":0,"isHowto":false,"isOriginal":true,"imageResponseOutData":[],"imageResponseData":[],"imageResponseCount":0,"pollData":null,"seriesNavData":null,"descriptionBoothId":null,"descriptionYoutubeId":null,"comicPromotion":null,"fanboxPromotion":{"userName":"Nekojira","userImageUrl":"https://i.pximg.net/user-profile/img/2021/09/02/00/03/49/21339747_704230a8a408159fcfac3a840a2e4e78_170.jpg","contentUrl":"https://www.pixiv.net/fanbox/creator/4526277?utm_campaign=www_artwork&amp;utm_medium=site_flow&amp;utm_source=pixiv","description":"こにちわ、Nekojiraです。\\r\\n白き女の子を作りたい....!","imageUrl":"https://pixiv.pximg.net/c/520x280_90_a2_g5/fanbox/public/images/creator/4526277/cover/cO0FmWb0ZGYb6cBywNzNR1Nt.jpeg","imageUrlMobile":"https://pixiv.pximg.net/c/520x280_90_a2_g5/fanbox/public/images/creator/4526277/cover/cO0FmWb0ZGYb6cBywNzNR1Nt.jpeg","hasAdultContent":true},"contestBanners":[],"isBookmarkable":true,"bookmarkData":null,"contestData":null,"zoneConfig":{"responsive":{"url":"https://pixon.ads-pixiv.net/show?zone_id=illust_responsive_side&amp;format=js&amp;s=1&amp;up=0&amp;a=25&amp;ng=w&amp;l=zh&amp;uri=%2Fartworks%2F_PARAM_&amp;ref=www.pixiv.net%2Fartworks%2F101739861&amp;is_spa=1&amp;K=84c28b3a151d2&amp;ab_test_digits_first=19&amp;uab=31&amp;yuid=hgMjJoA&amp;suid=Ph6zdipfehg9njqt2&amp;num=6342d156445"},"rectangle":{"url":"https://pixon.ads-pixiv.net/show?zone_id=illust_rectangle&amp;format=js&amp;s=1&amp;up=0&amp;a=25&amp;ng=w&amp;l=zh&amp;uri=%2Fartworks%2F_PARAM_&amp;ref=www.pixiv.net%2Fartworks%2F101739861&amp;is_spa=1&amp;K=84c28b3a151d2&amp;ab_test_digits_first=19&amp;uab=31&amp;yuid=hgMjJoA&amp;suid=Ph6zdipfekrupczz1&amp;num=6342d156710"},"500x500":{"url":"https://pixon.ads-pixiv.net/show?zone_id=bigbanner&amp;format=js&amp;s=1&amp;up=0&amp;a=25&amp;ng=w&amp;l=zh&amp;uri=%2Fartworks%2F_PARAM_&amp;ref=www.pixiv.net%2Fartworks%2F101739861&amp;is_spa=1&amp;K=84c28b3a151d2&amp;ab_test_digits_first=19&amp;uab=31&amp;yuid=hgMjJoA&amp;suid=Ph6zdipfenb6s0j6j&amp;num=6342d156152"},"header":{"url":"https://pixon.ads-pixiv.net/show?zone_id=header&amp;format=js&amp;s=1&amp;up=0&amp;a=25&amp;ng=w&amp;l=zh&amp;uri=%2Fartworks%2F_PARAM_&amp;ref=www.pixiv.net%2Fartworks%2F101739861&amp;is_spa=1&amp;K=84c28b3a151d2&amp;ab_test_digits_first=19&amp;uab=31&amp;yuid=hgMjJoA&amp;suid=Ph6zdipfepuruldoa&amp;num=6342d156609"},"footer":{"url":"https://pixon.ads-pixiv.net/show?zone_id=footer&amp;format=js&amp;s=1&amp;up=0&amp;a=25&amp;ng=w&amp;l=zh&amp;uri=%2Fartworks%2F_PARAM_&amp;ref=www.pixiv.net%2Fartworks%2F101739861&amp;is_spa=1&amp;K=84c28b3a151d2&amp;ab_test_digits_first=19&amp;uab=31&amp;yuid=hgMjJoA&amp;suid=Ph6zdipfesc2dlow&amp;num=6342d156798"},"expandedFooter":{"url":"https://pixon.ads-pixiv.net/show?zone_id=multiple_illust_viewer&amp;format=js&amp;s=1&amp;up=0&amp;a=25&amp;ng=w&amp;l=zh&amp;uri=%2Fartworks%2F_PARAM_&amp;ref=www.pixiv.net%2Fartworks%2F101739861&amp;is_spa=1&amp;K=84c28b3a151d2&amp;ab_test_digits_first=19&amp;uab=31&amp;yuid=hgMjJoA&amp;suid=Ph6zdipfeuu3big9k&amp;num=6342d15676"},"logo":{"url":"https://pixon.ads-pixiv.net/show?zone_id=logo_side&amp;format=js&amp;s=1&amp;up=0&amp;a=25&amp;ng=w&amp;l=zh&amp;uri=%2Fartworks%2F_PARAM_&amp;ref=www.pixiv.net%2Fartworks%2F101739861&amp;is_spa=1&amp;K=84c28b3a151d2&amp;ab_test_digits_first=19&amp;uab=31&amp;yuid=hgMjJoA&amp;suid=Ph6zdipfexd1er4hc&amp;num=6342d15634"},"relatedworks":{"url":"https://pixon.ads-pixiv.net/show?zone_id=relatedworks&amp;format=js&amp;s=1&amp;up=0&amp;a=25&amp;ng=w&amp;l=zh&amp;uri=%2Fartworks%2F_PARAM_&amp;ref=www.pixiv.net%2Fartworks%2F101739861&amp;is_spa=1&amp;K=84c28b3a151d2&amp;ab_test_digits_first=19&amp;uab=31&amp;yuid=hgMjJoA&amp;suid=Ph6zdipfezv9s8fv6&amp;num=6342d156364"}},"extraData":{"meta":{"title":"#女の子 ♥ - Nekojira的插画 - pixiv","description":"この作品 「♥」 は 「女の子」「白髪」 等のタグがつけられた「Nekojira」さんのイラストです。","canonical":"https://www.pixiv.net/artworks/101739861","alternateLanguages":{"ja":"https://www.pixiv.net/artworks/101739861","en":"https://www.pixiv.net/en/artworks/101739861"},"descriptionHeader":"本作「♥」为附有「女の子」「白髪」等标签的插画。","ogp":{"description":"","image":"https://embed.pixiv.net/artwork.php?illust_id=101739861","title":"#女の子 ♥ - Nekojira的插画 - pixiv","type":"article"},"twitter":{"description":"♥ by Nekojira","image":"https://embed.pixiv.net/artwork.php?illust_id=101739861","title":"♥","card":"summary_large_image"}}},"titleCaptionTranslation":{"workTitle":null,"workCaption":null},"isUnlisted":false,"request":null,"commentOff":0}},"user":{"4526277":{"userId":"4526277","name":"Nekojira","image":"https://i.pximg.net/user-profile/img/2021/09/02/00/03/49/21339747_704230a8a408159fcfac3a840a2e4e78_50.jpg","imageBig":"https://i.pximg.net/user-profile/img/2021/09/02/00/03/49/21339747_704230a8a408159fcfac3a840a2e4e78_170.jpg","premium":false,"isFollowed":false,"isMypixiv":false,"isBlocking":false,"background":{"repeat":null,"color":null,"url":"https://i.pximg.net/c/1920x960_80_a2_g5/background/img/2021/09/02/00/03/18/4526277_584561861b79297d4d05ececd39bd800.jpg","isPrivate":false},"sketchLiveId":null,"partial":0,"acceptRequest":false,"sketchLives":[]}}}'>
                </head><body><div id='root'></div><script>'use strict';var dataLayer = [{login: 'yes',gender: "male",user_id: "60903890",lang: "zh",illustup_flg: 'not_uploaded',premium: 'no',default_service_is_touch: 'no',}];</script>
                <!-- Google Tag Manager -->
                <noscript><iframe src="//www.googletagmanager.com/ns.html?id=GTM-55FG"
                height="0" width="0" style="display:none;visibility:hidden"></iframe></noscript>
                <script>(function(w,d,s,l,i){w[l]=w[l]||[];w[l].push({'gtm.start':
                new Date().getTime(),event:'gtm.js'});var f=d.getElementsByTagName(s)[0],
                j=d.createElement(s),dl=l!='dataLayer'?'&l='+l:'';j.async=true;j.src=
                '//www.googletagmanager.com/gtm.js?id='+i+dl;f.parentNode.insertBefore(j,f);
                })(window,document,'script','dataLayer','GTM-55FG');</script>
                <!-- End Google Tag Manager -->
                <script>window.dataLayer = window.dataLayer || [];function gtag(){dataLayer.push(arguments);}gtag('js', new Date());let event_params = {'login': 'yes','p_ab_d_id': "2134615577",'default_service_is_touch': 'no',};gtag('set', 'user_properties', {'plan': 'normal','gender': 'male','user_id': "60903890",'lang': "zh",'illustup_flg': 'not_uploaded',});gtag('config', 'G-75BBYNYN9J', {...event_params});</script><span id="qualtrics_user-id" hidden>60903890</span><span id="qualtrics_gender" hidden>male</span><span id="qualtrics_age" hidden>25</span><span id="qualtrics_language" hidden>zh</span><span id="qualtrics_is-premium" hidden>no</span><span id="qualtrics_is-user-is-illust-creator" hidden>no</span><span id="qualtrics_is-user-is-manga-creator" hidden>no</span><span id="qualtrics_is-user-is-novel-creator" hidden>no</span><span id="qualtrics_default-service-is-touch" hidden>no</span>
                    <script type='text/javascript'>
                        (function(){var g=function(e,h,f,g){
                            this.get=function(a){for(var a=a+"=",c=document.cookie.split(";"),b=0,e=c.length;b<e;b++){for(var d=c[b];" "==d.charAt(0);)d=d.substring(1,d.length);if(0==d.indexOf(a))return d.substring(a.length,d.length)}return null};
                            this.set=function(a,c){var b="",b=new Date;b.setTime(b.getTime()+6048E5);b="; expires="+b.toGMTString();document.cookie=a+"="+c+b+"; path=/; "};
                            this.check=function(){var a=this.get(f);if(a)a=a.split(":");else if(100!=e)"v"==h&&(e=Math.random()>=e/100?0:100),a=[h,e,0],this.set(f,a.join(":"));else return!0;var c=a[1];if(100==c)return!0;switch(a[0]){case "v":return!1;case "r":return c=a[2]%Math.floor(100/c),a[2]++,this.set(f,a.join(":")),!c}return!0};
                            this.go=function(){if(this.check()){var a=document.createElement("script");a.type="text/javascript";a.src=g;document.body&&document.body.appendChild(a)}};
                            this.start=function(){var t=this;"complete"!==document.readyState?window.addEventListener?window.addEventListener("load",function(){t.go()},!1):window.attachEvent&&window.attachEvent("onload",function(){t.go()}):t.go()};};
                            try{(new g(1,"v","QSI_S_ZN_5hF4My7Ad6VNNAi","https://zn5hf4my7ad6vnnai-pixiv.siteintercept.qualtrics.com/SIE/?Q_ZID=ZN_5hF4My7Ad6VNNAi")).start()}catch(i){}})();
                    </script><div id='ZN_5hF4My7Ad6VNNAi'></div>
                </body></html>
                """);

        System.out.println(StrUtil.subAfter(group0, "\"original\":\"", false));
    }

    private void fetchPicture() {
        RamRecorder ramRecorder = new RamRecorder();
        Crawler crawler = new Crawler("https://www.huashi6.com/rank");
        crawler.addParam("key", "value1");
        crawler.addLine("method", "GET");
        crawler.addHeader("Referer", "http://www.baidu.com");
        ramRecorder.add(crawler);
        CrawlerGlobalConfig crawlerGlobalConfig = new CrawlerGlobalConfig();
//        crawlerGlobalConfig.addRegEx("(http|https)://.*");
        crawlerGlobalConfig.setDepth(3);
        crawlerGlobalConfig.setMaxSize((long) 1024 * 1024);
        new GenericStarter(crawlerGlobalConfig, ramRecorder, new ResultHandler() {
            public Set<Crawler> doSuccess(Recorder recorder, Crawler crawler, Page page, Response response) {

                List<String> urlList = new ArrayList<>();
                String regEx = """
                        ("originalPath":")[(\\w),(\\\\u002F)]{1,}(\\.)([a-z]{3})
                        """;
                List<String> allGroups = ReUtil.findAllGroup0(Pattern.compile(regEx.trim(), Pattern.DOTALL), page.getContent());
                for (String str : allGroups) {
                    String url = "http://img2.huashi6.com/" + str.substring(16).replaceAll("\\\\u002F", "/");
                    if (StrUtil.isNotBlank(url)) {
                        urlList.add(url);
                    }
                }
                page.addSeeds(urlList);
                return page.getCrawlers();
            }

            public void doFailure(Recorder recorder, Crawler crawler, IOException e) {

            }
        }).start();
    }

    void fetchWeChatArticle() {
        RamRecorder ramRecorder = new RamRecorder();
        ramRecorder.add(new Crawler("https://mp.weixin.qq.com/s?__biz=MzIxMjgzMDUyNw==&mid=2247489048&idx=1&sn=072866b456945d297ec2516dd72e5a41&chksm=97414648a036cf5eba9ddf88c7cf7a27809ae414b4ce43d5595c7351172d04d70664eab25761&scene=90&subscene=93&sessionid=1664460391&clicktime=1664460397&enterid=1664460397&ascene=56&fasttmpl_type=0&fasttmpl_fullversion=6351034-zh_CN-zip&fasttmpl_flag=0&realreporttime=1664460397767&devicetype=android-31&version=28001c3b&nettype=WIFI&abtest_cookie=AAACAA%3D%3D&lang=zh_CN&session_us=gh_391abad800db&exportkey=A01mvM0fP%2BtbjfOBlrDdga8%3D&pass_ticket=fRKCL5vF5nmJEU4Y0DJ60ftOP9hbDgcI5Syn9wR%2BP26sjnBzcmbbozXA3pV42cES&wx_header=3"));
        CrawlerGlobalConfig crawlerGlobalConfig = new CrawlerGlobalConfig();
        crawlerGlobalConfig.addRegEx("(http|https)://.*");
        crawlerGlobalConfig.setAllowEnd(true);
        crawlerGlobalConfig.setForceEnd(true);
        crawlerGlobalConfig.setDepth(3);
        new GenericStarter(crawlerGlobalConfig, ramRecorder, new ResultHandler() {
            public Set<Crawler> doSuccess(Recorder recorder, Crawler crawler, Page page, Response response) {
//                Set<Crawler> crawlers = page.getCrawlers().stream().filter(
//                        crawler1 -> {
//                            return ReUtil.isMatch(".*\\.(jpg|jpeg|png|webp|gif)", crawler1.getUrl());
//                        }
//                ).collect(Collectors.toSet());
                List<String> urlList = page.getCrawlers().stream().map(Crawler::getUrl).toList();
//                System.out.println(urlList.size());
//                System.out.println(urlList);
                page.addSeeds(urlList);
                return page.getCrawlers();
            }

            public void doFailure(Recorder recorder, Crawler crawler, IOException e) {

            }
        }).start();
    }

    /**
     * 爬取开源中国的数据
     */
    void fetchOpenSourceChina() {
        RamRecorder ramRecorder = new RamRecorder();
        ramRecorder.add(new Crawler("https://gitee.com/explore"));
        CrawlerGlobalConfig crawlerGlobalConfig = new CrawlerGlobalConfig();
        crawlerGlobalConfig.addRegEx("http://.*");
        crawlerGlobalConfig.addRegEx("https://.*");
        crawlerGlobalConfig.setDepth(2);
        GenericStarter genericStarter = new GenericStarter(crawlerGlobalConfig, ramRecorder, new ResultHandler() {
            public Set<Crawler> doSuccess(Recorder recorder, Crawler crawler, Page page, Response response) {
                if (ReUtil.isMatch("^https://gitee.com/explore/([A-Za-z0-9-]{1,})", page.getUrl())) {
                    match(page);
                }
                Set<Crawler> crawlers = page.getCrawlers().stream().filter(
                        crawler1 -> ReUtil.isMatch("^https://gitee.com/explore/([A-Za-z0-9-]{1,})", crawler1.getUrl())
                ).collect(Collectors.toSet());
                List<String> strings = crawlers.stream().map(Crawler::getUrl).toList();
//                System.out.println(strings);
                return crawlers;
            }


            public void doFailure(Recorder recorder, Crawler crawler, IOException e) {

            }
        });
        genericStarter.start();
    }

    /**
     * 匹配左侧菜单URL
     *
     * @param page
     */
    public void match(Page page) {
        log.info("match: {}", page.getUrl());
        try {
            Elements select = page.getDocument().select(".pagination>.item");//翻页URL
            if (select.size() > 0) {
                String text = select.get(select.size() - 2).text();
                if (StrUtil.isNotBlank(text)) {
                    int pages = Integer.parseInt(text);//最大页数
                    for (int i = 1; i <= pages; i++) {
                        String url = page.getUrl();
                        if (url.contains("?page=")) {
                            String[] split = url.split("page=");
                            url = split[0] + "page=" + i;
                            page.addSeed(url);
                            log.info("url: " + url);
                        } else {
                            url = url + "?page=" + i;
                            log.info("url: " + url);
                            page.addSeed(url);
                        }
                    }
                }
            }
            Elements elements = page.getDocument().select("div.ui.relaxed.divided.items.explore-repo__list>.item");
            Element categoryElement = page.getDocument().selectFirst("div > div.explore-project__selection > div > div.ui.breadcrumb > div.section");
            String category = categoryElement.text();
            for (Element element : elements) {
                Element contentEle = element.selectFirst(".content");
                Element tagA = contentEle.selectFirst("h3 > a");
                String url = tagA.absUrl("href");//项目地址
                String title = tagA.text();//项目标题
                Element descEle = element.selectFirst(".project-desc");
                String desc = descEle.text();
                String language = contentEle.select(".project-language").text();
                String type = contentEle.select(".project-item-bottom__item").text();
                String time = contentEle.select(".text-muted").text();
                log.info("category: " + category + " title: " + title + " url: " + url + " desc: " + desc + " language: " + language + " type: " + type + " time: " + time);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
