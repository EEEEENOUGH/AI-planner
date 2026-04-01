package com.zxy.aiplanner.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxy.aiplanner.entity.TStudyCheckin;
import com.zxy.aiplanner.entity.TStudyPlanDayTask;
import com.zxy.aiplanner.mapper.TStudyCheckinMapper;
import com.zxy.aiplanner.service.TStudyCheckinService;
import com.zxy.aiplanner.service.TStudyPlanDayTaskService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 针对表【t_study_checkin】的数据库操作 Service 实现
 */
@Service
public class TStudyCheckinServiceImpl
        extends ServiceImpl<TStudyCheckinMapper, TStudyCheckin>
        implements TStudyCheckinService {

    private final TStudyPlanDayTaskService dayTaskService;

    public TStudyCheckinServiceImpl(@Lazy TStudyPlanDayTaskService dayTaskService) {
        this.dayTaskService = dayTaskService;
    }

    @Override
    public TStudyCheckin upsertCheckin(Long userId, Long totalPlanId, LocalDate date, String checkinNote) {
        List<TStudyPlanDayTask> todayTasks = dayTaskService.getTodayTasks(userId);
        int totalCount = todayTasks.size();
        int finishedCount = (int) todayTasks.stream().filter(task -> task.getTaskStatus() == 1).count();
        int actualMinutes = todayTasks.stream()
                .mapToInt(task -> task.getActualDurationMinutes() == null ? 0 : task.getActualDurationMinutes())
                .sum();
        BigDecimal rate = totalCount == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(finishedCount * 100.0 / totalCount)
                .setScale(2, RoundingMode.HALF_UP);

        TStudyCheckin existing = lambdaQuery()
                .eq(TStudyCheckin::getUserId, userId)
                .eq(TStudyCheckin::getTotalPlanId, totalPlanId)
                .eq(TStudyCheckin::getCheckinDate, date)
                .one();

        if (existing != null) {
            existing.setCheckinStatus(1);
            existing.setFinishedTaskCount(finishedCount);
            existing.setTotalTaskCount(totalCount);
            existing.setActualDurationMinutes(actualMinutes);
            existing.setTaskCompletionRate(rate);
            if (checkinNote != null && !checkinNote.isBlank()) {
                existing.setCheckinNote(checkinNote);
            }
            updateById(existing);
            return existing;
        }

        TStudyCheckin checkin = new TStudyCheckin();
        checkin.setUserId(userId);
        checkin.setTotalPlanId(totalPlanId);
        checkin.setCheckinDate(date);
        checkin.setCheckinStatus(1);
        checkin.setFinishedTaskCount(finishedCount);
        checkin.setTotalTaskCount(totalCount);
        checkin.setActualDurationMinutes(actualMinutes);
        checkin.setTaskCompletionRate(rate);
        checkin.setRemindSentStatus(0);
        if (checkinNote != null && !checkinNote.isBlank()) {
            checkin.setCheckinNote(checkinNote);
        }
        save(checkin);
        return checkin;
    }

    @Override
    public CheckinSummary getCheckinSummary(Long userId, Long totalPlanId) {
        List<TStudyCheckin> records = lambdaQuery()
                .eq(TStudyCheckin::getUserId, userId)
                .eq(TStudyCheckin::getTotalPlanId, totalPlanId)
                .eq(TStudyCheckin::getCheckinStatus, 1)
                .orderByDesc(TStudyCheckin::getCheckinDate)
                .list();

        if (records.isEmpty()) {
            return new CheckinSummary(0, 0, false);
        }

        Set<LocalDate> checkinDates = new HashSet<>();
        for (TStudyCheckin record : records) {
            if (record.getCheckinDate() != null) {
                checkinDates.add(record.getCheckinDate());
            }
        }

        LocalDate today = LocalDate.now();
        boolean todayCheckedIn = checkinDates.contains(today);
        int streakDays = 0;
        LocalDate cursor = todayCheckedIn ? today : today.minusDays(1);
        while (checkinDates.contains(cursor)) {
            streakDays++;
            cursor = cursor.minusDays(1);
        }

        return new CheckinSummary(checkinDates.size(), streakDays, todayCheckedIn);
    }
}
