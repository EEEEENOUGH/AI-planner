package com.zxy.aiplanner.controller;

import com.zxy.aiplanner.annotation.OperateLog;
import com.zxy.aiplanner.common.Result;
import com.zxy.aiplanner.constant.OperationTypeConstants;
import com.zxy.aiplanner.entity.TStudyPlanStage;
import com.zxy.aiplanner.entity.TStudyPlanTotal;
import com.zxy.aiplanner.exception.BusinessException;
import com.zxy.aiplanner.service.StudyPlanGenerateService;
import com.zxy.aiplanner.service.TStudyPlanStageService;
import com.zxy.aiplanner.service.TStudyPlanTotalService;
import com.zxy.aiplanner.utils.UserContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 复习计划接口：AI 生成阶段规划 + 查询 + 阶段手动编辑
 * Controller 只负责：鉴权、参数传递、调用 Service、返回结果
 */
@RestController
@RequestMapping("/api/v1/plan")
public class StudyPlanController {

    private final StudyPlanGenerateService planGenerateService;
    private final TStudyPlanTotalService planTotalService;
    private final TStudyPlanStageService planStageService;

    public StudyPlanController(StudyPlanGenerateService planGenerateService,
                               TStudyPlanTotalService planTotalService,
                               TStudyPlanStageService planStageService) {
        this.planGenerateService = planGenerateService;
        this.planTotalService = planTotalService;
        this.planStageService = planStageService;
    }

    // ==================== VO / DTO ====================

    public record StageVO(
            Long stageId,
            Integer stageType,
            String stageName,
            String stageStartDate,
            String stageEndDate,
            Integer stagePlannedDays,
            String stageCoreTask,
            Integer createSource,
            Integer modifySource,
            Integer stageStatus
    ) {}

    public record PlanWithStagesVO(
            Long planId,
            String planName,
            String overallStartDate,
            String overallEndDate,
            Integer dailyTargetMinutes,
            String overallGoal,
            Integer createSource,
            Integer modifySource,
            List<StageVO> stages
    ) {}

    public record StageUpdateDTO(
            String stageName,
            String stageStartDate,
            String stageEndDate,
            String stageCoreTask
    ) {}

    // ==================== 接口 ====================

    /**
     * AI 生成阶段规划
     * POST /api/v1/plan/generate
     */
    @OperateLog(module = "复习计划", type = OperationTypeConstants.PLAN_GENERATE)
    @PostMapping("/generate")
    public Result<PlanWithStagesVO> generate() {
        Long userId = requireUserId();
        return Result.success(planGenerateService.generateAndSave(userId));
    }

    /**
     * 查询当前用户最新启用的总计划及阶段
     * GET /api/v1/plan/current
     */
    @OperateLog(module = "复习计划", type = OperationTypeConstants.PLAN_QUERY)
    @GetMapping("/current")
    public Result<PlanWithStagesVO> current() {
        Long userId = requireUserId();

        TStudyPlanTotal plan = planTotalService.getActivePlan(userId);
        if (plan == null) return Result.success(null);

        List<TStudyPlanStage> stages = planStageService.getStagesByPlanId(plan.getId());
        return Result.success(toPlanVO(plan, stages));
    }

    /**
     * 用户手动修改某个阶段信息
     * PUT /api/v1/plan/stage/{stageId}
     */
    @OperateLog(module = "复习计划", type = OperationTypeConstants.PLAN_STAGE_UPDATE)
    @PutMapping("/stage/{stageId}")
    public Result<StageVO> updateStage(@PathVariable Long stageId,
                                       @RequestBody StageUpdateDTO dto) {
        Long userId = requireUserId();
        TStudyPlanStage stage = planStageService.updateByUser(
                stageId, userId,
                dto.stageName(), dto.stageStartDate(), dto.stageEndDate(), dto.stageCoreTask());
        return Result.success(toStageVO(stage));
    }

    // ==================== 私有工具方法 ====================

    private Long requireUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) throw new BusinessException(401, "未登录或token已过期");
        return userId;
    }

    private PlanWithStagesVO toPlanVO(TStudyPlanTotal plan, List<TStudyPlanStage> stages) {
        return new PlanWithStagesVO(
                plan.getId(),
                plan.getPlanName(),
                plan.getOverallStartDate() != null ? plan.getOverallStartDate().toString() : null,
                plan.getOverallEndDate() != null ? plan.getOverallEndDate().toString() : null,
                plan.getDailyTargetMinutes(),
                plan.getOverallGoal(),
                plan.getCreateSource(),
                plan.getModifySource(),
                stages.stream().map(this::toStageVO).toList()
        );
    }

    private StageVO toStageVO(TStudyPlanStage stage) {
        return new StageVO(
                stage.getId(),
                stage.getStageType(),
                stage.getStageName(),
                stage.getStageStartDate() != null ? stage.getStageStartDate().toString() : null,
                stage.getStageEndDate() != null ? stage.getStageEndDate().toString() : null,
                stage.getStagePlannedDays(),
                stage.getStageCoreTask(),
                stage.getCreateSource(),
                stage.getModifySource(),
                stage.getStageStatus()
        );
    }
}
