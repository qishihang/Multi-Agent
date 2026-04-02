package com.qsh.multiagent.application.service;

import com.qsh.multiagent.infrastructure.workspace.model.WorkspaceFileEntry;

import java.util.List;

public interface WorkspaceApplicationService {

    void addTextFile(String conversationId, String relativePath, String content);

    List<WorkspaceFileEntry> listFiles(String conversationId);
}
