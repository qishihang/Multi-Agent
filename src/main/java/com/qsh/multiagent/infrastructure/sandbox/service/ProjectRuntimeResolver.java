package com.qsh.multiagent.infrastructure.sandbox.service;

import com.qsh.multiagent.infrastructure.sandbox.model.ProjectRuntimeType;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Component
public class ProjectRuntimeResolver {

    public ProjectRuntimeType resolveRuntimeType(Path workspaceRoot) {
        if (Files.exists(workspaceRoot.resolve("pom.xml"))) {
            return ProjectRuntimeType.JAVA_MAVEN;
        }
        if (Files.exists(workspaceRoot.resolve("build.gradle"))
                || Files.exists(workspaceRoot.resolve("build.gradle.kts"))) {
            return ProjectRuntimeType.JAVA_GRADLE;
        }
        if (Files.exists(workspaceRoot.resolve("package.json"))) {
            return ProjectRuntimeType.NODE;
        }
        if (Files.exists(workspaceRoot.resolve("pyproject.toml"))
                || Files.exists(workspaceRoot.resolve("requirements.txt"))) {
            return ProjectRuntimeType.PYTHON;
        }
        if (Files.exists(workspaceRoot.resolve("go.mod"))) {
            return ProjectRuntimeType.GO;
        }
        if (Files.exists(workspaceRoot.resolve("Cargo.toml"))) {
            return ProjectRuntimeType.RUST;
        }
        return ProjectRuntimeType.UNKNOWN;
    }

    public String describe(ProjectRuntimeType runtimeType) {
        return switch (runtimeType) {
            case JAVA_MAVEN -> "java-maven";
            case JAVA_GRADLE -> "java-gradle";
            case NODE -> "node";
            case PYTHON -> "python";
            case GO -> "go";
            case RUST -> "rust";
            case UNKNOWN -> "unknown";
        };
    }

    public String suggestedCompileCommand(ProjectRuntimeType runtimeType) {
        return switch (runtimeType) {
            case JAVA_MAVEN -> "mvn -q -DskipTests compile";
            case JAVA_GRADLE -> "./gradlew compileJava";
            case RUST -> "cargo check";
            case NODE, PYTHON, GO, UNKNOWN -> "none";
        };
    }

    public String suggestedTestCommand(ProjectRuntimeType runtimeType) {
        return switch (runtimeType) {
            case JAVA_MAVEN -> "mvn -q test";
            case JAVA_GRADLE -> "./gradlew test";
            case NODE -> "npm test";
            case PYTHON -> "pytest";
            case GO -> "go test ./...";
            case RUST -> "cargo test";
            case UNKNOWN -> "unknown";
        };
    }

    public List<String> prepareDependenciesCommand(ProjectRuntimeType runtimeType, Path workspaceRoot) {
        return switch (runtimeType) {
            case JAVA_MAVEN -> preferWrapper(workspaceRoot)
                    ? List.of("./mvnw", "-q", "-DskipTests", "dependency:go-offline")
                    : List.of("mvn", "-q", "-DskipTests", "dependency:go-offline");
            case JAVA_GRADLE -> preferGradleWrapper(workspaceRoot)
                    ? List.of("./gradlew", "dependencies")
                    : List.of("gradle", "dependencies");
            case NODE -> List.of("npm", "install");
            case PYTHON -> pythonPrepareCommand(workspaceRoot);
            case GO -> List.of("go", "mod", "download");
            case RUST -> List.of("cargo", "fetch");
            case UNKNOWN -> List.of();
        };
    }

    public List<String> prepareBuildEnvironmentCommand(ProjectRuntimeType runtimeType, Path workspaceRoot) {
        return switch (runtimeType) {
            case JAVA_MAVEN -> preferWrapper(workspaceRoot)
                    ? List.of("./mvnw", "-q", "-DskipTests", "test-compile")
                    : List.of("mvn", "-q", "-DskipTests", "test-compile");
            case JAVA_GRADLE -> preferGradleWrapper(workspaceRoot)
                    ? List.of("./gradlew", "testClasses")
                    : List.of("gradle", "testClasses");
            case NODE -> List.of("npm", "run", "build");
            case PYTHON -> List.of("python", "-m", "compileall", ".");
            case GO -> List.of("go", "build", "./...");
            case RUST -> List.of("cargo", "check");
            case UNKNOWN -> List.of();
        };
    }

    private boolean preferWrapper(Path workspaceRoot) {
        return Files.exists(workspaceRoot.resolve("mvnw"));
    }

    private boolean preferGradleWrapper(Path workspaceRoot) {
        return Files.exists(workspaceRoot.resolve("gradlew"));
    }

    private List<String> pythonPrepareCommand(Path workspaceRoot) {
        if (Files.exists(workspaceRoot.resolve("requirements.txt"))) {
            return List.of("python", "-m", "pip", "install", "-r", "requirements.txt");
        }
        return List.of("python", "-m", "pip", "install", "-e", ".");
    }
}
