package com.withinu;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthorizationIT extends AbstractIntegrationTest {

    private static final String ROOM_ID = "11111111-1111-1111-1111-111111111111";

    private String userToken() throws Exception {
        var result = mockMvc.perform(post("/api/v1/token/geo")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"latitude\": 19.2065, \"longitude\": 72.8748}"))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private String adminToken() throws Exception {
        var result = mockMvc.perform(post("/api/v1/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username": "admin", "password": "admin123"}
                    """))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    void adminLoginWithWrongPasswordFails() throws Exception {
        mockMvc.perform(post("/api/v1/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username": "admin", "password": "wrong"}
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));
    }

    @Test
    void userCannotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard")
                .header("Authorization", "Bearer " + userToken()))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
    }

    @Test
    void adminCanAccessDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard")
                .header("Authorization", "Bearer " + adminToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalRooms").value(6));
    }

    @Test
    void adminCanDeleteAnyMessage() throws Exception {
        String userToken = userToken();
        MvcResult send = mockMvc.perform(post("/api/v1/messages")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"roomId": "%s", "content": "admin target"}""" .formatted(ROOM_ID)))
            .andExpect(status().isOk())
            .andReturn();
        String messageId = objectMapper.readTree(send.getResponse().getContentAsString())
            .get("id").asText();

        mockMvc.perform(delete("/api/v1/admin/messages/" + messageId)
                .header("Authorization", "Bearer " + adminToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/messages")
                .header("Authorization", "Bearer " + userToken)
                .param("roomId", ROOM_ID))
            .andExpect(jsonPath("$.content[0].deleted").value(true));
    }

    @Test
    void userCannotUseAdminMessageDeletion() throws Exception {
        String userToken = userToken();
        MvcResult send = mockMvc.perform(post("/api/v1/messages")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"roomId": "%s", "content": "protected"}""" .formatted(ROOM_ID)))
            .andExpect(status().isOk())
            .andReturn();
        String messageId = objectMapper.readTree(send.getResponse().getContentAsString())
            .get("id").asText();

        mockMvc.perform(delete("/api/v1/admin/messages/" + messageId)
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isForbidden());
    }

    @Test
    void reportFlowWorksEndToEnd() throws Exception {
        String userToken = userToken();
        MvcResult send = mockMvc.perform(post("/api/v1/messages")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"roomId": "%s", "content": "offensive content"}""" .formatted(ROOM_ID)))
            .andExpect(status().isOk())
            .andReturn();
        String messageId = objectMapper.readTree(send.getResponse().getContentAsString())
            .get("id").asText();

        String reporter = userToken();
        MvcResult report = mockMvc.perform(post("/api/v1/reports")
                .header("Authorization", "Bearer " + reporter)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"messageId": "%s", "reason": "ABUSE"}""" .formatted(messageId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andReturn();
        String reportId = objectMapper.readTree(report.getResponse().getContentAsString())
            .get("id").asText();

        String admin = adminToken();
        mockMvc.perform(get("/api/v1/admin/reports")
                .header("Authorization", "Bearer " + admin)
                .param("status", "PENDING"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].reason").value("ABUSE"));

        mockMvc.perform(patch("/api/v1/admin/reports/" + reportId)
                .header("Authorization", "Bearer " + admin)
                .param("status", "RESOLVED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("RESOLVED"));

        mockMvc.perform(get("/api/v1/admin/reports")
                .header("Authorization", "Bearer " + admin)
                .param("status", "PENDING"))
            .andExpect(jsonPath("$.totalElements").value(0));
    }
}