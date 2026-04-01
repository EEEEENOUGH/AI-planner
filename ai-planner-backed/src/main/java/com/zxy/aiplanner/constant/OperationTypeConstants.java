package com.zxy.aiplanner.constant;
/**
 * 操作日志类型常量。
 */
public final class OperationTypeConstants {
    private OperationTypeConstants() {
    }
    /** 通用查询（按ID查看） */
    public static final int QUERY_BY_ID = 5;
    /** 查询当前用户个人信息 */
    public static final int QUERY_CURRENT_USER_PROFILE = 6;
    /** AI答疑对话 */
    public static final int AI_QA_CHAT = 7;
    /** AI答疑历史查询 */
    public static final int AI_QA_HISTORY_QUERY = 8;
    /** 查询备考档案 */
    public static final int ARCHIVE_QUERY = 9;
    /** 保存/更新备考档案 */
    public static final int ARCHIVE_SAVE = 10;
    /** AI 生成复习计划 */
    public static final int PLAN_GENERATE = 11;
    /** 查询复习计划 */
    public static final int PLAN_QUERY = 12;
    /** 手动修改阶段 */
    public static final int PLAN_STAGE_UPDATE = 13;
    /** 查询每日任务 */
    public static final int TASK_QUERY = 14;
    /** 新增每日任务 */
    public static final int TASK_CREATE = 15;
    /** 更新每日任务 */
    public static final int TASK_UPDATE = 16;
    /** 更新任务状态（完成/跳过） */
    public static final int TASK_STATUS_UPDATE = 17;
    /** 删除每日任务 */
    public static final int TASK_DELETE = 18;
    /** 每日打卡 */
    public static final int CHECKIN = 19;
    /** 查询学习统计数据 */
    public static final int STATS_QUERY = 20;
    /** 修改用户资料 */
    public static final int UPDATE_PROFILE = 21;
}