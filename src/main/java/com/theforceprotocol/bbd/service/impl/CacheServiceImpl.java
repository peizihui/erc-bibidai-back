package com.theforceprotocol.bbd.service.impl;

import com.theforceprotocol.bbd.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Service
public class CacheServiceImpl implements CacheService {
    private final RedisTemplate redisTemplate;

    public CacheServiceImpl(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public <T> T load(String key, long timeout, TimeUnit timeUnit,
                      Class<T> cls,
                      Supplier<? extends T> supplier) {
        ValueOperations ops = redisTemplate.opsForValue();
        return Optional.ofNullable(ops.get(key))
                .map(cls::cast)
                .orElseGet(() -> {
                    T t = supplier.get();
                    ops.set(key, t, timeout, timeUnit);
                    return t;
                });
    }
}
