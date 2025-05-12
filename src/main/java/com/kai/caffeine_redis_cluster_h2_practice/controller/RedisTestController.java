package com.kai.caffeine_redis_cluster_h2_practice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/redis")
@RequiredArgsConstructor
public class RedisTestController {

    private final RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/set")
    public String setValue(@RequestParam String key, @RequestParam String value) {
        redisTemplate.opsForValue().set(key, value);
        return "✅ Set success: " + key + " = " + value;
    }

    @GetMapping("/get")
    public String getValue(@RequestParam String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? "🔍 Value: " + value : "⚠️ No value found for key: " + key;
    }
}
