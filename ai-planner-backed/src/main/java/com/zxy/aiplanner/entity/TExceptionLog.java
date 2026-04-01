package com.zxy.aiplanner.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统异常日志记录表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_exception_log")
public class TExceptionLog extends BaseEntity {

    private Long userId;
    private Integer exceptionLevel;
    private String exceptionType;
    private String exceptionMessage;
    private String stackTrace;
    private String requestMethod;
    private String requestPath;
    private Integer handledStatus;
    private String logSource;
}
