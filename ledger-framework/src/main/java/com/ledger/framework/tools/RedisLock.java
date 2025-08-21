package com.ledger.framework.tools;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class RedisLock {
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    private static final String LOCK_PREFIX = "ry_lock:";
    private static final ThreadLocal<String> LOCK_VALUE = new ThreadLocal<>();

    /**
     * 尝试获取锁，返回 true/false
     */
    public boolean tryLock(String lockKey, long timeout, TimeUnit unit) {
        String uuid = UUID.randomUUID().toString();
        Boolean ok = redisTemplate.opsForValue()
                .setIfAbsent(LOCK_PREFIX + lockKey, uuid, timeout, unit);
        if (Boolean.TRUE.equals(ok)) {
            LOCK_VALUE.set(uuid);   // 把值暂存到线程变量，供释放锁使用
            return true;
        }
        return false;
    }

    /**
     * 释放锁（只能释放当前线程自己加的锁）
     */
    public boolean releaseLock(String lockKey) {
        String uuid = LOCK_VALUE.get();
        if (uuid == null) return false;

        String script =
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                        "return redis.call('del', KEYS[1]) else return 0 end";
        Long rst = redisTemplate.execute(
                new DefaultRedisScript<>(script, Long.class),
                Collections.singletonList(LOCK_PREFIX + lockKey),
                uuid
        );
        LOCK_VALUE.remove();
        return rst != null && rst > 0;
    }
}