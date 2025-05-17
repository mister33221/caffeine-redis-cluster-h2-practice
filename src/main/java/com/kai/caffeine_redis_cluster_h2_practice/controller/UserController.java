package com.kai.caffeine_redis_cluster_h2_practice.controller;

import com.kai.caffeine_redis_cluster_h2_practice.entity.User;
import com.kai.caffeine_redis_cluster_h2_practice.repository.UserRepository;
import com.kai.caffeine_redis_cluster_h2_practice.service.UserHashCacheService;
import com.kai.caffeine_redis_cluster_h2_practice.service.UserMultiLevelCacheService;
import com.kai.caffeine_redis_cluster_h2_practice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserHashCacheService userHashCacheService;
    private final UserMultiLevelCacheService userMultiLevelCacheService;

    /**
     * 一般的快取，看你的 cacheconfig 是使用哪一種快取策略
     * 若是選擇使用 redis，則會使用 redis object 的方式存入 redis 當作快取
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    /**
     * 使用 Redis Hash 的方式寫入 redis 當作快取
     */
    @GetMapping("/hash/{id}")
    public User getUserFromHash(@PathVariable Long id) {
        return userHashCacheService.getUserById(id);
    }

    /**
     * Caffeine 命中 → 直接回傳
     *
     * Redis Hash 命中 → 寫入 Caffeine → 回傳
     *
     * H2 命中 → 寫入 Redis Hash + Caffeine → 回傳
     * @param id
     * @return
     */
    @GetMapping("/multi/{id}")
    public User getUserMultiLevel(@PathVariable Long id) {
        return userMultiLevelCacheService.getUserById(id);
    }
}
