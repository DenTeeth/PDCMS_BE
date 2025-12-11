package com.dental.clinic.management.patient.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_PREFIX = "patients::";
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);

    public void cachePatient(Long patientId, Object patient) {
        String key = CACHE_PREFIX + patientId;
        redisTemplate.opsForValue().set(key, patient, DEFAULT_TTL);
        log.debug("Cached patient: {}", patientId);
    }

    public Object getPatient(Long patientId) {
        String key = CACHE_PREFIX + patientId;
        Object value = redisTemplate.opsForValue().get(key);
        log.debug("Get patient: {} = {}", patientId, value != null ? "HIT" : "MISS");
        return value;
    }

    public void evictPatient(Long patientId) {
        String key = CACHE_PREFIX + patientId;
        redisTemplate.delete(key);
        log.info("Evicted patient cache: {}", patientId);
    }

    public void evictAllPatients() {
        Set<String> keys = redisTemplate.keys(CACHE_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Evicted all patient cache ({} keys)", keys.size());
        }
    }

    public void evictPatientList() {
        String pattern = CACHE_PREFIX + "list:*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Evicted patient list cache ({} keys)", keys.size());
        }
    }
}
