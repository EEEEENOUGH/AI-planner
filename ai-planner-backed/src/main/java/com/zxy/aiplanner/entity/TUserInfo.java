package com.zxy.aiplanner.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户基础信息表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user_info")
public class TUserInfo extends BaseEntity {

    private String loginName;
    private String passwordHash;
    private String nickname;
    private String phone;
    private String email;
    private String avatarUrl;
    private Integer gender;
    private Integer userStatus;
    private LocalDateTime lastLoginTime;
    private String createIp;
}
