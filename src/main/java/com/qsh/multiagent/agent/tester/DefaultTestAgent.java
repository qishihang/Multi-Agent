package com.qsh.multiagent.agent.tester;

import com.qsh.multiagent.agent.common.Agent;
import com.qsh.multiagent.agent.common.AgentResult;
import com.qsh.multiagent.agent.common.AgentTask;
import com.qsh.multiagent.agent.common.AgentType;
import com.qsh.multiagent.domain.report.model.CoderReport;
import com.qsh.multiagent.domain.report.model.TestReport;
import com.qsh.multiagent.infrastructure.llm.prompt.TestPromptBuilder;
import com.qsh.multiagent.infrastructure.llm.service.TestAiService;
import com.qsh.multiagent.infrastructure.llm.service.TestGenerationOutput;
import com.qsh.multiagent.infrastructure.skill.registry.SkillLoader;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

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
    public AgentResult<TestReport> execute(AgentTask task) {
        CoderReport coderReport = task.getCoderReport();
        String skillContent = skillLoader.loadSkill(TEST_SKILL_PATH);
        String prompt = testPromptBuilder.buildUserPrompt(
                task.getTask(),
                task.getPlan(),
                coderReport,
                skillContent
        );

        TestGenerationOutput output = testAiService.test(prompt);
        TestReport report = new TestReport(
                Boolean.TRUE.equals(output.passed()),
                output.projectType(),
                Boolean.TRUE.equals(output.compileRequired()),
                Boolean.TRUE.equals(output.compilePassed()),
                Boolean.TRUE.equals(output.testsGenerated()),
                output.generatedTestFileCount(),
                Boolean.TRUE.equals(output.testsExecuted()),
                output.testsPassedCount(),
                output.testsFailedCount(),
                output.producedFiles(),
                output.summary(),
                output.failureAnalysis()
        );

        return new AgentResult<>(
                task.getTaskId(),
                task.getPlanId(),
                task.getRound(),
                getType(),
                report.isPassed(),
                report.getSummary(),
                report,
                report.isPassed() ? null : report.getFailureAnalysis()
        );
    }
}
