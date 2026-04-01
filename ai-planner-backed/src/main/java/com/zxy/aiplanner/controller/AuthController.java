package com.zxy.aiplanner.controller;

import com.zxy.aiplanner.annotation.OperateLog;
import com.zxy.aiplanner.common.Result;
import com.zxy.aiplanner.controller.dto.LoginDTO;
import com.zxy.aiplanner.controller.dto.RegisterDTO;
import com.zxy.aiplanner.service.TUserInfoService;
import com.zxy.aiplanner.utils.UserContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口（注册/登录/登出）
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final TUserInfoService userInfoService;

    public AuthController(TUserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    public record TokenVO(String token) {
    }

    @OperateLog(module = "认证模块", type = 1)
    @PostMapping("/register")
    public Result<TokenVO> register(@RequestBody RegisterDTO dto) {
        String token = userInfoService.register(dto.loginName(), dto.password(), dto.nickname());
        return Result.success(new TokenVO(token));
    }

    @OperateLog(module = "认证模块", type = 2)
    @PostMapping("/login")
    public Result<TokenVO> login(@RequestBody LoginDTO dto) {
        String token = userInfoService.login(dto.loginName(), dto.password());
        return Result.success(new TokenVO(token));
    }

    @OperateLog(module = "认证模块", type = 4)
    @PostMapping("/logout")
    public Result<Void> logout() {
        userInfoService.logout(UserContext.getUserId());
        return Result.success();
    }
}
