package com.withinu.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "withinu")
public record WithinuProperties(
    Jwt jwt,
    Cors cors,
    RateLimit ratelimit,
    Geo geo
) {
    public record Jwt(String secret, Duration userTokenTtl, Duration adminTokenTtl) {
    }

    public record Cors(List<String> allowedOrigins) {
    }

    public record RateLimit(int userMessagesPerMinute, int ipRequestsPerMinute) {
    }

    public record Geo(Duration verificationCacheTtl, String campusBoundaryWkt, boolean allowAnyLocation) {
    }
}