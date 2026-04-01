package com.zxy.aiplanner.service;

import com.zxy.aiplanner.controller.StudyPlanController.PlanWithStagesVO;

/**
 * AI 生成阶段规划业务 Service
 * 封装：Prompt 构建 → DeepSeek 调用 → JSON 解析 → 事务落库
 */
public interface StudyPlanGenerateService {

    /**
     * 为指定用户 AI 生成阶段规划并落库
     * 1. 读取用户启用中的备考档案
     * 2. 构建结构化 Prompt
     * 3. 调用 DeepSeek chatSingle
     * 4. 解析返回 JSON，停用旧计划，写入 t_study_plan_total + t_study_plan_stage
     *
     * @param userId 当前用户ID
     * @return 包含总计划信息和阶段列表的 VO
     */
    PlanWithStagesVO generateAndSave(Long userId);
}
