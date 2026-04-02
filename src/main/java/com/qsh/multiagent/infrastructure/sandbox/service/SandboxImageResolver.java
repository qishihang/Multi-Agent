package com.qsh.multiagent.infrastructure.sandbox.service;

import com.qsh.multiagent.infrastructure.sandbox.model.ProjectRuntimeType;
import org.springframework.stereotype.Component;

@Component
public class SandboxImageResolver {

    public String resolveImage(ProjectRuntimeType runtimeType) {
        return switch (runtimeType) {
            case JAVA_MAVEN -> "maven:3.9.9-eclipse-temurin-17";
            case JAVA_GRADLE -> "gradle:8.10.2-jdk17";
            case NODE -> "node:22";
            case PYTHON -> "python:3.11";
            case GO -> "golang:1.24";
            case RUST -> "rust:1.86";
            case UNKNOWN -> "ubuntu:24.04";
        };
    }
}
