package com.qsh.multiagent.api.controller;

import com.qsh.multiagent.api.request.CreateTaskRequest;
import com.qsh.multiagent.api.response.TaskResponse;
import com.qsh.multiagent.application.service.ConversationTaskApplicationService;
import com.qsh.multiagent.domain.task.Task;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conversations/{conversationId}/tasks")
public class ConversationTaskController {

    private final ConversationTaskApplicationService conversationTaskApplicationService;

    public ConversationTaskController(ConversationTaskApplicationService conversationTaskApplicationService) {
        this.conversationTaskApplicationService = conversationTaskApplicationService;
    }

    @PostMapping
    public TaskResponse createTask(@PathVariable String conversationId,
                                   @RequestBody @Validated CreateTaskRequest request) {
        Task task = conversationTaskApplicationService.createAndRunTask(
                conversationId,
                request.getGoal(),
                request.getMaxRounds()
        );
        return new TaskResponse(
                task.getId(),
                task.getGoal(),
                task.getStatus(),
                task.getCurrentRound(),
                task.getMaxRounds(),
                task.getFinalSummary()
        );
    }
}
