package com.zxy.aiplanner.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxy.aiplanner.entity.TStudyPlanStage;
import com.zxy.aiplanner.entity.TStudyPlanTotal;
import com.zxy.aiplanner.exception.BusinessException;
import com.zxy.aiplanner.mapper.TStudyPlanStageMapper;
import com.zxy.aiplanner.service.TStudyPlanStageService;
import com.zxy.aiplanner.service.TStudyPlanTotalService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 针对表【t_study_plan_stage】的数据库操作 Service 实现
 */
@Service
public class TStudyPlanStageServiceImpl
        extends ServiceImpl<TStudyPlanStageMapper, TStudyPlanStage>
        implements TStudyPlanStageService {

    private final TStudyPlanTotalService planTotalService;

    public TStudyPlanStageServiceImpl(@Lazy TStudyPlanTotalService planTotalService) {
        this.planTotalService = planTotalService;
    }

    @Override
    public List<TStudyPlanStage> getStagesByPlanId(Long totalPlanId) {
        return lambdaQuery()
                .eq(TStudyPlanStage::getTotalPlanId, totalPlanId)
                .eq(TStudyPlanStage::getStageStatus, 1)
                .orderByAsc(TStudyPlanStage::getStageType)
                .list();
    }

    @Override
    public TStudyPlanStage updateByUser(Long stageId,
                                         Long userId,
                                         String stageName,
                                         String startDate,
                                         String endDate,
                                         String stageCoreTask) {
        TStudyPlanStage stage = getById(stageId);
        if (stage == null) throw new BusinessException(404, "阶段不存在");

        // 校验归属
        TStudyPlanTotal plan = planTotalService.getById(stage.getTotalPlanId());
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权修改此阶段");
        }

        if (stageName != null && !stageName.isBlank()) {
            stage.setStageName(stageName);
        }
        if (startDate != null) {
            stage.setStageStartDate(parseDate(startDate));
        }
        if (endDate != null) {
            stage.setStageEndDate(parseDate(endDate));
        }
        if (stageCoreTask != null && !stageCoreTask.isBlank()) {
            stage.setStageCoreTask(stageCoreTask);
        }
        // 重新计算计划天数
        if (stage.getStageStartDate() != null && stage.getStageEndDate() != null) {
            stage.setStagePlannedDays(
                    (int) ChronoUnit.DAYS.between(stage.getStageStartDate(), stage.getStageEndDate()) + 1);
        }
        stage.setModifySource(1);
        updateById(stage);
        return stage;
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr.trim());
        } catch (DateTimeParseException e) {
            throw new BusinessException(400, "日期格式错误，请使用 yyyy-MM-dd 格式");
        }
    }
}
