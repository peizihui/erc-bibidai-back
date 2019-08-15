package com.theforceprotocol.bbd.service;

import java.util.concurrent.Callable;

public interface RedisLockService {

    <T> T process(String lockKey, Callable<T> callable, Class<T> returnType) throws Exception;

    default void process(String lockKey, Runnable runnable) throws Exception {
        process(lockKey, () -> {
            runnable.run();
            return null;
        }, Void.class);
    }

    class RequireLockFailureException extends RuntimeException {
        private static final long serialVersionUID = -8478853064646086098L;

        public RequireLockFailureException(String message) {
            super(message);
        }
    }
}
