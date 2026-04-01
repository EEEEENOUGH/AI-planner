package com.zxy.aiplanner.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统操作日志表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_operation_log")
public class TOperationLog extends BaseEntity {

    private Long userId;
    private Integer operationType;
    private String requestMethod;
    private String requestPath;
    private String ipAddress;
    private String userAgent;
    private String operationDetail;
}
