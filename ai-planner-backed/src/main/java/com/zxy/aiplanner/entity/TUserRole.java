package com.zxy.aiplanner.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户-角色关联表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user_role")
public class TUserRole extends BaseEntity {

    private Long userId;
    private Long roleId;
    private Integer bindStatus;
}
