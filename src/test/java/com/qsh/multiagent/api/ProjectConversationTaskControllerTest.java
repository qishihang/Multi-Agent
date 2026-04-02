package com.qsh.multiagent.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qsh.multiagent.application.service.ConversationTaskApplicationService;
import com.qsh.multiagent.domain.task.Task;
import com.qsh.multiagent.domain.task.TaskStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
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

    @MockBean
    private ConversationTaskApplicationService conversationTaskApplicationService;

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

        Task mockedTask = new Task();
        mockedTask.setId("task-test-001");
        mockedTask.setConversationId(conversationId);
        mockedTask.setGoal("Create task through explicit project and conversation API");
        mockedTask.setStatus(TaskStatus.COMPLETED);
        mockedTask.setCurrentRound(1);
        mockedTask.setMaxRounds(3);
        mockedTask.setFinalSummary("Mocked task result for explicit API flow.");

        given(conversationTaskApplicationService.createAndRunTask(
                eq(conversationId),
                eq("Create task through explicit project and conversation API"),
                eq(3)
        )).willReturn(mockedTask);

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
                .andExpect(jsonPath("$.taskId").value("task-test-001"))
                .andExpect(jsonPath("$.goal").value("Create task through explicit project and conversation API"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.currentRound").value(1))
                .andExpect(jsonPath("$.maxRounds").value(3))
                .andExpect(jsonPath("$.finalSummary").isNotEmpty());
    }
}
