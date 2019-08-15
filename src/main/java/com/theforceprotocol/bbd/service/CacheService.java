package com.theforceprotocol.bbd.service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public interface CacheService {
    <T> T load(String key, long timeout, TimeUnit timeUnit,
               Class<T> cls, Supplier<? extends T> supplier);
}
