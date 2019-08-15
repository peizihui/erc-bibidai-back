package com.theforceprotocol.bbd.service.impl;

import com.theforceprotocol.bbd.service.RedisLockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Service
public class RedisLockServiceImpl implements RedisLockService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final RedisTemplate redisTemplate;

    public RedisLockServiceImpl(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public <T> T process(String lockKey, Callable<T> callable, Class<T> returnType) throws Exception {
        checkLock(lockKey);
        try {
            return callable.call();
        } finally {
            releaseLock(lockKey);
        }
    }

    private void checkLock(String lockKey) {
        if (!requireLock(lockKey)) {
            throw new RequireLockFailureException(lockKey);
        }
    }

    private void releaseLock(String lockKey) {
        while (true) {
            try {
                redisTemplate.delete(lockKey);
                return;
            } catch (Exception e) {
                logger.error("release lock error: {}", lockKey, e);
            }
        }
    }


    private boolean requireLock(String lockKey) {
        boolean isSuccess = redisTemplate.opsForValue().increment(lockKey, 1L).compareTo(1L) == 0;
        if (isSuccess) {
            expireLock(lockKey);
        }
        return isSuccess;
    }


    private void expireLock(String lockKey) {
        try {
            if (redisTemplate.expire(lockKey, 600_000L, TimeUnit.MILLISECONDS)) {
                return;
            }
        } catch (Exception e) {
            logger.error("expire lock error", e);
        }
        releaseLock(lockKey);
    }
}
