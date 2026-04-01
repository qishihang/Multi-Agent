package com.qsh.multiagent.domain.context;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExecutionContextTest {

    @Test
    void should_allow_and_check_tool_capabilities() {
        ExecutionContext executionContext = new ExecutionContext();

        Assertions.assertFalse(executionContext.allowsTool(ToolCapability.WORKSPACE_READ));

        executionContext.allowTool(ToolCapability.WORKSPACE_READ);
        executionContext.allowTool(ToolCapability.WORKSPACE_WRITE);

        Assertions.assertTrue(executionContext.allowsTool(ToolCapability.WORKSPACE_READ));
        Assertions.assertTrue(executionContext.allowsTool(ToolCapability.WORKSPACE_WRITE));
        Assertions.assertFalse(executionContext.allowsTool(ToolCapability.WORKSPACE_TEST));
    }

    @Test
    void should_store_and_read_typed_metadata() {
        ExecutionContext executionContext = new ExecutionContext();

        executionContext.putMetadata("projectType", "maven");
        executionContext.putMetadata("retryCount", 2);

        Assertions.assertEquals("maven", executionContext.getMetadata("projectType", String.class));
        Assertions.assertEquals(2, executionContext.getMetadata("retryCount", Integer.class));
        Assertions.assertNull(executionContext.getMetadata("projectType", Integer.class));
        Assertions.assertNull(executionContext.getMetadata("missingKey", String.class));
    }

    @Test
    void should_default_to_workspace_sandbox_policy() {
        ExecutionContext executionContext = new ExecutionContext();

        Assertions.assertEquals(ExecutionSandboxPolicy.WORKSPACE_DEFAULT, executionContext.getSandboxPolicy());
    }
}
