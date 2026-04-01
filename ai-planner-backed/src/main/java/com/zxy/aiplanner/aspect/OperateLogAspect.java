package com.zxy.aiplanner.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zxy.aiplanner.annotation.OperateLog;
import com.zxy.aiplanner.entity.TOperationLog;
import com.zxy.aiplanner.service.TOperationLogService;
import com.zxy.aiplanner.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 操作日志切面：拦截 @OperateLog 并异步落库。
 */
@Aspect
@Component
public class OperateLogAspect {

    private final TOperationLogService operationLogService;
    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    public OperateLogAspect(TOperationLogService operationLogService, JwtUtils jwtUtils, ObjectMapper objectMapper) {
        this.operationLogService = operationLogService;
        this.jwtUtils = jwtUtils;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(com.zxy.aiplanner.annotation.OperateLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Throwable throwable = null;
        Object result = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            throwable = ex;
            throw ex;
        } finally {
            saveOperationLogAsync(joinPoint, result, throwable, System.currentTimeMillis() - start);
        }
    }

    private void saveOperationLogAsync(ProceedingJoinPoint joinPoint, Object result, Throwable throwable, long costMs) {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return;
        }

        HttpServletRequest request = servletRequestAttributes.getRequest();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        OperateLog operateLog = signature.getMethod().getAnnotation(OperateLog.class);
        if (operateLog == null) {
            return;
        }

        TOperationLog logEntity = new TOperationLog();
        logEntity.setUserId(parseUserIdFromHeaderToken(request.getHeader("Authorization")));
        logEntity.setOperationType(operateLog.type());
        logEntity.setRequestMethod(request.getMethod());
        logEntity.setRequestPath(request.getRequestURI());
        logEntity.setIpAddress(getClientIp(request));
        logEntity.setUserAgent(request.getHeader("User-Agent"));

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("module", operateLog.module());
        detail.put("type", operateLog.type());
        detail.put("args", toJsonSafe(filterArgs(joinPoint.getArgs())));
        detail.put("result", toJsonSafe(result));
        detail.put("costMs", costMs);
        detail.put("success", throwable == null);
        if (throwable != null) {
            detail.put("error", throwable.getMessage());
        }
        logEntity.setOperationDetail(toJsonSafe(detail));

        CompletableFuture.runAsync(() -> operationLogService.save(logEntity));
    }

    private Long parseUserIdFromHeaderToken(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return null;
        }
        String token = authorization.startsWith("Bearer ")
                ? authorization.substring("Bearer ".length()).trim()
                : authorization.trim();
        if (token.isBlank() || !jwtUtils.validateToken(token)) {
            return null;
        }
        Claims claims = jwtUtils.parseClaims(token);
        Object userIdObj = claims.get("userId");
        if (userIdObj instanceof Number number) {
            return number.longValue();
        }
        if (userIdObj instanceof String userIdStr && !userIdStr.isBlank()) {
            return Long.parseLong(userIdStr);
        }
        String subject = claims.getSubject();
        if (subject != null && !subject.isBlank()) {
            return Long.parseLong(subject);
        }
        return null;
    }

    private Object[] filterArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return new Object[0];
        }
        return Arrays.stream(args)
                .filter(arg -> !(arg instanceof HttpServletRequest))
                .toArray();
    }

    private String toJsonSafe(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return String.valueOf(obj);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        return request.getRemoteAddr();
    }
}
