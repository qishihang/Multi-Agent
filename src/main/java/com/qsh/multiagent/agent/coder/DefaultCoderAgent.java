package com.qsh.multiagent.agent.coder;

import com.qsh.multiagent.agent.common.Agent;
import com.qsh.multiagent.agent.common.AgentResult;
import com.qsh.multiagent.agent.common.AgentTask;
import com.qsh.multiagent.agent.common.AgentType;
import com.qsh.multiagent.domain.artifact.CodeArtifact;
import com.qsh.multiagent.domain.artifact.PlanArtifact;
import com.qsh.multiagent.infrastructure.llm.prompt.CoderPromptBuilder;
import com.qsh.multiagent.infrastructure.llm.service.CoderAiService;
import com.qsh.multiagent.infrastructure.llm.service.CoderGenerationOutput;
import com.qsh.multiagent.infrastructure.skill.registry.SkillLoader;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Primary
public class DefaultCoderAgent implements Agent {

    private static final String CODER_SKILL_PATH = "skills/coder-skill.md";

    private final CoderAiService coderAiService;
    private final CoderPromptBuilder coderPromptBuilder;
    private final SkillLoader skillLoader;

    public DefaultCoderAgent(CoderAiService coderAiService,
                            CoderPromptBuilder coderPromptBuilder,
                            SkillLoader skillLoader) {
        this.coderAiService = coderAiService;
        this.coderPromptBuilder = coderPromptBuilder;
        this.skillLoader = skillLoader;
    }

    @Override
    public AgentType getType() {
        return AgentType.CODER;
    }

    @Override
    public AgentResult execute(AgentTask task) {
        String skillContent = skillLoader.loadSkill(CODER_SKILL_PATH);
        PlanArtifact planArtifact = requirePlanArtifact(task);

        String prompt = coderPromptBuilder.buildUserPrompt(
                task,
                planArtifact,
                skillContent
        );

        CoderGenerationOutput output = coderAiService.generate(
                buildExecutionMemoryId(task),
                prompt
        );

        AgentResult result = new AgentResult(
                task.getTaskId(),
                task.getPlanId(),
                task.getRound(),
                getType(),
                Boolean.TRUE.equals(output.passed()),
                output.changeSummary(),
                null,
                null,
                null,
                null
        );
        CodeArtifact codeArtifact = buildCodeArtifact(task, output);
        result.addOutputArtifact(codeArtifact);
        result.setRawEvidence(output.codeDraft());

        if (output.risks() != null && !output.risks().isBlank()) {
            result.addIssue(output.risks());
        }

        return result;
    }

    private String buildExecutionMemoryId(AgentTask task) {
        if (task.getMemoryScope() != null && !task.getMemoryScope().isBlank()) {
            return task.getMemoryScope();
        }
        return "%s::coder::task-%s::round-%s".formatted(
                task.getConversationId(),
                task.getTaskId(),
                task.getRound()
        );
    }

    private CodeArtifact buildCodeArtifact(AgentTask task,
                                           CoderGenerationOutput output) {
        CodeArtifact artifact = new CodeArtifact(
                buildCodeArtifactId(task),
                resolveConversationId(task),
                task.getTaskId(),
                task.getRunId(),
                task.getRound(),
                AgentType.CODER,
                Instant.now()
        );
        artifact.setFilesWritten(Boolean.TRUE.equals(output.filesWritten()));
        artifact.setChangeSummary(output.changeSummary());
        artifact.setCodeDraft(output.codeDraft());
        artifact.setRisks(output.risks());
        artifact.setReviewFocusHint("Focus on files changed in this round and confirm alignment with the current plan.");
        artifact.setTestFocusHint("Validate changed files and any newly introduced behavior.");

        if (output.changedFiles() != null) {
            for (String changedFile : output.changedFiles()) {
                artifact.addChangedFile(changedFile);
            }
        }

        return artifact;
    }

    private String buildCodeArtifactId(AgentTask task) {
        return "code-" + task.getTaskId() + "-" + task.getRound();
    }

    private String resolveConversationId(AgentTask task) {
        return task.getConversationId();
    }

    private PlanArtifact requirePlanArtifact(AgentTask task) {
        PlanArtifact artifact = task.findInputArtifact(PlanArtifact.class);
        if (artifact == null) {
            throw new IllegalStateException("CoderAgent requires PlanArtifact input");
        }
        return artifact;
    }
}
