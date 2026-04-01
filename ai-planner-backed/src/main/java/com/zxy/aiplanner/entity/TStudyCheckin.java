package com.zxy.aiplanner.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 每日打卡记录表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_study_checkin")
public class TStudyCheckin extends BaseEntity {

    private Long userId;
    private Long totalPlanId;
    private LocalDate checkinDate;
    private Integer checkinStatus;
    private Integer actualDurationMinutes;
    private Integer finishedTaskCount;
    private Integer totalTaskCount;
    private BigDecimal taskCompletionRate;
    private Integer remindSentStatus;
    private String checkinNote;
}
