package com.kai.caffeine_redis_cluster_h2_practice.repository;

import com.kai.caffeine_redis_cluster_h2_practice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
