package com.zxy.aiplanner.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxy.aiplanner.entity.TStudyPlanDayTask;
import com.zxy.aiplanner.entity.TStudyPlanStage;
import com.zxy.aiplanner.entity.TStudyPlanTotal;
import com.zxy.aiplanner.exception.BusinessException;
import com.zxy.aiplanner.mapper.TStudyPlanDayTaskMapper;
import com.zxy.aiplanner.service.TStudyPlanDayTaskService;
import com.zxy.aiplanner.service.TStudyPlanStageService;
import com.zxy.aiplanner.service.TStudyPlanTotalService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 针对表【t_study_plan_day_task】的数据库操作 Service 实现
 */
@Service
public class TStudyPlanDayTaskServiceImpl
        extends ServiceImpl<TStudyPlanDayTaskMapper, TStudyPlanDayTask>
        implements TStudyPlanDayTaskService {

    private final TStudyPlanStageService planStageService;
    private final TStudyPlanTotalService planTotalService;

    public TStudyPlanDayTaskServiceImpl(
            @Lazy TStudyPlanStageService planStageService,
            @Lazy TStudyPlanTotalService planTotalService) {
        this.planStageService = planStageService;
        this.planTotalService = planTotalService;
    }

    @Override
    public List<TStudyPlanDayTask> getTasksByStageId(Long stageId) {
        return lambdaQuery()
                .eq(TStudyPlanDayTask::getStageId, stageId)
                .orderByAsc(TStudyPlanDayTask::getTaskDate)
                .orderByAsc(TStudyPlanDayTask::getTaskSequence)
                .list();
    }

    @Override
    public List<TStudyPlanDayTask> getTodayTasks(Long userId) {
        // 获取用户当前启用计划
        TStudyPlanTotal plan = planTotalService.getActivePlan(userId);
        if (plan == null) return List.of();

        // 获取该计划所有启用阶段的 ID 列表
        List<Long> stageIds = planStageService.getStagesByPlanId(plan.getId())
                .stream().map(s -> s.getId()).toList();
        if (stageIds.isEmpty()) return List.of();

        LocalDate today = LocalDate.now();
        return lambdaQuery()
                .in(TStudyPlanDayTask::getStageId, stageIds)
                .eq(TStudyPlanDayTask::getTaskDate, today)
                .orderByAsc(TStudyPlanDayTask::getTaskSequence)
                .list();
    }

    @Override
    public TStudyPlanDayTask createTask(Long stageId, Long userId,
                                        LocalDate taskDate,
                                        String taskCoreGoal,
                                        String taskDetail,
                                        Integer durationMinutes) {
        // 归属校验
        requireStageOwner(stageId, userId);

        // 计算同日序号
        long existCount = lambdaQuery()
                .eq(TStudyPlanDayTask::getStageId, stageId)
                .eq(TStudyPlanDayTask::getTaskDate, taskDate)
                .count();

        TStudyPlanDayTask task = new TStudyPlanDayTask();
        task.setStageId(stageId);
        task.setTaskDate(taskDate);
        task.setTaskSequence((int) existCount + 1);
        task.setTaskCoreGoal(taskCoreGoal == null ? "" : taskCoreGoal);
        task.setTaskDetail(taskDetail == null ? "" : taskDetail);
        task.setDurationMinutes(durationMinutes == null ? 0 : durationMinutes);
        task.setTaskStatus(0);
        task.setActualDurationMinutes(0);
        task.setCreateSource(1);
        task.setModifySource(1);
        save(task);
        return task;
    }

    @Override
    public TStudyPlanDayTask updateTask(Long taskId, Long userId,
                                        Long stageId,
                                         String taskCoreGoal,
                                         String taskDetail,
                                         Integer durationMinutes) {
        TStudyPlanDayTask task = requireTaskOwner(taskId, userId);
        if (stageId != null) {
            // 校验新阶段归属当前用户
            requireStageOwner(stageId, userId);
            task.setStageId(stageId);
        };
        if (taskCoreGoal != null && !taskCoreGoal.isBlank()) task.setTaskCoreGoal(taskCoreGoal);
        if (taskDetail != null) task.setTaskDetail(taskDetail);
        if (durationMinutes != null) task.setDurationMinutes(durationMinutes);
        task.setModifySource(1);
        updateById(task);
        return task;
    }

    @Override
    public TStudyPlanDayTask updateTaskStatus(Long taskId, Long userId,
                                               Integer taskStatus,
                                               Integer actualDurationMinutes) {
        TStudyPlanDayTask task = requireTaskOwner(taskId, userId);
        task.setTaskStatus(taskStatus);
        if (taskStatus == 1) {
            // 标记完成
            task.setCompletionTime(LocalDateTime.now());
            if (actualDurationMinutes != null) task.setActualDurationMinutes(actualDurationMinutes);
        } else {
            // 重置或跳过
            task.setCompletionTime(null);
            task.setActualDurationMinutes(0);
        }
        updateById(task);
        return task;
    }

    @Override
    public void deleteTask(Long taskId, Long userId) {
        requireTaskOwner(taskId, userId);
        removeById(taskId);
    }

    @Override
    public List<TStudyPlanDayTask> getTasksByDate(Long userId, LocalDate date) {
        TStudyPlanTotal plan = planTotalService.getActivePlan(userId);
        if (plan == null) return List.of();

        List<Long> stageIds = planStageService.getStagesByPlanId(plan.getId())
                .stream().map(s -> s.getId()).toList();
        if (stageIds.isEmpty()) return List.of();

        return lambdaQuery()
                .in(TStudyPlanDayTask::getStageId, stageIds)
                .eq(TStudyPlanDayTask::getTaskDate, date)
                .orderByAsc(TStudyPlanDayTask::getTaskSequence)
                .list();
    }

    // ==================== 私有校验方法 ====================

    private void requireStageOwner(Long stageId, Long userId) {
        TStudyPlanStage stage = planStageService.getById(stageId);
        if (stage == null) throw new BusinessException(404, "阶段不存在");
        TStudyPlanTotal plan = planTotalService.getById(stage.getTotalPlanId());
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此阶段");
        }
    }

    private TStudyPlanDayTask requireTaskOwner(Long taskId, Long userId) {
        TStudyPlanDayTask task = getById(taskId);
        if (task == null) throw new BusinessException(404, "任务不存在");
        requireStageOwner(task.getStageId(), userId);
        return task;
    }
}
