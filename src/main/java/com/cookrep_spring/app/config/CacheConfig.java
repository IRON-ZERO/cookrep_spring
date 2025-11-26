package com.cookrep_spring.app.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public Cache<String, Boolean> viewCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS) // TTL: 필요에 따라 조정
                .maximumSize(200_000)
                .build();
    }
}