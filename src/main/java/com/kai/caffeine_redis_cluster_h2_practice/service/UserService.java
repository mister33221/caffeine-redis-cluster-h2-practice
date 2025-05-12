package com.kai.caffeine_redis_cluster_h2_practice.service;

import com.kai.caffeine_redis_cluster_h2_practice.entity.User;
import com.kai.caffeine_redis_cluster_h2_practice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Cacheable(value = "userCache", key = "#id")
    public User getUserById(Long id) {
        simulateSlowQuery(); // 模擬查詢延遲
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    private void simulateSlowQuery() {
        try {
            System.out.println("🚧 模擬從 H2 查詢中...");
            Thread.sleep(3000); // 故意延遲 3 秒
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
