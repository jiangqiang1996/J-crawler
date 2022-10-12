package top.jiangqiang.crawler.core.recorder;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import top.jiangqiang.crawler.core.constants.RedisConstants;
import top.jiangqiang.crawler.core.entities.Crawler;

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
@RequiredArgsConstructor
public class RedisRecorder extends AbstractRecorder {
    public final RedisTemplate<String, Crawler> redisTemplate;

    @Override
    public void initBeforeStart() {
        addAll(getAllError());
        addAll(getAllActive());
        super.initBeforeStart();
    }

    @Override
    public synchronized void add(Crawler crawler) {
        if (!exist(crawler)) {
            redisTemplate.opsForList().rightPush(RedisConstants.CRAWLER_WAITING_LIST, crawler);
            redisTemplate.opsForHash().put(RedisConstants.CRAWLER_HISTORY_HASH, crawler.getUrl(), crawler);
        }
    }

    @Override
    public Crawler popOne() {
        return redisTemplate.opsForList().leftPop(RedisConstants.CRAWLER_WAITING_LIST, 5L, TimeUnit.MILLISECONDS);
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
        redisTemplate.opsForList().rightPush(RedisConstants.CRAWLER_SUCCESS_LIST, crawler);
    }

    @Override
    public List<Crawler> getAllSuccess() {
        return redisTemplate.opsForList().range(RedisConstants.CRAWLER_SUCCESS_LIST, 0, -1);
    }

    @Override
    public Long countSuccess() {
        return redisTemplate.opsForList().size(RedisConstants.CRAWLER_SUCCESS_LIST);
    }

    @Override
    public void addError(Crawler crawler) {
        redisTemplate.opsForList().rightPush(RedisConstants.CRAWLER_ERROR_LIST, crawler);
    }

    @Override
    public List<Crawler> getAllError() {
        return redisTemplate.opsForList().range(RedisConstants.CRAWLER_ERROR_LIST, 0, -1);
    }

    @Override
    public Long countError() {
        return redisTemplate.opsForList().size(RedisConstants.CRAWLER_ERROR_LIST);
    }

    @Override
    public void addActive(Crawler crawler) {
        redisTemplate.opsForList().rightPush(RedisConstants.CRAWLER_ACTIVE_LIST, crawler);
    }

    @Override
    public void removeActive(Crawler crawler) {
        redisTemplate.opsForList().remove(RedisConstants.CRAWLER_ACTIVE_LIST, 0, crawler);
    }

    @Override
    public List<Crawler> getAllActive() {
        return redisTemplate.opsForList().range(RedisConstants.CRAWLER_ACTIVE_LIST, 0, -1);
    }

    @Override
    public Long countActive() {
        return redisTemplate.opsForList().size(RedisConstants.CRAWLER_ACTIVE_LIST);
    }

    @Override
    public synchronized Boolean exist(Crawler crawler) {
        return redisTemplate.opsForHash().get(RedisConstants.CRAWLER_HISTORY_HASH, crawler.getUrl()) != null;
    }
}
