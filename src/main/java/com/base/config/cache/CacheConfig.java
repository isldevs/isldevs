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
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author YISivlay
 */
@Configuration
public class CacheConfig {

    final Logger logger = LoggerFactory.getLogger(getClass());

    private final Dotenv env;
    private final Environment springEnvironment;

    @Autowired
    public CacheConfig(Environment environment) {
        this.springEnvironment = environment;
        var activeProfile = environment.getActiveProfiles()[0];
        this.env = Dotenv.configure()
                .directory("./config")
                .filename("." + activeProfile)
                .ignoreIfMissing()
                .load();
    }

    @Bean
    public CacheManager cacheManager() {
        boolean useRedis = shouldUseRedis();
        logger.info("Using {} cache manager", useRedis ? "redis" : "default");

        if (useRedis) {
            return createRedisCacheManager();
        } else {
            return createDefaultCacheManager();
        }
    }

    private boolean shouldUseRedis() {

        String redisEnabled = env.get("REDIS_ENABLED");
        String systemRedis = System.getProperty("redis.enabled");
        String springRedis = springEnvironment.getProperty("spring.cache.redis.enabled");

        return "true".equalsIgnoreCase(redisEnabled) ||
                "true".equalsIgnoreCase(systemRedis) ||
                "true".equalsIgnoreCase(springRedis);
    }

    private CacheManager createRedisCacheManager() {
        try {
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
            config.setHostName(env.get("REDIS_HOST", "localhost"));
            config.setPort(Integer.parseInt(env.get("REDIS_PORT", "6379")));
            config.setPassword(env.get("REDIS_PASSWORD", ""));

            LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(config);
            connectionFactory.afterPropertiesSet();

            RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofDays(7))
                    .disableCachingNullValues()
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

            return RedisCacheManager.builder(RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory))
                    .cacheDefaults(cacheConfig)
                    .transactionAware()
                    .build();

        } catch (Exception e) {
            logger.warn("Failed to create redis cache manager, switch back to default cache manager", e);
            return createDefaultCacheManager();
        }
    }

    private CacheManager createDefaultCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(7, TimeUnit.DAYS)
                .maximumSize(1000));
        return cacheManager;
    }
}
