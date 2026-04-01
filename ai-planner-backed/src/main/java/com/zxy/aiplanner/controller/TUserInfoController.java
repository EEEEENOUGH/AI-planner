package com.zxy.aiplanner.controller;

import com.zxy.aiplanner.annotation.OperateLog;
import com.zxy.aiplanner.common.Result;
import com.zxy.aiplanner.constant.OperationTypeConstants;
import com.zxy.aiplanner.entity.TStudyCheckin;
import com.zxy.aiplanner.entity.TStudyPlanDayTask;
import com.zxy.aiplanner.entity.TStudyPlanTotal;
import com.zxy.aiplanner.entity.TUserInfo;
import com.zxy.aiplanner.exception.BusinessException;
import com.zxy.aiplanner.service.TStudyCheckinService;
import com.zxy.aiplanner.service.TStudyPlanDayTaskService;
import com.zxy.aiplanner.service.TStudyPlanStageService;
import com.zxy.aiplanner.service.TStudyPlanTotalService;
import com.zxy.aiplanner.service.TUserInfoService;
import com.zxy.aiplanner.utils.UserContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户基础信息接口
 */
@RestController
@RequestMapping("/api/v1/user")
public class TUserInfoController {

    private final TUserInfoService userInfoService;
    private final TStudyCheckinService checkinService;
    private final TStudyPlanDayTaskService dayTaskService;
    private final TStudyPlanTotalService planTotalService;
    private final TStudyPlanStageService planStageService;

    public TUserInfoController(TUserInfoService userInfoService,
                               TStudyCheckinService checkinService,
                               TStudyPlanDayTaskService dayTaskService,
                               TStudyPlanTotalService planTotalService,
                               TStudyPlanStageService planStageService) {
        this.userInfoService = userInfoService;
        this.checkinService = checkinService;
        this.dayTaskService = dayTaskService;
        this.planTotalService = planTotalService;
        this.planStageService = planStageService;
    }

    // ==================== VO ====================

    public record UserProfileVO(Long id, String loginName, String nickname, String avatarUrl) {}

    public record UserStatsVO(
            int checkinDays,
            int doneTasks,
            int totalHours
    ) {}

    // ==================== 接口 ====================

    /**
     * 获取当前用户个人资料
     * GET /api/v1/user/me
     */
    @OperateLog(module = "用户信息", type = OperationTypeConstants.QUERY_CURRENT_USER_PROFILE)
    @GetMapping("/me")
    public Result<UserProfileVO> me() {
        Long userId = requireUserId();
        TUserInfo userInfo = userInfoService.getById(userId);
        if (userInfo == null) throw new BusinessException(404, "用户不存在");
        return Result.success(new UserProfileVO(
                userInfo.getId(),
                userInfo.getLoginName(),
                userInfo.getNickname(),
                userInfo.getAvatarUrl()
        ));
    }

    /**
     * 获取当前用户学习统计数据
     * GET /api/v1/user/stats
     *
     * 返回：
     *   checkinDays  - 历史总打卡天数
     *   doneTasks    - 已完成任务总数
     *   totalHours   - 累计实际学习时长（小时，向下取整）
     */
    @OperateLog(module = "用户信息", type = OperationTypeConstants.STATS_QUERY)
    @GetMapping("/stats")
    public Result<UserStatsVO> stats() {
        Long userId = requireUserId();

        // 1. 历史总打卡天数
        int checkinDays = (int) checkinService.lambdaQuery()
                .eq(TStudyCheckin::getUserId, userId)
                .eq(TStudyCheckin::getCheckinStatus, 1)
                .count().intValue();

        // 2. 已完成任务总数 + 累计实际时长
        //    通过当前用户所有计划下所有阶段的任务统计
        int doneTasks = 0;
        int totalMinutes = 0;

        List<TStudyPlanTotal> plans = planTotalService.lambdaQuery()
                .eq(TStudyPlanTotal::getUserId, userId)
                .list();

        for (var plan : plans) {
            List<Long> stageIds = planStageService.getStagesByPlanId(plan.getId())
                    .stream().map(s -> s.getId()).toList();
            if (stageIds.isEmpty()) continue;

            List<TStudyPlanDayTask> tasks = dayTaskService.lambdaQuery()
                    .in(TStudyPlanDayTask::getStageId, stageIds)
                    .list();

            for (TStudyPlanDayTask t : tasks) {
                if (t.getTaskStatus() != null && t.getTaskStatus() == 1) {
                    doneTasks++;
                    totalMinutes += t.getActualDurationMinutes() == null ? 0 : t.getActualDurationMinutes();
                }
            }
        }

        int totalHours = totalMinutes / 60;

        return Result.success(new UserStatsVO(checkinDays, doneTasks, totalHours));
    }

    /**
     * 按 ID 查询用户信息（管理侧使用，路径改为 /info/{id} 避免与 /stats 冲突）
     * GET /api/v1/user/info/{id}
     */
    @OperateLog(module = "用户信息", type = OperationTypeConstants.QUERY_BY_ID)
    @GetMapping("/info/{id}")
    public Result<TUserInfo> getUserInfoById(@PathVariable("id") Long id) {
        TUserInfo userInfo = userInfoService.getById(id);
        if (userInfo == null) throw new BusinessException(404, "用户不存在");
        userInfo.setPasswordHash(null);
        return Result.success(userInfo);
    }

    // ==================== 私有工具 ====================

    private Long requireUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) throw new BusinessException(401, "未登录或token已过期");
        return userId;
    }
}
