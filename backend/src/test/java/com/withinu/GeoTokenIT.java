package com.withinu;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GeoTokenIT extends AbstractIntegrationTest {

    @Test
    void pointInsideCampusReceivesToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/token/geo")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"latitude": 19.2065, "longitude": 72.8748}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.expiresIn").isNumber())
            .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        String token = body.get("token").asText();

        mockMvc.perform(get("/api/v1/rooms")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    void pointOutsideCampusIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/token/geo")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"latitude": 28.6139, "longitude": 77.2090}
                    """))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("OUTSIDE_CAMPUS"));
    }

    @Test
    void invalidLatitudeIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/token/geo")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"latitude": 91.5, "longitude": 72.8748}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void missingCoordinatesAreRejected() throws Exception {
        mockMvc.perform(post("/api/v1/token/geo")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void existingIdentityIsRefreshedNotRecreated() throws Exception {
        MvcResult first = mockMvc.perform(post("/api/v1/token/geo")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"latitude\": 19.2065, \"longitude\": 72.8748}"))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode body = objectMapper.readTree(first.getResponse().getContentAsString());
        String token = body.get("token").asText();

        MvcResult second = mockMvc.perform(post("/api/v1/token/geo")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"latitude\": 19.2066, \"longitude\": 72.8749}"))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode body2 = objectMapper.readTree(second.getResponse().getContentAsString());
        assertThat(body2.get("token").asText()).isNotEqualTo(token);
    }
}