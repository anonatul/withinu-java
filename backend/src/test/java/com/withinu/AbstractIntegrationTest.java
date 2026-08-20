package com.withinu;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> POSTGIS = new PostgreSQLContainer<>(
        DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres"));
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);

    static {
        POSTGIS.start();
        REDIS.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGIS::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGIS::getUsername);
        registry.add("spring.datasource.password", POSTGIS::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        registry.add("withinu.ratelimit.ip-requests-per-minute", () -> 100000);
        registry.add("withinu.jwt.secret", () -> "test-secret-key-that-is-long-enough-for-hs256-signing-0123456789");
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected StringRedisTemplate redis;

    @BeforeEach
    void flushRedis() {
        redis.getConnectionFactory().getConnection().serverCommands().flushAll();
    }
}