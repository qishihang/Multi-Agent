package com.qsh.multiagent.agent.common;

import com.qsh.multiagent.domain.artifact.Artifact;
import com.qsh.multiagent.domain.context.ExecutionContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentTask {
    private String taskId;
    private String planId;
    private Integer round;
    private AgentType targetAgentType;
    private String objective;
    private String taskGoal;
    private String input;

    /**
     * 统一运行时上下文，承载会话、工作空间、memory scope 与能力边界。
     */
    private ExecutionContext executionContext;

    /**
     * 标准输入工件列表，是 Agent 协作的主输入载体。
     */
    private List<Artifact> inputArtifacts = new ArrayList<>();

    /**
     * 约束信息，用于承载阶段性扩展字段，避免过早设计过重的专用对象。
     */
    private Map<String, Object> constraints = new LinkedHashMap<>();

    public void addInputArtifact(Artifact artifact) {
        if (artifact == null) {
            return;
        }
        if (inputArtifacts == null) {
            inputArtifacts = new ArrayList<>();
        }
        inputArtifacts.add(artifact);
    }

    public void putConstraint(String key, Object value) {
        if (key == null || key.isBlank()) {
            return;
        }
        if (constraints == null) {
            constraints = new LinkedHashMap<>();
        }
        constraints.put(key, value);
    }

    public Object getConstraint(String key) {
        if (constraints == null || key == null || key.isBlank()) {
            return null;
        }
        return constraints.get(key);
    }

    public <T extends Artifact> T findInputArtifact(Class<T> artifactClass) {
        if (artifactClass == null || inputArtifacts == null) {
            return null;
        }
        for (Artifact artifact : inputArtifacts) {
            if (artifactClass.isInstance(artifact)) {
                return artifactClass.cast(artifact);
            }
        }
        return null;
    }

    public String getConversationId() {
        return executionContext == null ? null : executionContext.getConversationId();
    }

    public String getMemoryScope() {
        return executionContext == null ? null : executionContext.getMemoryScope();
    }

    public String getRunId() {
        return executionContext == null ? null : executionContext.getRunId();
    }
}
