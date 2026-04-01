package com.zxy.aiplanner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zxy.aiplanner.entity.TStudyPlanDayTask;

import java.time.LocalDate;
import java.util.List;

/**
 * 针对表【t_study_plan_day_task(每日任务表)】的数据库操作 Service
 */
public interface TStudyPlanDayTaskService extends IService<TStudyPlanDayTask> {

    /**
     * 查询某阶段下所有启用任务，按日期升序排列
     *
     * @param stageId 阶段ID
     * @return 任务列表
     */
    List<TStudyPlanDayTask> getTasksByStageId(Long stageId);

    /**
     * 查询今日任务（属于当前用户的活跃计划）
     *
     * @param userId 用户ID
     * @return 今日任务列表
     */
    List<TStudyPlanDayTask> getTodayTasks(Long userId);

    /**
     * 新增一条每日任务（用户手动创建，create_source=1）
     *
     * @param stageId         阶段ID
     * @param userId          当前用户ID（用于归属校验）
     * @param taskDate        任务日期
     * @param taskCoreGoal    核心目标
     * @param taskDetail      任务详情
     * @param durationMinutes 计划时长（分钟）
     * @return 保存后的任务实体
     */
    TStudyPlanDayTask createTask(Long stageId, Long userId,
                                 LocalDate taskDate,
                                 String taskCoreGoal,
                                 String taskDetail,
                                 Integer durationMinutes);

    /**
     * 更新任务内容（用户手动，modify_source=1）
     *
     * @param taskId          任务ID
     * @param userId          当前用户ID（归属校验）
     * @param stageId         阶段ID
     * @param taskCoreGoal    新核心目标（null 则不更新）
     * @param taskDetail      新任务详情（null 则不更新）
     * @param durationMinutes 新计划时长（null 则不更新）
     * @return 更新后的任务实体
     */
    TStudyPlanDayTask updateTask(Long taskId, Long userId,
                                  Long stageId,
                                  String taskCoreGoal,
                                  String taskDetail,
                                  Integer durationMinutes);

    /**
     * 更新任务状态（完成/跳过/重置未完成）
     *
     * @param taskId             任务ID
     * @param userId             当前用户ID（归属校验）
     * @param taskStatus         新状态：0未完成，1已完成，2已跳过
     * @param actualDurationMinutes 实际用时（分钟，完成时填写）
     * @return 更新后的任务实体
     */
    TStudyPlanDayTask updateTaskStatus(Long taskId, Long userId,
                                        Integer taskStatus,
                                        Integer actualDurationMinutes);

    /**
     * 逻辑删除任务
     *
     * @param taskId 任务ID
     * @param userId 当前用户ID（归属校验）
     */
    void deleteTask(Long taskId, Long userId);

    /**
     * 查询指定日期的任务（属于当前用户的活跃计划）
     *
     * @param userId 用户ID
     * @param date   任务日期
     * @return 该日期任务列表
     */
    List<TStudyPlanDayTask> getTasksByDate(Long userId, LocalDate date);
}
