/*
 * Copyright 2025 iSLDevs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.base.config.cache;


import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author YISivlay
 */
@Component
public class CacheMonitor {
    private final CacheManager cacheManager;
    private final Logger logger = LoggerFactory.getLogger(CacheMonitor.class);

    public CacheMonitor(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Scheduled(fixedRate = 60000)
    public void logCacheStats() {
        cacheManager.getCacheNames().forEach(name -> {
            var cache = (CaffeineCache) cacheManager.getCache(name);
            if (cache != null) {
                var stats = cache.getNativeCache().stats();
                var hitRatio = (stats.hitCount() + stats.missCount()) > 0
                        ? (double) stats.hitCount() / (stats.hitCount() + stats.missCount())
                        : 1.0;
                if (hitRatio < 0.7) {
                    //Many database call, potential performance bottleneck
                    logger.warn("Cache [{}] hit ratio low: {}%", name, hitRatio * 100);
                }
            }
        });
    }
}
