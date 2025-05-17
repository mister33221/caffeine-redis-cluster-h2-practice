package com.kai.caffeine_redis_cluster_h2_practice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CaffeineRedisClusterH2PracticeApplication {

	public static void main(String[] args) {
		SpringApplication.run(CaffeineRedisClusterH2PracticeApplication.class, args);
	}

}
