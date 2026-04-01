package com.zxy.aiplanner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zxy.aiplanner.entity.TStudyPlanTotal;

/**
 * 针对表【t_study_plan_total】的数据库操作 Service
 */
public interface TStudyPlanTotalService extends IService<TStudyPlanTotal> {

    /**
     * 获取用户当前启用的总计划（最新一条）
     *
     * @param userId 用户ID
     * @return 总计划实体，不存在则返回 null
     */
    TStudyPlanTotal getActivePlan(Long userId);

    /**
     * 停用该用户所有启用中的总计划
     *
     * @param userId 用户ID
     */
    void disableAllPlans(Long userId);
}
