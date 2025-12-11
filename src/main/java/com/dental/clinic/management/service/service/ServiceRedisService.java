package com.dental.clinic.management.service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_PREFIX = "services::";
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);

    public void cacheService(Long serviceId, Object service) {
        String key = CACHE_PREFIX + serviceId;
        redisTemplate.opsForValue().set(key, service, DEFAULT_TTL);
        log.debug("Cached service: {}", serviceId);
    }

    public Object getService(Long serviceId) {
        String key = CACHE_PREFIX + serviceId;
        Object value = redisTemplate.opsForValue().get(key);
        log.debug("Get service: {} = {}", serviceId, value != null ? "HIT" : "MISS");
        return value;
    }

    public void evictService(Long serviceId) {
        String key = CACHE_PREFIX + serviceId;
        redisTemplate.delete(key);
        log.info("Evicted service cache: {}", serviceId);
    }

    public void evictAllServices() {
        Set<String> keys = redisTemplate.keys(CACHE_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Evicted all service cache ({} keys)", keys.size());
        }
    }

    public void evictServicesByCategory(Long categoryId) {
        String pattern = CACHE_PREFIX + "category:" + categoryId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Evicted services for category: {} ({} keys)", categoryId, keys.size());
        }
    }
}
