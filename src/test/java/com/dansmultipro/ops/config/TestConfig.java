package com.dansmultipro.ops.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@EnableCaching
@Profile("test")
public class TestConfig {

    @Bean
    CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }
}
