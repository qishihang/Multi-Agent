package com.qsh.multiagent.api.controller;

import com.qsh.multiagent.api.request.CreateTaskRequest;
import com.qsh.multiagent.api.response.TaskResponse;
import com.qsh.multiagent.application.service.TaskApplicationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskApplicationService taskApplicationService;

    public TaskController(TaskApplicationService taskApplicationService) {
        this.taskApplicationService = taskApplicationService;
    }

    @PostMapping
    public TaskResponse createTask(@RequestBody @Validated CreateTaskRequest request) {
        return taskApplicationService.createAndRunTask(request);
    }
}
