package com.qsh.multiagent.conversation;

import com.qsh.multiagent.application.service.ConversationApplicationService;
import com.qsh.multiagent.application.service.ConversationTaskApplicationService;
import com.qsh.multiagent.domain.conversation.Conversation;
import com.qsh.multiagent.domain.task.Task;
import com.qsh.multiagent.domain.task.TaskStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ConversationTaskApplicationServiceTest {

    @Autowired
    private ConversationApplicationService conversationApplicationService;

    @Autowired
    private ConversationTaskApplicationService conversationTaskApplicationService;

    @Test
    void should_create_and_run_task_in_conversation() {
        Conversation conversation = conversationApplicationService.createConversation();

        Task task = conversationTaskApplicationService.createAndRunTask(
                conversation,
                "请为一个登录功能生成当前轮计划与编码结果，使用java",
                3
        );

        Assertions.assertNotNull(task);
        Assertions.assertNotNull(task.getId());
        Assertions.assertEquals(conversation.getId(), task.getConversationId());
        Assertions.assertNotEquals(TaskStatus.CREATED, task.getStatus());
    }
}
