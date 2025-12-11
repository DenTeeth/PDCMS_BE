package com.dental.clinic.management.booking_appointment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_PREFIX = "appointments::";
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(3);

    public void cacheAppointment(Long appointmentId, Object appointment) {
        String key = CACHE_PREFIX + appointmentId;
        redisTemplate.opsForValue().set(key, appointment, DEFAULT_TTL);
        log.debug("Cached appointment: {}", appointmentId);
    }

    public Object getAppointment(Long appointmentId) {
        String key = CACHE_PREFIX + appointmentId;
        Object value = redisTemplate.opsForValue().get(key);
        log.debug("Get appointment: {} = {}", appointmentId, value != null ? "HIT" : "MISS");
        return value;
    }

    public void evictAppointment(Long appointmentId) {
        String key = CACHE_PREFIX + appointmentId;
        redisTemplate.delete(key);
        log.info("Evicted appointment cache: {}", appointmentId);
    }

    public void evictAllAppointments() {
        Set<String> keys = redisTemplate.keys(CACHE_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Evicted all appointment cache ({} keys)", keys.size());
        }
    }

    public void evictAppointmentsByPatient(Long patientId) {
        String pattern = CACHE_PREFIX + "patient:" + patientId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Evicted appointments for patient: {} ({} keys)", patientId, keys.size());
        }
    }
}
