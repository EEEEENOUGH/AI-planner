package com.zxy.aiplanner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zxy.aiplanner.entity.TStudyPlanStage;
import com.zxy.aiplanner.exception.BusinessException;

import java.util.List;

/**
 * 针对表【t_study_plan_stage】的数据库操作 Service
 */
public interface TStudyPlanStageService extends IService<TStudyPlanStage> {

    /**
     * 获取总计划下所有启用的阶段，按 stageType 升序
     *
     * @param totalPlanId 总计划ID
     * @return 阶段列表
     */
    List<TStudyPlanStage> getStagesByPlanId(Long totalPlanId);

    /**
     * 用户手动修改阶段信息
     * 校验阶段归属权限后更新，modify_source 置为 1（人工）
     *
     * @param stageId       阶段ID
     * @param userId        当前用户ID（用于归属校验）
     * @param stageName     新阶段名（null 则不更新）
     * @param startDate     新开始日期字符串 yyyy-MM-dd（null 则不更新）
     * @param endDate       新结束日期字符串 yyyy-MM-dd（null 则不更新）
     * @param stageCoreTask 新核心任务说明（null 则不更新）
     * @return 更新后的阶段实体
     * @throws BusinessException 阶段不存在或无权限
     */
    TStudyPlanStage updateByUser(Long stageId,
                                  Long userId,
                                  String stageName,
                                  String startDate,
                                  String endDate,
                                  String stageCoreTask);
}
