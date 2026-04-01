package com.zxy.aiplanner.utils;

/**
 * 轻量级用户上下文（ThreadLocal）
 * 注意：必须在请求结束时 remove，避免线程池复用导致数据串流/内存泄漏。
 */
public final class UserContext {

    private UserContext() {
    }

    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();

    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    public static void remove() {
        USER_ID_HOLDER.remove();
    }
}

