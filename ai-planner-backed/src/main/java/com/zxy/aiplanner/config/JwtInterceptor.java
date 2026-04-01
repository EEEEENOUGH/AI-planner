package com.zxy.aiplanner.config;

import com.zxy.aiplanner.exception.BusinessException;
import com.zxy.aiplanner.utils.JwtUtils;
import com.zxy.aiplanner.utils.RedisUtils;
import com.zxy.aiplanner.utils.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 拦截器：校验请求头 Authorization
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final String LOGIN_TOKEN_KEY_PREFIX = "auth:token:user:";

    private final JwtUtils jwtUtils;
    private final RedisUtils redisUtils;

    public JwtInterceptor(JwtUtils jwtUtils, RedisUtils redisUtils) {
        this.jwtUtils = jwtUtils;
        this.redisUtils = redisUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank()) {
            throw new BusinessException(401, "未登录或token已过期");
        }

        String token = authorization;
        if (authorization.startsWith("Bearer ")) {
            token = authorization.substring("Bearer ".length()).trim();
        }

        if (!jwtUtils.validateToken(token)) {
            throw new BusinessException(401, "未登录或token已过期");
        }

        Claims claims = jwtUtils.parseClaims(token);
        Object userIdObj = claims.get("userId");
        Long userId = null;
        if (userIdObj instanceof Number number) {
            userId = number.longValue();
        } else if (userIdObj instanceof String s && !s.isBlank()) {
            userId = Long.parseLong(s);
        } else if (claims.getSubject() != null && !claims.getSubject().isBlank()) {
            userId = Long.parseLong(claims.getSubject());
        }

        if (userId == null) {
            throw new BusinessException(401, "未登录或token已过期");
        }

        String cachedToken = redisUtils.getString(LOGIN_TOKEN_KEY_PREFIX + userId);
        if (cachedToken == null || !cachedToken.equals(token)) {
            throw new BusinessException(401, "未登录或token已过期");
        }

        UserContext.setUserId(userId);

        request.setAttribute("userId", userId);
        request.setAttribute("loginName", claims.get("loginName"));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.remove();
    }
}
