package com.base.config.cache;


import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;

/**
 * @author YISivlay
 */
public class CustomCaffeineCacheManager extends CaffeineCacheManager {

    public CustomCaffeineCacheManager(String... cacheNames) {
        super(cacheNames);
    }

    @Override
    protected CaffeineCache createCaffeineCache(String name) {
        return new CustomCaffeineCache(name);
    }
}
