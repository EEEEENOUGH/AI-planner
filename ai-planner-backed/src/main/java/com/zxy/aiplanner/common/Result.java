package com.zxy.aiplanner.common;

/**
 * 统一 RESTful 响应体
 *
 * @param <T> 返回数据类型
 */
public record Result<T>(int code, String msg, T data) {

    public static <T> Result<T> success() {
        return new Result<>(0, "成功", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(0, "成功", data);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }

    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    public static <T> Result<T> error() {
        return new Result<>(500, "系统异常，请稍后再试", null);
    }
}

