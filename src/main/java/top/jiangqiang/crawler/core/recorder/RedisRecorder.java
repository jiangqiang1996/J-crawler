package top.jiangqiang.crawler.core.recorder;

import cn.hutool.core.collection.CollUtil;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import top.jiangqiang.crawler.core.constants.RedisConstants;
import top.jiangqiang.crawler.core.entities.Crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 使用redis的hash去重
 * 可以使用布隆过滤器自定义实现去重
 *
 * @author Jiangqiang
 * @version 1.0
 * @description
 * @date 2022/10/11 17:30
 */
@SuppressWarnings(value = {"unchecked", "rawtypes"})
public class RedisRecorder extends AbstractRecorder {
    public final RedisTemplate<String, Crawler> redisTemplate;
    public final HashOperations<String, String, Crawler> hashOperations;

    public RedisRecorder(RedisTemplate<String, Crawler> redisTemplate) {
        this.redisTemplate = redisTemplate;
        hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public synchronized void initBeforeStart() {
        Boolean isContinue = getConfig().getIsContinue();
        if (isContinue) {
            //上次失败的列表
            List<Crawler> errorCrawlers = hashOperations.values(RedisConstants.CRAWLER_ERROR_HASH);
            //上次没有完成的列表，一般是因为意外情况导致程序突然终止，才会有数据
            List<Crawler> activeCrawlers = hashOperations.values(RedisConstants.CRAWLER_ACTIVE_HASH);
            List<Crawler> crawlers = new ArrayList<>();
            if (CollUtil.isNotEmpty(errorCrawlers)) {
                crawlers.addAll(errorCrawlers);
                //删除整个hash结构
                redisTemplate.delete(RedisConstants.CRAWLER_ERROR_HASH);
            }
            if (CollUtil.isNotEmpty(activeCrawlers)) {
                crawlers.addAll(activeCrawlers);
                //删除整个hash结构
                redisTemplate.delete(RedisConstants.CRAWLER_ACTIVE_HASH);
            }
            if (CollUtil.isNotEmpty(crawlers)) {
                //去重
                redisTemplate.opsForList().rightPushAll(RedisConstants.CRAWLER_WAITING_LIST, crawlers.stream().distinct().toList());
            }
        }
        super.initBeforeStart();
    }

    @Override
    public synchronized void add(Crawler crawler) {
        if (!exist(crawler)) {
            redisTemplate.opsForList().rightPush(RedisConstants.CRAWLER_WAITING_LIST, crawler);
            redisTemplate.opsForHash().put(RedisConstants.CRAWLER_HISTORY_HASH, crawler.getId(), crawler);
        }
    }

    @Override
    public Crawler popOne() {
        try {
            return redisTemplate.opsForList().leftPop(RedisConstants.CRAWLER_WAITING_LIST, 5L, TimeUnit.SECONDS);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Crawler> getAll() {
        return redisTemplate.opsForList().range(RedisConstants.CRAWLER_WAITING_LIST, 0, -1);
    }

    @Override
    public Long count() {
        return redisTemplate.opsForList().size(RedisConstants.CRAWLER_WAITING_LIST);
    }

    @Override
    public void addSuccess(Crawler crawler) {
        hashOperations.put(RedisConstants.CRAWLER_SUCCESS_HASH, crawler.getId(), crawler);
    }

    @Override
    public List<Crawler> getAllSuccess() {
        return hashOperations.values(RedisConstants.CRAWLER_SUCCESS_HASH);
    }

    @Override
    public Long countSuccess() {
        return hashOperations.size(RedisConstants.CRAWLER_SUCCESS_HASH);
    }

    @Override
    public void addError(Crawler crawler) {
        hashOperations.put(RedisConstants.CRAWLER_ERROR_HASH, crawler.getId(), crawler);
    }

    @Override
    public List<Crawler> getAllError() {
        return hashOperations.values(RedisConstants.CRAWLER_ERROR_HASH);
    }

    @Override
    public Long countError() {
        return hashOperations.size(RedisConstants.CRAWLER_ERROR_HASH);
    }

    @Override
    public void addActive(Crawler crawler) {
        hashOperations.put(RedisConstants.CRAWLER_ACTIVE_HASH, crawler.getId(), crawler);
    }

    @Override
    public void removeActive(Crawler crawler) {
        hashOperations.delete(RedisConstants.CRAWLER_ACTIVE_HASH, crawler.getId());
    }

    @Override
    public List<Crawler> getAllActive() {
        return hashOperations.values(RedisConstants.CRAWLER_ACTIVE_HASH);
    }

    @Override
    public Long countActive() {
        return hashOperations.size(RedisConstants.CRAWLER_ACTIVE_HASH);
    }

    @Override
    public synchronized Boolean exist(Crawler crawler) {
        return redisTemplate.opsForHash().get(RedisConstants.CRAWLER_HISTORY_HASH, crawler.getId()) != null;
    }
}
