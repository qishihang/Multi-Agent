package com.qsh.multiagent.infrastructure.llm.config;

import com.qsh.multiagent.infrastructure.llm.service.TestAiService;
import com.qsh.multiagent.infrastructure.tool.support.TestTools;
import com.qsh.multiagent.infrastructure.tool.support.WorkspaceTools;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestAiServiceConfig {

    @Bean
    public TestAiService testAiService(@Qualifier("qwenChatModel") ChatModel qwenChatModel,
                                       WorkspaceTools workspaceTools,
                                       TestTools testTools) {
        return AiServices.builder(TestAiService.class)
                .chatModel(qwenChatModel)
                .tools(workspaceTools, testTools)
                .build();
    }
}
