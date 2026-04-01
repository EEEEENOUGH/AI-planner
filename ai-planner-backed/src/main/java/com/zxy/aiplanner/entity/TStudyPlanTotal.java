package com.zxy.aiplanner.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 复习总计划表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_study_plan_total")
public class TStudyPlanTotal extends BaseEntity {

    private Long userId;
    private Long examArchiveId;
    private String planName;
    private LocalDate overallStartDate;
    private LocalDate overallEndDate;
    private Integer dailyTargetMinutes;
    private String overallGoal;
    private Integer createSource;
    private Integer modifySource;
    private Integer planStatus;
}
