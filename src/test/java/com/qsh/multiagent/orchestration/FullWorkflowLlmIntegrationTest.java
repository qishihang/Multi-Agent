package com.qsh.multiagent.orchestration;

import com.qsh.multiagent.application.service.ConversationApplicationService;
import com.qsh.multiagent.application.service.ConversationTaskApplicationService;
import com.qsh.multiagent.domain.conversation.Conversation;
import com.qsh.multiagent.domain.task.Task;
import com.qsh.multiagent.domain.task.TaskStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

@SpringBootTest
class FullWorkflowLlmIntegrationTest {

    private static final Set<TaskStatus> TERMINAL_STATUSES = Set.of(
            TaskStatus.COMPLETED,
            TaskStatus.FAILED,
            TaskStatus.MAX_ROUND_REACHED
    );

    @Autowired
    private ConversationApplicationService conversationApplicationService;

    @Autowired
    private ConversationTaskApplicationService conversationTaskApplicationService;

    @Test
    void should_run_full_llm_workflow_from_conversation_entry() {
        Conversation conversation = conversationApplicationService.createConversation();

        Task task = conversationTaskApplicationService.createAndRunTask(
                conversation,
                """
                请围绕一个 Spring Boot 登录接口完成完整多智能体流程：
                1. Planner 生成当前轮计划
                2. Coder 产出实现或代码草案
                3. Reviewer 和 Tester 并发验证
                4. Aggregator 汇总结果并返回最终结论
                """,
                3
        );

        Assertions.assertNotNull(conversation);
        Assertions.assertNotNull(conversation.getId());
        Assertions.assertNotNull(conversation.getWorkspacePath());
        Assertions.assertTrue(Files.exists(Path.of(conversation.getWorkspacePath())));

        Assertions.assertNotNull(task);
        Assertions.assertNotNull(task.getId());
        Assertions.assertEquals(conversation.getId(), task.getConversationId());
        Assertions.assertNotNull(task.getCurrentRound());
        Assertions.assertTrue(task.getCurrentRound() >= 1);
        Assertions.assertTrue(TERMINAL_STATUSES.contains(task.getStatus()));
        Assertions.assertNotNull(task.getFinalSummary());
        Assertions.assertFalse(task.getFinalSummary().isBlank());
    }
}
