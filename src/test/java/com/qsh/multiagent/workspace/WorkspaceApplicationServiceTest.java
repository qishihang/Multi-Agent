package com.qsh.multiagent.workspace;

import com.qsh.multiagent.application.service.ConversationApplicationService;
import com.qsh.multiagent.application.service.ProjectApplicationService;
import com.qsh.multiagent.application.service.WorkspaceApplicationService;
import com.qsh.multiagent.domain.conversation.Conversation;
import com.qsh.multiagent.domain.project.Project;
import com.qsh.multiagent.infrastructure.workspace.model.WorkspaceFileEntry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class WorkspaceApplicationServiceTest {

    @Autowired
    private ConversationApplicationService conversationApplicationService;

    @Autowired
    private WorkspaceApplicationService workspaceApplicationService;

    @Autowired
    private ProjectApplicationService projectApplicationService;

    @Test
    void should_add_file_into_project_workspace() {
        Project project = projectApplicationService.createProject();
        Conversation conversation = conversationApplicationService.createConversation(project.getId());

        workspaceApplicationService.addTextFile(
                conversation.getId(),
                "src/main/java/com/example/demo/Demo.java",
                "public class Demo {}"
        );

        List<WorkspaceFileEntry> files = workspaceApplicationService.listFiles(conversation.getId());

        Assertions.assertNotNull(files);
        Assertions.assertFalse(files.isEmpty());
        boolean found = files.stream().anyMatch(file -> file.getRelativePath().contains("Demo.java"));
        Assertions.assertTrue(found);
    }
}
