package com.dental.clinic.management.config;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableCaching
public class RedisConfig {

        /**
         * Create ObjectMapper with JavaTimeModule for LocalDateTime serialization
         */
        private ObjectMapper createObjectMapper() {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(),
                                ObjectMapper.DefaultTyping.NON_FINAL);
                return objectMapper;
        }

        @Bean
        public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
                RedisTemplate<String, Object> template = new RedisTemplate<>();
                template.setConnectionFactory(connectionFactory);

                GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(
                                createObjectMapper());

                template.setKeySerializer(new StringRedisSerializer());
                template.setValueSerializer(serializer);

                template.setHashKeySerializer(new StringRedisSerializer());
                template.setHashValueSerializer(serializer);

                template.afterPropertiesSet();
                return template;
        }

        @Bean
        @Primary
        public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
                try {
                        // Test Redis connection
                        connectionFactory.getConnection().ping();

                        log.info("✅ Redis connected successfully - using Redis cache");

                        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(
                                        createObjectMapper());

                        // Default cache configuration (30 minutes TTL)
                        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                                        .entryTtl(Duration.ofMinutes(30))
                                        .serializeKeysWith(
                                                        RedisSerializationContext.SerializationPair
                                                                        .fromSerializer(new StringRedisSerializer()))
                                        .serializeValuesWith(
                                                        RedisSerializationContext.SerializationPair
                                                                        .fromSerializer(serializer))
                                        .disableCachingNullValues();

                        // Custom configurations for different cache types
                        java.util.Map<String, RedisCacheConfiguration> cacheConfigurations = new java.util.HashMap<>();

                        // Dashboard statistics - varying TTLs
                        cacheConfigurations.put("dashboardOverview", defaultConfig.entryTtl(Duration.ofMinutes(5)));
                        cacheConfigurations.put("dashboardRevenue", defaultConfig.entryTtl(Duration.ofMinutes(5)));
                        cacheConfigurations.put("dashboardEmployees", defaultConfig.entryTtl(Duration.ofMinutes(10)));
                        cacheConfigurations.put("dashboardWarehouse", defaultConfig.entryTtl(Duration.ofMinutes(15)));
                        cacheConfigurations.put("dashboardTransactions", defaultConfig.entryTtl(Duration.ofMinutes(5)));
                        cacheConfigurations.put("dashboardHeatmap", defaultConfig.entryTtl(Duration.ofMinutes(30)));
                        cacheConfigurations.put("dashboardPreferences", defaultConfig.entryTtl(Duration.ofHours(1)));
                        cacheConfigurations.put("dashboardSavedViews", defaultConfig.entryTtl(Duration.ofMinutes(30)));

                        return RedisCacheManager.builder(connectionFactory)
                                        .cacheDefaults(defaultConfig)
                                        .withInitialCacheConfigurations(cacheConfigurations)
                                        .transactionAware()
                                        .build();
                } catch (RedisConnectionFailureException e) {
                        log.warn("⚠️ Redis unavailable - falling back to in-memory cache: {}", e.getMessage());
                        return fallbackCacheManager();
                } catch (Exception e) {
                        log.warn("⚠️ Redis connection error - falling back to in-memory cache: {}", e.getMessage());
                        return fallbackCacheManager();
                }
        }

        /**
         * Fallback to in-memory cache when Redis is unavailable
         */
        private CacheManager fallbackCacheManager() {
                log.info("📦 Using ConcurrentMapCacheManager (in-memory) as fallback");
                // Define ALL cache names used in @Cacheable annotations across the application
                return new ConcurrentMapCacheManager(
                                "roles", // RoleService.getAllRoles(), getEmployeeAssignableRoles()
                                "roleById", // RoleService.getRoleById()
                                "rolePermissions", // RoleService.getRolePermissions()
                                "permissions", // PermissionService.getAllActivePermissions()
                                "permissionById", // PermissionService.getPermissionById()
                                "permissionsByModule", // PermissionService.getPermissionsByModule()
                                "permissionsGrouped", // PermissionService.getPermissionsGroupedByModule(),
                                                      // getPermissionHierarchy()
                                "sidebar", // SidebarService.getSidebarData()
                                // Dashboard caches
                                "dashboardOverview",
                                "dashboardRevenue",
                                "dashboardEmployees",
                                "dashboardWarehouse",
                                "dashboardTransactions",
                                "dashboardHeatmap",
                                "dashboardPreferences",
                                "dashboardSavedViews"
                );
        }
}
