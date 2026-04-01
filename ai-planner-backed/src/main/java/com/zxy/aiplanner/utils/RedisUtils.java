package com.zxy.aiplanner.utils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 通用工具类。
 */
@Component
public class RedisUtils {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUtils(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean setWithExpire(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    public String getString(String key) {
        Object value = get(key);
        return value == null ? null : String.valueOf(value);
    }

    public boolean expire(String key, long timeout, TimeUnit unit) {
        if (key == null || key.isBlank()) {
            return false;
        }
        try {
            Boolean result = redisTemplate.expire(key, timeout, unit);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            return false;
        }
    }

    public long getExpire(String key, TimeUnit unit) {
        if (key == null || key.isBlank()) {
            return -1;
        }
        try {
            Long ttl = redisTemplate.getExpire(key, unit);
            return ttl == null ? -1 : ttl;
        } catch (Exception e) {
            return -1;
        }
    }

    public boolean del(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        Boolean result = redisTemplate.delete(key);
        return Boolean.TRUE.equals(result);
    }

    public boolean hasKey(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        Boolean result = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(result);
    }
}
