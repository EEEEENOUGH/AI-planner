package com.zxy.aiplanner.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日任务表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_study_plan_day_task")
public class TStudyPlanDayTask extends BaseEntity {

    private Long stageId;
    private LocalDate taskDate;
    private Integer taskSequence;
    private Integer durationMinutes;
    private String taskCoreGoal;
    private String taskDetail;
    private Integer taskStatus;
    private Integer actualDurationMinutes;
    private LocalDateTime completionTime;
    private Integer createSource;
    private Integer modifySource;
}
