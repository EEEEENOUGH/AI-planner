package com.zxy.aiplanner.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 阶段计划表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_study_plan_stage")
public class TStudyPlanStage extends BaseEntity {

    private Long totalPlanId;
    private Integer stageType;
    private String stageName;
    private LocalDate stageStartDate;
    private LocalDate stageEndDate;
    private Integer stagePlannedDays;
    private String stageCoreTask;
    private Integer createSource;
    private Integer modifySource;
    private Integer stageStatus;
}
