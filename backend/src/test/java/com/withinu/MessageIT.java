package com.withinu;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = "withinu.ratelimit.user-messages-per-minute=100")
class MessageIT extends AbstractIntegrationTest {

    private static final String ROOM_ID = "11111111-1111-1111-1111-111111111111";

    private String token() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/token/geo")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"latitude\": 19.2065, \"longitude\": 72.8748}"))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    void sendValidMessage() throws Exception {
        mockMvc.perform(post("/api/v1/messages")
                .header("Authorization", "Bearer " + token())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roomId\": \"%s\", \"content\": \"  Hello   everyone  \"}" .formatted(ROOM_ID)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("Hello everyone"))
            .andExpect(jsonPath("$.displayName").value(org.hamcrest.Matchers.startsWith("Anonymous #")))
            .andExpect(jsonPath("$.mine").value(true))
            .andExpect(jsonPath("$.deleted").value(false));
    }

    @Test
    void emptyMessageIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/messages")
                .header("Authorization", "Bearer " + token())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"roomId": "%s", "content": "   """ .formatted(ROOM_ID) + "}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void tooLongMessageIsRejected() throws Exception {
        String content = "a".repeat(1001);
        mockMvc.perform(post("/api/v1/messages")
                .header("Authorization", "Bearer " + token())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"roomId": "%s", "content": "%s"  """ .formatted(ROOM_ID, content) + "}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("MESSAGE_TOO_LONG"));
    }

    @Test
    void invalidRoomIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/messages")
                .header("Authorization", "Bearer " + token())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"roomId": "99999999-9999-9999-9999-999999999999", "content": "hi"}
                    """))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorCode").value("ROOM_NOT_FOUND"));
    }

    @Test
    void unauthorizedMessageIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"roomId": "%s", "content": "hi"}""" .formatted(ROOM_ID)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void messagesArePaginatedNewestFirst() throws Exception {
        String token = token();
        for (int i = 0; i < 35; i++) {
            mockMvc.perform(post("/api/v1/messages")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"roomId": "%s", "content": "msg-%d"}""" .formatted(ROOM_ID, i)))
                .andExpect(status().isOk());
        }

        MvcResult result = mockMvc.perform(get("/api/v1/messages")
                .header("Authorization", "Bearer " + token)
                .param("roomId", ROOM_ID)
                .param("page", "0")
                .param("size", "30"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(30))
            .andExpect(jsonPath("$.totalElements").value(35))
            .andExpect(jsonPath("$.hasNext").value(true))
            .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("content").get(0).get("content").asText()).isEqualTo("msg-34");
        assertThat(body.get("content").get(29).get("content").asText()).isEqualTo("msg-5");

        mockMvc.perform(get("/api/v1/messages")
                .header("Authorization", "Bearer " + token)
                .param("roomId", ROOM_ID)
                .param("page", "1")
                .param("size", "30"))
            .andExpect(jsonPath("$.content.length()").value(5))
            .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void pageSizeIsCappedAtFifty() throws Exception {
        mockMvc.perform(get("/api/v1/messages")
                .header("Authorization", "Bearer " + token())
                .param("roomId", ROOM_ID)
                .param("size", "500"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size").value(50));
    }

    @Test
    void userCanDeleteOwnMessage() throws Exception {
        String token = token();
        MvcResult send = mockMvc.perform(post("/api/v1/messages")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"roomId": "%s", "content": "delete me"}""" .formatted(ROOM_ID)))
            .andExpect(status().isOk())
            .andReturn();
        String messageId = objectMapper.readTree(send.getResponse().getContentAsString())
            .get("id").asText();

        mockMvc.perform(delete("/api/v1/messages/" + messageId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/messages")
                .header("Authorization", "Bearer " + token)
                .param("roomId", ROOM_ID))
            .andExpect(jsonPath("$.content[0].deleted").value(true))
            .andExpect(jsonPath("$.content[0].content").doesNotExist());
    }

    @Test
    void userCannotDeleteOthersMessage() throws Exception {
        String sender = token();
        MvcResult send = mockMvc.perform(post("/api/v1/messages")
                .header("Authorization", "Bearer " + sender)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"roomId": "%s", "content": "not yours"}""" .formatted(ROOM_ID)))
            .andExpect(status().isOk())
            .andReturn();
        String messageId = objectMapper.readTree(send.getResponse().getContentAsString())
            .get("id").asText();

        String other = token();
        mockMvc.perform(delete("/api/v1/messages/" + messageId)
                .header("Authorization", "Bearer " + other))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
    }
}