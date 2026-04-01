package com.zxy.aiplanner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zxy.aiplanner.entity.TUserInfo;

/**
 * @author lenovo
 * @description 针对表【t_user_info(用户基础信息表)】的数据库操作Service
 * @createDate 2026-03-25 17:50:23
 */
public interface TUserInfoService extends IService<TUserInfo> {

    /**
     * 注册：创建用户并返回 token
     */
    String register(String loginName, String password, String nickname);

    /**
     * 登录：校验账号密码并返回 token
     */
    String login(String loginName, String password);

    /**
     * 登出：清理当前用户登录态缓存
     */
    void logout(Long userId);
}
