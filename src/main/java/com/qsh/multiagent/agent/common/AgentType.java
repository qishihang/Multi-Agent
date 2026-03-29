package com.qsh.multiagent.agent.common;

public enum AgentType {

    PLANNER, // 规划
    CODER, // 编码
    REVIEWER, // 审核 业务逻辑
    BUILD_TESTER, // 构建测试
    UNIT_TESTER, // 单元测试
    LINT_TESTER, // 静态检查 规则
    AGGREGATOR // 聚合
}
