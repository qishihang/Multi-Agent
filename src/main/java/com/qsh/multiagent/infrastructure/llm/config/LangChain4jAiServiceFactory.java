package com.qsh.multiagent.infrastructure.llm.config;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.stereotype.Component;

@Component
public class LangChain4jAiServiceFactory {

    public <T> T create(Class<T> serviceType,
                        ChatModel chatModel,
                        ChatMemoryProvider chatMemoryProvider,
                        Object... tools) {
        AiServices<T> builder = AiServices.builder(serviceType)
                .chatModel(chatModel)
                .chatMemoryProvider(chatMemoryProvider);

        if (tools != null && tools.length > 0) {
            builder.tools(tools);
        }

        return builder.build();
    }
}
