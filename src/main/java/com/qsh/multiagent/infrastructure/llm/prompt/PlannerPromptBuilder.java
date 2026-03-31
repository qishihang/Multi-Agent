package com.qsh.multiagent.infrastructure.llm.prompt;

import com.qsh.multiagent.domain.task.Task;
import org.springframework.stereotype.Component;

@Component
public class PlannerPromptBuilder {

    public String buildUserPrompt(Task task, String skillContent){
        return  """
                请你根据以下技能说明和任务上下文，为当前轮次生成结构化计划。

                ====================
                【Skill】
                ====================
                %s

                ====================
                【Task Context】
                ====================
                taskId: %s
                currentRound: %s
                taskGoal: %s

                ====================
                【Your Task】
                ====================
                请基于上述 skill 要求和当前任务上下文，
                输出当前轮次的结构化计划结果。
                """.formatted(
                skillContent,
                task.getId(),
                task.getCurrentRound(),
                task.getGoal()
        );
    }
}
