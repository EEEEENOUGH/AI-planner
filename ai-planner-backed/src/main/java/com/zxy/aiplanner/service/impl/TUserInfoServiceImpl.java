package com.zxy.aiplanner.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxy.aiplanner.entity.TUserInfo;
import com.zxy.aiplanner.exception.BusinessException;
import com.zxy.aiplanner.mapper.TUserInfoMapper;
import com.zxy.aiplanner.service.TUserInfoService;
import com.zxy.aiplanner.utils.JwtUtils;
import com.zxy.aiplanner.utils.RedisUtils;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author lenovo
 * @description 针对表【t_user_info(用户基础信息表)】的数据库操作Service实现
 * @createDate 2026-03-25 17:50:23
 */
@Service
public class TUserInfoServiceImpl extends ServiceImpl<TUserInfoMapper, TUserInfo>
        implements TUserInfoService {

    private static final Logger log = LoggerFactory.getLogger(TUserInfoServiceImpl.class);
    private static final String LOGIN_TOKEN_KEY_PREFIX = "auth:token:user:";

    private final JwtUtils jwtUtils;
    private final RedisUtils redisUtils;

    public TUserInfoServiceImpl(JwtUtils jwtUtils, RedisUtils redisUtils) {
        this.jwtUtils = jwtUtils;
        this.redisUtils = redisUtils;
    }

    @Override
    public String register(String loginName, String password, String nickname) {
        if (loginName == null || loginName.isBlank()) {
            throw new BusinessException(400, "账号不能为空");
        }
        if (password == null || password.isBlank()) {
            throw new BusinessException(400, "密码不能为空");
        }

        boolean exists = this.lambdaQuery()
                .eq(TUserInfo::getLoginName, loginName)
                .exists();
        if (exists) {
            throw new BusinessException(400, "账号已存在");
        }

        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());

        TUserInfo user = new TUserInfo();
        user.setLoginName(loginName);
        user.setPasswordHash(passwordHash);
        user.setNickname(nickname == null ? "" : nickname);
        user.setUserStatus(1);

        boolean saved = this.save(user);
        if (!saved || user.getId() == null) {
            throw new BusinessException(500, "注册失败");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getLoginName());
        cacheLoginToken(user.getId(), token);
        return token;
    }

    @Override
    public String login(String loginName, String password) {
        if (loginName == null || loginName.isBlank()) {
            throw new BusinessException(400, "账号不能为空");
        }
        if (password == null || password.isBlank()) {
            throw new BusinessException(400, "密码不能为空");
        }

        TUserInfo user = this.lambdaQuery()
                .eq(TUserInfo::getLoginName, loginName)
                .one();
        if (user == null) {
            throw new BusinessException(400, "账号或密码错误");
        }
        if (user.getUserStatus() != null && user.getUserStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }

        boolean ok = BCrypt.checkpw(password, user.getPasswordHash());
        if (!ok) {
            throw new BusinessException(400, "账号或密码错误");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getLoginName());
        cacheLoginToken(user.getId(), token);
        return token;
    }

    @Override
    public void logout(Long userId) {
        if (userId == null) {
            throw new BusinessException(401, "未登录或token已过期");
        }
        redisUtils.del(buildLoginTokenKey(userId));
    }

    private void cacheLoginToken(Long userId, String token) {
        if (userId == null || token == null || token.isBlank()) {
            return;
        }
        String key = buildLoginTokenKey(userId);
        boolean success = redisUtils.setWithExpire(key, token, 7, TimeUnit.DAYS);
        if (!success) {
            log.warn("登录态缓存失败，降级为仅返回 JWT，key={}, userId={}", key, userId);
        }
    }

    private String buildLoginTokenKey(Long userId) {
        return LOGIN_TOKEN_KEY_PREFIX + userId;
    }
}
