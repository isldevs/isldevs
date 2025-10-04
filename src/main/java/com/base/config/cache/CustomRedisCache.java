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

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;

/**
 * @author YISivlay
 */
public class CustomRedisCache extends RedisCache {

    private final AtomicLong cacheHits = new AtomicLong();

    private final AtomicLong databaseHits = new AtomicLong();

    protected CustomRedisCache(String name,
                               RedisCacheWriter cacheWriter,
                               RedisCacheConfiguration cacheConfiguration) {
        super(name,
              cacheWriter,
              cacheConfiguration);
    }

    @Override
    public ValueWrapper get(Object key) {
        var value = super.get(key);
        if (value != null) cacheHits.incrementAndGet();
        else databaseHits.incrementAndGet();
        return value;
    }

    public long getCacheHits() { return cacheHits.get(); }

    public long getDatabaseHits() { return databaseHits.get(); }

}
