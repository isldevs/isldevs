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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.CacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author YISivlay
 */
@Component
public class CacheMonitor {

  private final Logger logger = LoggerFactory.getLogger(CacheMonitor.class);

  private final CacheManager cacheManager;
  private final RedisConnectionFactory redisConnectionFactory;

  public CacheMonitor(
      final CacheManager cacheManager,
      final ObjectProvider<RedisConnectionFactory> redisConnectionFactory) {
    this.cacheManager = cacheManager;
    this.redisConnectionFactory = redisConnectionFactory.getIfAvailable();
  }

  @Scheduled(fixedRate = 60000)
  public void logCacheStats() {
    cacheManager
        .getCacheNames()
        .forEach(
            name -> {
              if (!CustomCaffeineCache.wasAccessed(name)) return;
              var cache = cacheManager.getCache(name);
              if (cache == null) return;
              if (cache instanceof TransactionAwareCacheDecorator decorator) {
                cache = decorator.getTargetCache();
              }
              if (cache instanceof CustomCaffeineCache caffeineCache) {
                var hits = caffeineCache.getCacheHits();
                var misses = caffeineCache.getDatabaseHits();
                var total = hits + misses;
                var hitRatio = total > 0 ? (double) hits / total : 1.0;
                logger.info(
                    "Caffeine Cache [{}] hit ratio: {}% (cache_hits={}, database_hits={})",
                    name, hitRatio * 100, hits, misses);
              } else if (cache instanceof CustomRedisCache redisCache
                  && redisConnectionFactory != null) {
                var hits = redisCache.getCacheHits();
                var misses = redisCache.getDatabaseHits();
                var total = hits + misses;
                var hitRatio = total > 0 ? (double) hits / total : 1.0;
                logger.info(
                    "Redis Cache [{}] hit ratio: {}% (cache_hits={}, database_hits={})",
                    name, hitRatio * 100, hits, misses);
              }
            });
    CustomCaffeineCache.clearAccessed();
  }
}
