package com.dental.clinic.management.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Cache Configuration for Dashboard Statistics
 * Implements caching strategy with different TTLs for different data types
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

    /**
     * Configure Redis Cache Manager with custom TTLs
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default cache configuration (5 minutes TTL)
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        // Custom configurations for different cache types
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Dashboard statistics - 5 minutes
        cacheConfigurations.put("dashboardOverview", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("dashboardRevenue", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("dashboardEmployees", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("dashboardWarehouse", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("dashboardTransactions", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("dashboardHeatmap", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // User preferences - 1 hour (changes less frequently)
        cacheConfigurations.put("dashboardPreferences", defaultConfig.entryTtl(Duration.ofHours(1)));

        // Saved views - 30 minutes
        cacheConfigurations.put("dashboardSavedViews", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
