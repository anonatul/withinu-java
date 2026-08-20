package com.withinu;

import com.withinu.service.RateLimitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = "withinu.ratelimit.user-messages-per-minute=3")
class RateLimitIT extends AbstractIntegrationTest {

    private static final String ROOM_ID = "11111111-1111-1111-1111-111111111111";

    @Autowired
    private RateLimitService rateLimitService;

    private String token() throws Exception {
        var result = mockMvc.perform(post("/api/v1/token/geo")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"latitude\": 19.2065, \"longitude\": 72.8748}"))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    void requestsUnderLimitAreAllowed() {
        assertThat(rateLimitService.isAllowed("ratelimit:test:key", 3, 60)).isTrue();
        assertThat(rateLimitService.isAllowed("ratelimit:test:key", 3, 60)).isTrue();
        assertThat(rateLimitService.isAllowed("ratelimit:test:key", 3, 60)).isTrue();
    }

    @Test
    void requestsOverLimitAreBlocked() {
        assertThat(rateLimitService.isAllowed("ratelimit:test:key2", 2, 60)).isTrue();
        assertThat(rateLimitService.isAllowed("ratelimit:test:key2", 2, 60)).isTrue();
        assertThat(rateLimitService.isAllowed("ratelimit:test:key2", 2, 60)).isFalse();
    }

    @Test
    void windowExpiresAfterTimeout() throws InterruptedException {
        String key = "ratelimit:test:key3";
        assertThat(rateLimitService.isAllowed(key, 1, 1)).isTrue();
        assertThat(rateLimitService.isAllowed(key, 1, 1)).isFalse();
        Thread.sleep(1100);
        assertThat(rateLimitService.isAllowed(key, 1, 1)).isTrue();
    }

    @Test
    void messageApiReturns429OverUserLimit() throws Exception {
        String token = token();
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/v1/messages")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"roomId": "%s", "content": "msg-%d"}""" .formatted(ROOM_ID, i)))
                .andExpect(status().isOk());
        }

        mockMvc.perform(post("/api/v1/messages")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"roomId": "%s", "content": "too many"}""" .formatted(ROOM_ID)))
            .andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.errorCode").value("RATE_LIMIT_EXCEEDED"));
    }
}