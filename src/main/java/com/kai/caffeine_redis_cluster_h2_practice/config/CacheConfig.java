package com.kai.caffeine_redis_cluster_h2_practice.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
// import org.springframework.data.redis.cache.RedisCacheManager;
// import org.springframework.cache.support.CompositeCacheManager;
// import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    // 1. 使用本地 Caffeine 快取
//    @Bean
//    public CacheManager caffeineCacheManager() {
//        CaffeineCacheManager manager = new CaffeineCacheManager("userCache");
//        manager.setCaffeine(Caffeine.newBuilder()
//                .expireAfterWrite(5, TimeUnit.MINUTES)
//                .maximumSize(1000));
//        return manager;
//    }

    // 2. 只使用 Redis 快取
    @Bean
    public CacheManager redisCacheManager(RedisConnectionFactory factory) {
        return RedisCacheManager.builder(factory).build();
    }


    // 3. Caffeine + Redis 組合快取
//    @Bean
//    public CacheManager compositeCacheManager(RedisConnectionFactory factory) {
//        CaffeineCacheManager caffeine = new CaffeineCacheManager("userCache");
//        caffeine.setCaffeine(Caffeine.newBuilder()
//            .expireAfterWrite(5, TimeUnit.SECONDS)
//            .maximumSize(1000));
//
//        RedisCacheManager redis = RedisCacheManager.builder(factory).build();
//
//        CompositeCacheManager manager = new CompositeCacheManager(caffeine, redis);
//        manager.setFallbackToNoOpCache(false);
//        return manager;
//    }

}
