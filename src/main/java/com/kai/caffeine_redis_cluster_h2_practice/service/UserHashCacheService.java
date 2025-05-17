package com.kai.caffeine_redis_cluster_h2_practice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kai.caffeine_redis_cluster_h2_practice.entity.User;
import com.kai.caffeine_redis_cluster_h2_practice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserHashCacheService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String PREFIX = "user:hash";

    public User getUserById(Long id) {
        String key = PREFIX;
        String field = id.toString();

        HashOperations<String, Object, Object> ops = redisTemplate.opsForHash();

        // 嘗試從 Redis Hash 快取讀取
        Object cachedJson = ops.get(key, field);
        if (cachedJson != null) {
            try {
                return objectMapper.readValue(cachedJson.toString(), User.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("反序列化快取資料失敗", e);
            }
        }

        // 若無快取 → 查詢 DB → 寫入 Redis Hash
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        try {
            ops.put(key, field, objectMapper.writeValueAsString(user));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化快取資料失敗", e);
        }

        return user;
    }
}
