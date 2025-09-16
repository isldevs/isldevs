package com.base.config.cache;


import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.caffeine.CaffeineCache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author YISivlay
 */
public class CustomCaffeineCache extends CaffeineCache {

    private static final ConcurrentHashMap<String, Boolean> accessedCaches = new ConcurrentHashMap<>();
    private final AtomicLong cacheHits = new AtomicLong();
    private final AtomicLong databaseHits = new AtomicLong();

    public CustomCaffeineCache(String name) {
        super(name, Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(1000)
                .recordStats()
                .build());
    }

    @Override
    public ValueWrapper get(Object key) {
        accessedCaches.put(getName(), true);
        var value = super.get(key);
        if (value != null) {
            cacheHits.incrementAndGet();
        } else {
            databaseHits.incrementAndGet();
        }
        return value;
    }

    public long getCacheHits() {
        return cacheHits.get();
    }

    public long getDatabaseHits() {
        return databaseHits.get();
    }

    public static boolean wasAccessed(String name) {
        return accessedCaches.containsKey(name);
    }

    public static void clearAccessed() {
        accessedCaches.clear();
    }
}
