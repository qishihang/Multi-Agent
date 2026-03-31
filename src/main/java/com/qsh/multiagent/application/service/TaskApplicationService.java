package com.qsh.multiagent.application.service;

import com.qsh.multiagent.api.request.CreateTaskRequest;
import com.qsh.multiagent.api.response.TaskResponse;

@Deprecated
public interface TaskApplicationService {

    TaskResponse createAndRunTask(CreateTaskRequest request);
}
