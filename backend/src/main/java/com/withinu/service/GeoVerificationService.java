package com.withinu.service;

import com.withinu.config.WithinuProperties;
import com.withinu.repository.CampusBoundaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class GeoVerificationService {

    private static final String CACHE_PREFIX = "geoverify:";

    private final CampusBoundaryRepository boundaryRepository;
    private final StringRedisTemplate redis;
    private final WithinuProperties props;

    public boolean isInsideCampus(double latitude, double longitude) {
        if (props.geo().allowAnyLocation()) {
            return true;
        }
        String key = cacheKey(latitude, longitude);
        String cached = redis.opsForValue().get(key);
        if (cached != null) {
            return "1".equals(cached);
        }
        boolean inside = boundaryRepository.existsActiveBoundaryContaining(latitude, longitude);
        redis.opsForValue().set(key, inside ? "1" : "0", props.geo().verificationCacheTtl());
        return inside;
    }

    private String cacheKey(double latitude, double longitude) {
        return CACHE_PREFIX + String.format("%.3f", latitude) + ":" + String.format("%.3f", longitude);
    }
}