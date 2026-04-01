package com.zxy.aiplanner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zxy.aiplanner.entity.TStudyCheckin;

import java.time.LocalDate;

/**
 * 针对表【t_study_checkin(每日打卡记录表)】的数据库操作 Service
 */
public interface TStudyCheckinService extends IService<TStudyCheckin> {

    record CheckinSummary(
            int totalStudyDays,
            int currentStreakDays,
            boolean todayCheckedIn
    ) {}

    /**
     * 打卡（不存在则新建，已存在则更新统计数据）
     * 自动统计当日已完成/总任务数、完成率
     *
     * @param userId      用户ID
     * @param totalPlanId 总计划ID
     * @param date        打卡日期
     * @param checkinNote 打卡备注（可为空）
     * @return 打卡记录实体
     */
    TStudyCheckin upsertCheckin(Long userId, Long totalPlanId, LocalDate date, String checkinNote);

    /**
     * 获取打卡汇总：累计学习天数、连续学习天数、今日是否已打卡
     */
    CheckinSummary getCheckinSummary(Long userId, Long totalPlanId);
}
