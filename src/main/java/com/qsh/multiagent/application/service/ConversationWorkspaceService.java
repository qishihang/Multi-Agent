package com.qsh.multiagent.application.service;

import com.qsh.multiagent.domain.conversation.Conversation;
import com.qsh.multiagent.infrastructure.workspace.model.WorkspaceFileEntry;

import java.util.List;

public interface ConversationWorkspaceService {

    void addTextFile(Conversation conversation, String relativePath, String content);

    List<WorkspaceFileEntry> listFiles(Conversation conversation);
}
