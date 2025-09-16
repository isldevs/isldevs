package com.base.config.cache;


import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author YISivlay
 */
public class CustomRedisCache extends RedisCache {

    private final AtomicLong cacheHits = new AtomicLong();
    private final AtomicLong databaseHits = new AtomicLong();

    protected CustomRedisCache(String name,
                               RedisCacheWriter cacheWriter,
                               RedisCacheConfiguration cacheConfiguration) {
        super(name, cacheWriter, cacheConfiguration);
    }

    @Override
    public ValueWrapper get(Object key) {
        var value = super.get(key);
        if (value != null) cacheHits.incrementAndGet();
        else databaseHits.incrementAndGet();
        return value;
    }

    public long getCacheHits() {
        return cacheHits.get();
    }

    public long getDatabaseHits() {
        return databaseHits.get();
    }
}
