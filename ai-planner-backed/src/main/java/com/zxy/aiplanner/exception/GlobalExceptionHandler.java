package com.zxy.aiplanner.exception;

import com.zxy.aiplanner.common.Result;
import com.zxy.aiplanner.entity.TExceptionLog;
import com.zxy.aiplanner.service.TExceptionLogService;
import com.zxy.aiplanner.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 全局异常处理：把异常统一转换为 {@link Result}
 * 避免向前端暴露原生堆栈
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final TExceptionLogService exceptionLogService;

    public GlobalExceptionHandler(TExceptionLogService exceptionLogService) {
        this.exceptionLogService = exceptionLogService;
    }

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        saveExceptionLogAsync(ex, request, 1, 1, "BUSINESS");
        return Result.error(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception ex, HttpServletRequest request) {
        // 服务端日志保留堆栈信息，前端只返回统一错误信息
        log.error("系统异常", ex);
        saveExceptionLogAsync(ex, request, 2, 1, "SYSTEM");
        return Result.error(500, "系统异常，请稍后再试");
    }

    private void saveExceptionLogAsync(Exception ex,
                                       HttpServletRequest request,
                                       int exceptionLevel,
                                       int handledStatus,
                                       String logSource) {
        TExceptionLog exceptionLog = new TExceptionLog();
        exceptionLog.setUserId(UserContext.getUserId());
        exceptionLog.setExceptionLevel(exceptionLevel);
        exceptionLog.setExceptionType(ex.getClass().getName());
        exceptionLog.setExceptionMessage(ex.getMessage());
        exceptionLog.setStackTrace(getStackTrace(ex));
        exceptionLog.setRequestMethod(request.getMethod());
        exceptionLog.setRequestPath(buildRequestPathWithQuery(request));
        exceptionLog.setHandledStatus(handledStatus);
        exceptionLog.setLogSource(logSource + "|params=" + buildRequestParams(request));

        CompletableFuture.runAsync(() -> exceptionLogService.save(exceptionLog));
    }

    private String buildRequestPathWithQuery(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (queryString == null || queryString.isBlank()) {
            return request.getRequestURI();
        }
        return request.getRequestURI() + "?" + queryString;
    }

    private String buildRequestParams(HttpServletRequest request) {
        try {
            Map<String, String[]> parameterMap = request.getParameterMap();
            if (parameterMap == null || parameterMap.isEmpty()) {
                return "{}";
            }
            StringBuilder builder = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                if (!first) {
                    builder.append(", ");
                }
                first = false;
                builder.append(entry.getKey()).append("=");
                String[] values = entry.getValue();
                if (values == null) {
                    builder.append("null");
                } else if (values.length == 1) {
                    builder.append(values[0]);
                } else {
                    builder.append("[");
                    for (int i = 0; i < values.length; i++) {
                        if (i > 0) {
                            builder.append(",");
                        }
                        builder.append(values[i]);
                    }
                    builder.append("]");
                }
            }
            builder.append("}");
            return builder.toString();
        } catch (Exception ignored) {
            return "{parseError=true}";
        }
    }

    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
            return sw.toString();
        }
    }
}
