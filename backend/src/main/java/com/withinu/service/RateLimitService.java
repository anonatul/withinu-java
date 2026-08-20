package com.withinu.service;

import com.withinu.config.WithinuProperties;
import com.withinu.exception.ApiException;
import com.withinu.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private static final String USER_KEY_PREFIX = "ratelimit:user:";
    private static final String IP_KEY_PREFIX = "ratelimit:ip:";

    private final StringRedisTemplate redis;
    private final DefaultRedisScript<Long> incrementAndExpireScript;
    private final WithinuProperties props;

    public void checkUserMessageRateLimit(UUID userId) {
        check(USER_KEY_PREFIX + userId, props.ratelimit().userMessagesPerMinute(), 60);
    }

    public void checkIpRateLimit(String ip) {
        check(IP_KEY_PREFIX + ip, props.ratelimit().ipRequestsPerMinute(), 60);
    }

    public boolean isAllowed(String key, long limit, long windowSeconds) {
        Long current = redis.execute(incrementAndExpireScript,
            List.of(key), String.valueOf(windowSeconds));
        return current == null || current <= limit;
    }

    private void check(String key, long limit, long windowSeconds) {
        if (!isAllowed(key, limit, windowSeconds)) {
            throw new ApiException(ErrorCode.RATE_LIMIT_EXCEEDED,
                "Too many requests. Please try again in a minute.");
        }
    }
}