package com.zxy.aiplanner.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 复习进度跟踪表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_study_progress")
public class TStudyProgress extends BaseEntity {

    private Long userId;
    private Long totalPlanId;
    private LocalDate progressDate;
    private Integer plannedMinutes;
    private Integer completedMinutes;
    private BigDecimal completionRate;
    private Integer remindStatus;
    private Integer remindCount;
    private LocalDateTime nextRemindTime;
}
