package com.qsh.multiagent.infrastructure.workspace.service;

import com.qsh.multiagent.infrastructure.workspace.model.WorkspaceFileEntry;
import com.qsh.multiagent.infrastructure.workspace.resolver.WorkspaceResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Component
public class WorkspaceFileService {

    private final WorkspaceResolver workspaceResolver;

    public WorkspaceFileService(WorkspaceResolver workspaceResolver) {
        this.workspaceResolver = workspaceResolver;
    }

    public void writeTextFile(Path workspaceRoot, String relativePath, String content) {
        try {
            Path target = workspaceRoot.resolve(relativePath).normalize();
            workspaceResolver.ensureInsideWorkspace(workspaceRoot, target);

            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            Files.writeString(
                    target,
                    content == null ? "" : content,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write file into workspace: " + relativePath, e);
        }
    }

    public List<WorkspaceFileEntry> listFiles(Path workspaceRoot) {
        try {
            List<WorkspaceFileEntry> result = new ArrayList<>();
            try (Stream<Path> stream = Files.walk(workspaceRoot)) {
                stream.filter(path -> !path.equals(workspaceRoot))
                        .forEach(path -> result.add(new WorkspaceFileEntry(
                                workspaceRoot.relativize(path).toString(),
                                Files.isDirectory(path)
                        )));
            }
            return result;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to list workspace files", e);
        }
    }
}
