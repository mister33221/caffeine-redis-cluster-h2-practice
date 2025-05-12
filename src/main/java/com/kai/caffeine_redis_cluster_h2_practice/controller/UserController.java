package com.kai.caffeine_redis_cluster_h2_practice.controller;

import com.kai.caffeine_redis_cluster_h2_practice.entity.User;
import com.kai.caffeine_redis_cluster_h2_practice.repository.UserRepository;
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

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }
}
