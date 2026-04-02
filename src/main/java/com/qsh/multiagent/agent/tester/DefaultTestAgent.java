package com.qsh.multiagent.agent.tester;

import com.qsh.multiagent.agent.common.Agent;
import com.qsh.multiagent.agent.common.AgentResult;
import com.qsh.multiagent.agent.common.AgentTask;
import com.qsh.multiagent.agent.common.AgentType;
import com.qsh.multiagent.domain.artifact.CodeArtifact;
import com.qsh.multiagent.domain.artifact.PlanArtifact;
import com.qsh.multiagent.domain.artifact.TestArtifact;
import com.qsh.multiagent.infrastructure.llm.prompt.TestPromptBuilder;
import com.qsh.multiagent.infrastructure.llm.service.TestAiService;
import com.qsh.multiagent.infrastructure.llm.service.TestGenerationOutput;
import com.qsh.multiagent.infrastructure.skill.registry.SkillLoader;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Primary
public class DefaultTestAgent implements Agent {

    private static final String TEST_SKILL_PATH = "skills/test-skill.md";

    private final TestAiService testAiService;
    private final TestPromptBuilder testPromptBuilder;
    private final SkillLoader skillLoader;

    public DefaultTestAgent(TestAiService testAiService,
                            TestPromptBuilder testPromptBuilder,
                            SkillLoader skillLoader) {
        this.testAiService = testAiService;
        this.testPromptBuilder = testPromptBuilder;
        this.skillLoader = skillLoader;
    }

    @Override
    public AgentType getType() {
        return AgentType.TESTER;
    }

    @Override
    public AgentResult execute(AgentTask task) {
        PlanArtifact planArtifact = requirePlanArtifact(task);
        CodeArtifact codeArtifact = requireCodeArtifact(task);
        String skillContent = skillLoader.loadSkill(TEST_SKILL_PATH);
        String prompt = testPromptBuilder.buildUserPrompt(
                task,
                planArtifact,
                codeArtifact,
                skillContent
        );

        TestGenerationOutput output = testAiService.test(buildExecutionMemoryId(task), prompt);
        AgentResult result = new AgentResult(
                task.getTaskId(),
                task.getPlanId(),
                task.getRound(),
                getType(),
                Boolean.TRUE.equals(output.passed()),
                output.summary(),
                Boolean.TRUE.equals(output.passed()) ? null : output.failureAnalysis(),
                null,
                null,
                null
        );
        TestArtifact testArtifact = buildTestArtifact(task, output);
        result.addOutputArtifact(testArtifact);
        result.setRawEvidence(output.summary());

        if (!Boolean.TRUE.equals(output.passed())
                && output.failureAnalysis() != null
                && !output.failureAnalysis().isBlank()) {
            result.addIssue(output.failureAnalysis());
        }
        if (Boolean.TRUE.equals(output.dependencyPreparationAttempted())
                && !Boolean.TRUE.equals(output.dependencyPreparationPassed())) {
            result.addIssue("Dependency preparation failed.");
        }
        if (output.testsFailedCount() != null && output.testsFailedCount() > 0) {
            result.addIssue("Failed tests: " + output.testsFailedCount());
        }

        return result;
    }

    private String buildExecutionMemoryId(AgentTask task) {
        if (task.getMemoryScope() != null && !task.getMemoryScope().isBlank()) {
            return task.getMemoryScope();
        }
        return "%s::tester::task-%s::round-%s".formatted(
                task.getConversationId(),
                task.getTaskId(),
                task.getRound()
        );
    }

    private TestArtifact buildTestArtifact(AgentTask task,
                                           TestGenerationOutput output) {
        TestArtifact artifact = new TestArtifact(
                buildTestArtifactId(task),
                resolveConversationId(task),
                task.getTaskId(),
                task.getRunId(),
                task.getRound(),
                AgentType.TESTER,
                Instant.now()
        );
        artifact.setPassed(Boolean.TRUE.equals(output.passed()));
        artifact.setProjectType(output.projectType());
        artifact.setDependencyPreparationAttempted(Boolean.TRUE.equals(output.dependencyPreparationAttempted()));
        artifact.setDependencyPreparationPassed(Boolean.TRUE.equals(output.dependencyPreparationPassed()));
        artifact.setCompileRequired(Boolean.TRUE.equals(output.compileRequired()));
        artifact.setCompilePassed(Boolean.TRUE.equals(output.compilePassed()));
        artifact.setTestsGenerated(Boolean.TRUE.equals(output.testsGenerated()));
        artifact.setGeneratedTestFileCount(output.generatedTestFileCount());
        artifact.setTestsExecuted(Boolean.TRUE.equals(output.testsExecuted()));
        artifact.setTestsPassedCount(output.testsPassedCount());
        artifact.setTestsFailedCount(output.testsFailedCount());
        artifact.setSummary(output.summary());
        artifact.setFailureAnalysis(output.failureAnalysis());
        artifact.setEvidenceSummary(buildEvidenceSummary(output));

        if (output.producedFiles() != null) {
            for (String producedFile : output.producedFiles()) {
                artifact.addProducedFile(producedFile);
            }
        }
        artifact.addExecutedCommand("generated-by-tester-agent");

        return artifact;
    }

    private String buildTestArtifactId(AgentTask task) {
        return "test-" + task.getTaskId() + "-" + task.getRound();
    }

    private String resolveConversationId(AgentTask task) {
        return task.getConversationId();
    }

    private String buildEvidenceSummary(TestGenerationOutput output) {
        Integer failedCount = output.testsFailedCount() == null ? 0 : output.testsFailedCount();
        Integer passedCount = output.testsPassedCount() == null ? 0 : output.testsPassedCount();
        String dependencySummary = Boolean.TRUE.equals(output.dependencyPreparationAttempted())
                ? (Boolean.TRUE.equals(output.dependencyPreparationPassed())
                ? "Dependency preparation succeeded."
                : "Dependency preparation failed.")
                : "Dependency preparation was not required.";
        return "%s Test evidence collected with %s passed and %s failed checks."
                .formatted(dependencySummary, passedCount, failedCount);
    }

    private PlanArtifact requirePlanArtifact(AgentTask task) {
        PlanArtifact artifact = task.findInputArtifact(PlanArtifact.class);
        if (artifact == null) {
            throw new IllegalStateException("TestAgent requires PlanArtifact input");
        }
        return artifact;
    }

    private CodeArtifact requireCodeArtifact(AgentTask task) {
        CodeArtifact artifact = task.findInputArtifact(CodeArtifact.class);
        if (artifact == null) {
            throw new IllegalStateException("TestAgent requires CodeArtifact input");
        }
        return artifact;
    }
}
