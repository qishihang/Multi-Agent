package com.qsh.multiagent.agent.coder;

import com.qsh.multiagent.agent.common.Agent;
import com.qsh.multiagent.agent.common.AgentResult;
import com.qsh.multiagent.agent.common.AgentTask;
import com.qsh.multiagent.agent.common.AgentType;
import com.qsh.multiagent.domain.report.model.CoderReport;
import com.qsh.multiagent.infrastructure.llm.prompt.CoderPromptBuilder;
import com.qsh.multiagent.infrastructure.llm.service.CoderAiService;
import com.qsh.multiagent.infrastructure.llm.service.CoderGenerationOutput;
import com.qsh.multiagent.infrastructure.skill.registry.SkillLoader;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

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
    public AgentResult<CoderReport> execute(AgentTask task) {
        String skillContent = skillLoader.loadSkill(CODER_SKILL_PATH);

        String prompt = coderPromptBuilder.buildUserPrompt(
                task.getTask(),
                task.getPlan(),
                skillContent
        );

        CoderGenerationOutput output = coderAiService.generate(
                task.getTask().getConversationId(),
                prompt
        );

        CoderReport report = new CoderReport(
                Boolean.TRUE.equals(output.passed()),
                output.changeSummary(),
                output.changedFiles(),
                output.codeDraft(),
                output.risks()
        );

        return new AgentResult<>(
                task.getTaskId(),
                task.getPlanId(),
                task.getRound(),
                getType(),
                report.isPassed(),
                report.getChangeSummary(),
                report,
                null
        );
    }
}
