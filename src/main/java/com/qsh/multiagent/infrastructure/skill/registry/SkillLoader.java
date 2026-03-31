package com.qsh.multiagent.infrastructure.skill.registry;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class SkillLoader {

    public String loadSkill(String path){
        ClassPathResource resource = new ClassPathResource(path);

        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load skill file: " + path, e);
        }
    }
}
