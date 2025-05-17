package com.kai.caffeine_redis_cluster_h2_practice.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.kai.caffeine_redis_cluster_h2_practice.entity.User;
import com.kai.caffeine_redis_cluster_h2_practice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserMultiLevelCacheService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheManager cacheManager;

    private static final String LOCAL_CACHE = "userCache";
    private static final String REDIS_HASH_KEY = "user:hash";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public User getUserById(Long id) {
        // 1. 先查 Caffeine 本地快取
        Cache localCache = cacheManager.getCache(LOCAL_CACHE);
        if (localCache != null) {
            User cached = localCache.get(id, User.class);
            if (cached != null) {
                System.out.println("🟢 Hit Caffeine cache");
                return cached;
            }
        }

        // 2. 查 Redis Hash
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
        Object hashValue = hashOps.get(REDIS_HASH_KEY, id.toString());
        if (hashValue != null) {
            try {
                User redisUser = objectMapper.readValue(hashValue.toString(), User.class);
                System.out.println("🟡 Hit Redis cache");
                if (localCache != null) localCache.put(id, redisUser);
                return redisUser;
            } catch (Exception e) {
                throw new RuntimeException("❌ Redis 反序列化失敗", e);
            }
        }

        // 3. 查資料庫（H2）
        System.out.println("🔴 Hit DB");
        User dbUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));

        try {
            String json = objectMapper.writeValueAsString(dbUser);
            hashOps.put(REDIS_HASH_KEY, id.toString(), json);
        } catch (Exception e) {
            throw new RuntimeException("❌ Redis 序列化失敗", e);
        }

        if (localCache != null) localCache.put(id, dbUser);
        return dbUser;
    }
}
