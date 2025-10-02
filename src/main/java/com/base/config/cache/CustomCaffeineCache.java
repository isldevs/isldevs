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

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.cache.caffeine.CaffeineCache;

/**
 * @author YISivlay
 */
public class CustomCaffeineCache extends CaffeineCache {

  private static final ConcurrentHashMap<String, Boolean> accessedCaches =
      new ConcurrentHashMap<>();
  private final AtomicLong cacheHits = new AtomicLong();
  private final AtomicLong databaseHits = new AtomicLong();

  public CustomCaffeineCache(String name) {
    super(
        name,
        Caffeine.newBuilder()
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
