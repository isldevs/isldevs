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

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

/**
 * @author YISivlay
 */
@Configuration
public class CacheConfig {

    final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String[] CACHE_NAMES = new String[]{"users", "roles", "offices", "provinces", "districts", "communes", "villages"};

    private final Environment env;

    @Autowired
    public CacheConfig(Environment environment) {
        this.env = environment;
    }

    @Bean
    public CacheManager cacheManager(ObjectProvider<RedisConnectionFactory> redisConnectionFactory) {
        boolean useRedis = Boolean.parseBoolean(env.getProperty("spring.redis.enabled", "false"));
        if (useRedis) {
            var connectionFactory = redisConnectionFactory.getIfAvailable();
            if (connectionFactory != null) {
                logger.info("Using redis cache manager");
                return createRedisCacheManager(connectionFactory);
            }
        }
        logger.info("Using default caffeine cache manager");
        return createCaffeineCacheManager();
    }

    private CacheManager createRedisCacheManager(RedisConnectionFactory connectionFactory) {
        try {
            var config = new RedisStandaloneConfiguration();
            config.setHostName(env.getProperty("spring.data.redis.host", "localhost"));
            config.setPort(Integer.parseInt(env.getProperty("spring.data.redis.port", "6379")));
            config.setPassword(env.getProperty("spring.data.redis.password", "password"));

            var cacheDefaults = RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofHours(1))
                    .disableCachingNullValues()
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

            return new CustomRedisCacheManager(RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory), cacheDefaults);

        } catch (Exception e) {
            logger.warn("Failed to create redis cache manager, falling back to caffeine cache manager", e);
            return createCaffeineCacheManager();
        }
    }

    private CacheManager createCaffeineCacheManager() {
        return new CustomCaffeineCacheManager(CACHE_NAMES);
    }

}
