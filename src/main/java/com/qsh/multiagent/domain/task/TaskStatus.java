package com.qsh.multiagent.domain.task;

public enum TaskStatus {

    CREATED, // 任务刚创建
    PLANNING, // Planner正在拆解任务
    CODING, // Coder正在生成或修改代码
    REVIEWING, // Reviewer正在静态审查
    TESTING, // 测试Agent在跑
    AGGREGATING, // 汇总结果中
    COMPLETED, // 任务完成
    FAILED, // 流程异常失败
    MAX_ROUND_REACHED // 尝试了最大轮次，任务失败

}
