package com.withinu.controller;

import com.withinu.dto.HealthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redis;

    @GetMapping
    public HealthResponse health() {
        String database = "UP";
        String redisStatus = "UP";
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        } catch (Exception e) {
            database = "DOWN";
        }
        try {
            redis.getConnectionFactory().getConnection().ping();
        } catch (RedisConnectionFailureException e) {
            redisStatus = "DOWN";
        }
        boolean up = "UP".equals(database) && "UP".equals(redisStatus);
        return new HealthResponse(up ? "UP" : "DEGRADED", database, redisStatus);
    }
}