package com.qsh.multiagent.infrastructure.llm;

import com.qsh.multiagent.infrastructure.llm.service.PlannerAiService;
import com.qsh.multiagent.infrastructure.llm.service.PlannerPlanOutput;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PlannerAiServiceSmokeTest {

    @Autowired
    private PlannerAiService plannerAiService;

    @Test
    void should_call_llm_and_return_structured_plan_output() {
        PlannerPlanOutput output = plannerAiService.createPlan(
                "conv-smoke-test",
                """
                请为“实现一个基础登录接口”生成当前轮结构化计划。
                要求输出 objective、doneCriteria 和至少一个 step。
                """
        );

        Assertions.assertNotNull(output);
        Assertions.assertNotNull(output.objective());
        Assertions.assertFalse(output.objective().isBlank());
        Assertions.assertNotNull(output.doneCriteria());
        Assertions.assertFalse(output.doneCriteria().isBlank());
        Assertions.assertNotNull(output.steps());
        Assertions.assertFalse(output.steps().isEmpty());
    }
}
