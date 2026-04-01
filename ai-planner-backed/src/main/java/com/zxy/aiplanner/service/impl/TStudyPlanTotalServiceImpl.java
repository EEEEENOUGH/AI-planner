package com.zxy.aiplanner.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxy.aiplanner.entity.TStudyPlanTotal;
import com.zxy.aiplanner.mapper.TStudyPlanTotalMapper;
import com.zxy.aiplanner.service.TStudyPlanTotalService;
import org.springframework.stereotype.Service;

/**
 * 针对表【t_study_plan_total】的数据库操作 Service 实现
 */
@Service
public class TStudyPlanTotalServiceImpl
        extends ServiceImpl<TStudyPlanTotalMapper, TStudyPlanTotal>
        implements TStudyPlanTotalService {

    @Override
    public TStudyPlanTotal getActivePlan(Long userId) {
        return lambdaQuery()
                .eq(TStudyPlanTotal::getUserId, userId)
                .eq(TStudyPlanTotal::getPlanStatus, 1)
                .orderByDesc(TStudyPlanTotal::getCreateTime)
                .last("LIMIT 1")
                .one();
    }

    @Override
    public void disableAllPlans(Long userId) {
        lambdaUpdate()
                .eq(TStudyPlanTotal::getUserId, userId)
                .eq(TStudyPlanTotal::getPlanStatus, 1)
                .set(TStudyPlanTotal::getPlanStatus, 0)
                .update();
    }
}
