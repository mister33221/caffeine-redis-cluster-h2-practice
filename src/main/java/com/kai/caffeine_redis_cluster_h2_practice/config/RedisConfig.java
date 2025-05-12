package com.kai.caffeine_redis_cluster_h2_practice.config;


import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;

import java.util.Arrays;

@Configuration
@EnableCaching
public class RedisConfig {

//    yml 中已經有設定了
//    @Bean
//    public LettuceConnectionFactory redisConnectionFactory() {
//        RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(
//                Arrays.asList("localhost:7000", "localhost:7001", "localhost:7002")
//        );
//        return new LettuceConnectionFactory(clusterConfig);
//    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 使用 JSON 序列化器（避免亂碼）
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        template.setDefaultSerializer(serializer);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

//    這是只有 Redis 當作快取的做法
//    @Bean
//    public CacheManager redisCacheManager(LettuceConnectionFactory factory) {
//        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.builder(factory);
//        return builder.build();
//    }


    @Bean
    public SimpleKeyGenerator keyGenerator() {
        return new SimpleKeyGenerator();
    }
}

