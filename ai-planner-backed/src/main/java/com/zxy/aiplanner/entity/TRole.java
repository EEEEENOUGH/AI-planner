package com.zxy.aiplanner.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统角色表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_role")
public class TRole extends BaseEntity {

    private String roleName;
    private Integer roleType;
    private Integer roleStatus;
}
