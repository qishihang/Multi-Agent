package com.qsh.multiagent.domain.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExecutionContext {

    private String conversationId;
    private String taskId;
    private String runId;
    private String workspaceRoot;
    private String memoryScope;
    private ExecutionSandboxPolicy sandboxPolicy = ExecutionSandboxPolicy.WORKSPACE_DEFAULT;
    private Set<ToolCapability> allowedToolCapabilities = EnumSet.noneOf(ToolCapability.class);
    private Map<String, Object> metadata = new HashMap<>();

    public boolean allowsTool(ToolCapability capability) {
        return capability != null
                && allowedToolCapabilities != null
                && allowedToolCapabilities.contains(capability);
    }

    public void allowTool(ToolCapability capability) {
        if (capability == null) {
            return;
        }
        if (allowedToolCapabilities == null) {
            allowedToolCapabilities = EnumSet.noneOf(ToolCapability.class);
        }
        allowedToolCapabilities.add(capability);
    }

    public void putMetadata(String key, Object value) {
        if (key == null || key.isBlank()) {
            return;
        }
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }

    public Object getMetadata(String key) {
        if (metadata == null || key == null || key.isBlank()) {
            return null;
        }
        return metadata.get(key);
    }

    public <T> T getMetadata(String key, Class<T> targetType) {
        Object value = getMetadata(key);
        if (value == null || targetType == null || !targetType.isInstance(value)) {
            return null;
        }
        return targetType.cast(value);
    }
}
