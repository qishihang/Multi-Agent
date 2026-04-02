package com.qsh.multiagent.workspace;

import com.qsh.multiagent.application.service.ConversationApplicationService;
import com.qsh.multiagent.application.service.ProjectApplicationService;
import com.qsh.multiagent.application.service.WorkspaceApplicationService;
import com.qsh.multiagent.domain.conversation.Conversation;
import com.qsh.multiagent.domain.project.Project;
import com.qsh.multiagent.infrastructure.tool.support.WorkspaceTools;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class WorkspaceToolsTest {

    @Autowired
    private ConversationApplicationService conversationApplicationService;

    @Autowired
    private WorkspaceApplicationService workspaceApplicationService;

    @Autowired
    private ProjectApplicationService projectApplicationService;

    @Autowired
    private WorkspaceTools workspaceTools;

    @Test
    void should_list_search_and_read_files_from_workspace() {
        Project project = projectApplicationService.createProject();
        Conversation conversation = conversationApplicationService.createConversation(project.getId());

        workspaceApplicationService.addTextFile(
                conversation.getId(),
                "src/main/java/com/example/demo/LoginService.java",
                "public class LoginService { void login() {} }"
        );

        String files = workspaceTools.listFiles(conversation.getId());
        Assertions.assertTrue(files.contains("LoginService.java"));

        String matches = workspaceTools.searchCode("LoginService", conversation.getId());
        Assertions.assertTrue(matches.contains("LoginService.java"));

        String content = workspaceTools.readFile(
                "src/main/java/com/example/demo/LoginService.java",
                conversation.getId()
        );
        Assertions.assertTrue(content.contains("LoginService"));
    }
}
