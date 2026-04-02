package com.qsh.multiagent.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectConversationTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void should_create_project_then_conversation_then_task_via_explicit_entrypoints() throws Exception {
        String projectResponse = mockMvc.perform(post("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").isNotEmpty())
                .andExpect(jsonPath("$.workspacePath").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode projectJson = objectMapper.readTree(projectResponse);
        String projectId = projectJson.get("projectId").asText();
        Assertions.assertFalse(projectId.isBlank());

        String conversationResponse = mockMvc.perform(post("/api/projects/{projectId}/conversations", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").isNotEmpty())
                .andExpect(jsonPath("$.projectId").value(projectId))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode conversationJson = objectMapper.readTree(conversationResponse);
        String conversationId = conversationJson.get("conversationId").asText();
        Assertions.assertFalse(conversationId.isBlank());

        String requestBody = """
                {
                  "goal": "Create task through explicit project and conversation API",
                  "maxRounds": 3
                }
                """;

        mockMvc.perform(post("/api/conversations/{conversationId}/tasks", conversationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").isNotEmpty())
                .andExpect(jsonPath("$.goal").value("Create task through explicit project and conversation API"))
                .andExpect(jsonPath("$.finalSummary").isNotEmpty());
    }
}
