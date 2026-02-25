package com.sky.cache._3_remote_redis_real_example.improved;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLockProvider {

    private final RedisTemplate<String, String> redisTemplate;

    public boolean tryLock(String key, long timeoutMs) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, "locked", timeoutMs, TimeUnit.MILLISECONDS));
    }

    public void releaseLock(String key) {
        redisTemplate.delete(key);
    }
}
