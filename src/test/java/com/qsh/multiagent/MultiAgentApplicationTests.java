package com.qsh.multiagent;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.spring.AiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import dev.langchain4j.model.openai.OpenAiChatModel;

import java.sql.SQLOutput;

@SpringBootTest
class MultiAgentApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    public void helloword(){
        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl("http://langchain4j.dev/demo/openai/v1")
                .modelName("gpt-4o-mini")
                .build();
        String answer = model.chat("你是谁");
        System.out.println(answer);
    }

    @Autowired OpenAiChatModel openAiChatModel;
    @Test
    public void testSpringBoot(){
        String anwser = openAiChatModel.chat("你是谁");
        System.out.println(anwser);
    }

    @Autowired QwenChatModel qwenChatModel;
    @Test
    public void testDashscope(){
        String anwser = qwenChatModel.chat("你是谁");
        System.out.println(anwser);
    }


    @Autowired Assistant assistant;
    @Test
    public  void testAiService(){
        String anwser = assistant.chat("你是谁");
        System.out.println(anwser);
    }

    @Test
    public void testMemory(){
        MessageWindowChatMemory messageWindowChatMemory = MessageWindowChatMemory.withMaxMessages(10);

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(qwenChatModel)
                .chatMemory(messageWindowChatMemory)
                .build();
        String answer = assistant.chat("我是qsh");
        System.out.println(answer);

        String answer2 = assistant.chat("我是谁");
        System.out.println(answer2);
    }
}
