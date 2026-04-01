package com.zxy.aiplanner.controller;

import com.zxy.aiplanner.annotation.OperateLog;
import com.zxy.aiplanner.common.Result;
import com.zxy.aiplanner.constant.OperationTypeConstants;
import com.zxy.aiplanner.entity.TStudyCheckin;
import com.zxy.aiplanner.entity.TStudyPlanDayTask;
import com.zxy.aiplanner.entity.TStudyPlanTotal;
import com.zxy.aiplanner.exception.BusinessException;
import com.zxy.aiplanner.service.TStudyCheckinService;
import com.zxy.aiplanner.service.TStudyPlanDayTaskService;
import com.zxy.aiplanner.service.TStudyPlanTotalService;
import com.zxy.aiplanner.utils.UserContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 每日任务接口
 * Controller 只负责：鉴权、参数传递、调用 Service、返回结果
 */
@Validated
@RestController
@RequestMapping("/api/v1/task")
public class DayTaskController {

    private final TStudyPlanDayTaskService dayTaskService;
    private final TStudyCheckinService checkinService;
    private final TStudyPlanTotalService planTotalService;

    public DayTaskController(TStudyPlanDayTaskService dayTaskService,
                             TStudyCheckinService checkinService,
                             TStudyPlanTotalService planTotalService) {
        this.dayTaskService = dayTaskService;
        this.checkinService = checkinService;
        this.planTotalService = planTotalService;
    }

    // ==================== VO / DTO ====================

    public record TaskVO(
            Long taskId,
            Long stageId,
            String taskDate,
            Integer taskSequence,
            Integer durationMinutes,
            String taskCoreGoal,
            String taskDetail,
            Integer taskStatus,
            Integer actualDurationMinutes,
            String completionTime,
            Integer createSource,
            Integer modifySource
    ) {}

    public record CheckinVO(
            Long checkinId,
            String checkinDate,
            Integer checkinStatus,
            Integer finishedTaskCount,
            Integer totalTaskCount,
            String taskCompletionRate,
            String checkinNote
    ) {}

    public record CheckinSummaryVO(
            Integer totalStudyDays,
            Integer currentStreakDays,
            Boolean todayCheckedIn
    ) {}

    public record TaskCreateDTO(
            @NotNull(message = "阶段ID不能为空")
            Long stageId,
            @NotNull(message = "任务日期不能为空")
            String taskDate,
            String taskCoreGoal,
            String taskDetail,
            Integer durationMinutes
    ) {}

    public record TaskUpdateDTO(
            Long stageId,
            String taskCoreGoal,
            String taskDetail,
            Integer durationMinutes
    ) {}

    public record TaskStatusDTO(
            @NotNull(message = "状态不能为空")
            Integer taskStatus,
            Integer actualDurationMinutes
    ) {}

    public record CheckinDTO(
            String checkinNote
    ) {}

    // ==================== 接口 ====================

    /**
     * 查询某阶段所有任务
     * GET /api/v1/task/stage/{stageId}
     */
    @OperateLog(module = "每日任务", type = OperationTypeConstants.TASK_QUERY)
    @GetMapping("/stage/{stageId}")
    public Result<List<TaskVO>> getByStage(@PathVariable Long stageId) {
        requireUserId();
        List<TStudyPlanDayTask> tasks = dayTaskService.getTasksByStageId(stageId);
        return Result.success(tasks.stream().map(this::toVO).toList());
    }

    /**
     * 查询今日任务
     * GET /api/v1/task/today
     */
    @OperateLog(module = "每日任务", type = OperationTypeConstants.TASK_QUERY)
    @GetMapping("/today")
    public Result<List<TaskVO>> today() {
        Long userId = requireUserId();
        List<TStudyPlanDayTask> tasks = dayTaskService.getTodayTasks(userId);
        return Result.success(tasks.stream().map(this::toVO).toList());
    }

    /**
     * 查询指定日期的任务
     * GET /api/v1/task/date/{date}
     */
    @OperateLog(module = "每日任务", type = OperationTypeConstants.TASK_QUERY)
    @GetMapping("/date/{date}")
    public Result<List<TaskVO>> byDate(@PathVariable String date) {
        Long userId = requireUserId();
        LocalDate taskDate;
        try {
            taskDate = LocalDate.parse(date);
        } catch (Exception e) {
            throw new BusinessException(400, "日期格式错误，请使用 yyyy-MM-dd");
        }
        List<TStudyPlanDayTask> tasks = dayTaskService.getTasksByDate(userId, taskDate);
        return Result.success(tasks.stream().map(this::toVO).toList());
    }

    /**
     * 新增每日任务
     * POST /api/v1/task
     */
    @OperateLog(module = "每日任务", type = OperationTypeConstants.TASK_CREATE)
    @PostMapping
    public Result<TaskVO> create(@RequestBody @Valid TaskCreateDTO dto) {
        Long userId = requireUserId();
        LocalDate taskDate;
        try {
            taskDate = LocalDate.parse(dto.taskDate());
        } catch (Exception e) {
            throw new BusinessException(400, "日期格式错误，请使用 yyyy-MM-dd");
        }
        TStudyPlanDayTask task = dayTaskService.createTask(
                dto.stageId(), userId, taskDate,
                dto.taskCoreGoal(), dto.taskDetail(), dto.durationMinutes());
        return Result.success(toVO(task));
    }

    /**
     * 更新任务内容
     * PUT /api/v1/task/{taskId}
     */
    @OperateLog(module = "每日任务", type = OperationTypeConstants.TASK_UPDATE)
    @PutMapping("/{taskId}")
    public Result<TaskVO> update(@PathVariable Long taskId,
                                   @RequestBody TaskUpdateDTO dto) {
        Long userId = requireUserId();
        TStudyPlanDayTask task = dayTaskService.updateTask(
                taskId, userId, dto.stageId(), dto.taskCoreGoal(), dto.taskDetail(), dto.durationMinutes());
        return Result.success(toVO(task));
    }

    /**
     * 更新任务状态（完成 / 跳过 / 重置）
     * PATCH /api/v1/task/{taskId}/status
     */
    @OperateLog(module = "每日任务", type = OperationTypeConstants.TASK_STATUS_UPDATE)
    @PatchMapping("/{taskId}/status")
    public Result<TaskVO> updateStatus(@PathVariable Long taskId,
                                        @RequestBody @Valid TaskStatusDTO dto) {
        Long userId = requireUserId();
        if (dto.taskStatus() < 0 || dto.taskStatus() > 2) {
            throw new BusinessException(400, "状态值非法：0=未完成，1=已完成，2=已跳过");
        }
        TStudyPlanDayTask task = dayTaskService.updateTaskStatus(
                taskId, userId, dto.taskStatus(), dto.actualDurationMinutes());
        return Result.success(toVO(task));
    }

    /**
     * 删除任务
     * DELETE /api/v1/task/{taskId}
     */
    @OperateLog(module = "每日任务", type = OperationTypeConstants.TASK_DELETE)
    @DeleteMapping("/{taskId}")
    public Result<Void> delete(@PathVariable Long taskId) {
        Long userId = requireUserId();
        dayTaskService.deleteTask(taskId, userId);
        return Result.success();
    }

    /**
     * 今日打卡
     * POST /api/v1/task/checkin
     */
    @OperateLog(module = "打卡", type = OperationTypeConstants.CHECKIN)
    @PostMapping("/checkin")
    public Result<CheckinVO> checkin(@RequestBody CheckinDTO dto) {
        Long userId = requireUserId();
        TStudyPlanTotal plan = planTotalService.getActivePlan(userId);
        if (plan == null) throw new BusinessException(400, "暂无启用中的复习计划");
        TStudyCheckin checkin = checkinService.upsertCheckin(
                userId, plan.getId(), LocalDate.now(),
                dto == null ? null : dto.checkinNote());
        return Result.success(toCheckinVO(checkin));
    }

    /**
     * 首页打卡汇总
     * GET /api/v1/task/checkin/summary
     */
    @OperateLog(module = "打卡", type = OperationTypeConstants.TASK_QUERY)
    @GetMapping("/checkin/summary")
    public Result<CheckinSummaryVO> checkinSummary() {
        Long userId = requireUserId();
        TStudyPlanTotal plan = planTotalService.getActivePlan(userId);
        if (plan == null) {
            return Result.success(new CheckinSummaryVO(0, 0, false));
        }
        TStudyCheckinService.CheckinSummary summary = checkinService.getCheckinSummary(userId, plan.getId());
        return Result.success(new CheckinSummaryVO(
                summary.totalStudyDays(),
                summary.currentStreakDays(),
                summary.todayCheckedIn()
        ));
    }

    // ==================== 私有工具方法 ====================

    private Long requireUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) throw new BusinessException(401, "未登录或token已过期");
        return userId;
    }

    private TaskVO toVO(TStudyPlanDayTask t) {
        return new TaskVO(
                t.getId(),
                t.getStageId(),
                t.getTaskDate() != null ? t.getTaskDate().toString() : null,
                t.getTaskSequence(),
                t.getDurationMinutes(),
                t.getTaskCoreGoal(),
                t.getTaskDetail(),
                t.getTaskStatus(),
                t.getActualDurationMinutes(),
                t.getCompletionTime() != null ? t.getCompletionTime().toString() : null,
                t.getCreateSource(),
                t.getModifySource()
        );
    }

    private CheckinVO toCheckinVO(TStudyCheckin c) {
        return new CheckinVO(
                c.getId(),
                c.getCheckinDate() != null ? c.getCheckinDate().toString() : null,
                c.getCheckinStatus(),
                c.getFinishedTaskCount(),
                c.getTotalTaskCount(),
                c.getTaskCompletionRate() != null ? c.getTaskCompletionRate().toPlainString() : "0.00",
                c.getCheckinNote()
        );
    }
}
