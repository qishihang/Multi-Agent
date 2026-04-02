package com.qsh.multiagent.infrastructure.sandbox.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SandboxSession {

    private String sessionId;
    private String conversationId;
    private String projectId;
    private String workspaceRoot;
    private String containerName;
    private String image;
    private SandboxSessionStatus status;
    private Instant createdAt;
    private Instant lastUsedAt;
}
