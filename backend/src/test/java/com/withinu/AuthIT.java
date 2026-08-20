package com.withinu;

import com.withinu.config.WithinuProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIT extends AbstractIntegrationTest {

    @Autowired
    private WithinuProperties props;

    private String validUserToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/token/geo")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"latitude\": 19.2065, \"longitude\": 72.8748}"))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
            .get("token").asText();
    }

    @Test
    void noTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/rooms"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    @Test
    void invalidTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/rooms")
                .header("Authorization", "Bearer not-a-jwt"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.errorCode").value("INVALID_TOKEN"));
    }

    @Test
    void expiredTokenIsRejected() throws Exception {
        String expired = Jwts.builder()
            .subject(UUID.randomUUID().toString())
            .claim("type", "USER")
            .claim("ver", 0)
            .issuedAt(new Date(System.currentTimeMillis() - 7200_000))
            .expiration(new Date(System.currentTimeMillis() - 3600_000))
            .signWith(Keys.hmacShaKeyFor(props.jwt().secret().getBytes(StandardCharsets.UTF_8)))
            .compact();

        mockMvc.perform(get("/api/v1/rooms")
                .header("Authorization", "Bearer " + expired))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.errorCode").value("TOKEN_EXPIRED"));
    }

    @Test
    void validTokenGrantsAccess() throws Exception {
        mockMvc.perform(get("/api/v1/rooms")
                .header("Authorization", "Bearer " + validUserToken()))
            .andExpect(status().isOk());
    }
}