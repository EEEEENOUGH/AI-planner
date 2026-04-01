package com.zxy.aiplanner.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zxy.aiplanner.annotation.OperateLog;
import com.zxy.aiplanner.common.Result;
import com.zxy.aiplanner.constant.OperationTypeConstants;
import com.zxy.aiplanner.dto.Message;
import com.zxy.aiplanner.entity.TAiQaHistory;
import com.zxy.aiplanner.exception.BusinessException;
import com.zxy.aiplanner.service.AiChatBusinessService;
import com.zxy.aiplanner.service.DeepSeekService;
import com.zxy.aiplanner.service.TAiQaHistoryService;
import com.zxy.aiplanner.utils.UserContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI答疑控制器。
 */
@Validated
@RestController
@RequestMapping("/api/v1/ai")
public class AiQaController {

    private final DeepSeekService deepSeekService;
    private final AiChatBusinessService aiChatBusinessService;
    private final TAiQaHistoryService aiQaHistoryService;

    public AiQaController(DeepSeekService deepSeekService,
                          AiChatBusinessService aiChatBusinessService,
                          TAiQaHistoryService aiQaHistoryService) {
        this.deepSeekService = deepSeekService;
        this.aiChatBusinessService = aiChatBusinessService;
        this.aiQaHistoryService = aiQaHistoryService;
    }

    @GetMapping("/test")
    public Result<String> test(@RequestParam(value = "prompt", defaultValue = "请给我一份今天的考研复习计划") String prompt) {
        return Result.success(deepSeekService.chatSingle(prompt));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam(value = "prompt", defaultValue = "请给我一份今天的考研复习计划") String prompt) {
        return deepSeekService.chatStream(prompt);
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody List<Message> messages) {
        return deepSeekService.chatStreamWithHistory(messages);
    }

    @OperateLog(module = "AI答疑", type = OperationTypeConstants.AI_QA_CHAT)
    @PostMapping("/history/save")
    public Result<Void> saveHistory(@RequestBody @Valid SaveAiHistoryDTO dto) {
        aiChatBusinessService.saveChatHistory(dto.messages(), dto.answerContent());
        return Result.success();
    }

    @OperateLog(module = "AI答疑", type = OperationTypeConstants.AI_QA_CHAT)
    @PostMapping("/chat")
    public Result<String> chat(@RequestBody List<Message> messages) {
        String answer = aiChatBusinessService.processChatAndSaveHistory(messages);
        return Result.success(answer);
    }

    @OperateLog(module = "AI答疑", type = OperationTypeConstants.AI_QA_HISTORY_QUERY)
    @GetMapping("/history")
    public Result<IPage<AiQaHistoryVO>> history(@RequestParam(value = "pageNum", defaultValue = "1") long pageNum,
                                                @RequestParam(value = "pageSize", defaultValue = "20") long pageSize) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录或token已过期");
        }
        if (pageNum < 1 || pageSize < 1) {
            throw new BusinessException(400, "分页参数不合法");
        }

        Page<TAiQaHistory> page = new Page<>(pageNum, pageSize);
        IPage<TAiQaHistory> entityPage = aiQaHistoryService.lambdaQuery()
                .eq(TAiQaHistory::getUserId, userId)
                .orderByDesc(TAiQaHistory::getCreateTime)
                .page(page);

        IPage<AiQaHistoryVO> voPage = entityPage.convert(item -> new AiQaHistoryVO(
                item.getId(),
                item.getQuestionText(),
                item.getAiAnswerText(),
                item.getCreateTime()
        ));

        return Result.success(voPage);
    }

    public record AiQaHistoryVO(Long id, String questionContent, String answerContent,
                                java.time.LocalDateTime createTime) {
    }

    public record SaveAiHistoryDTO(@NotNull List<Message> messages,
                                   @NotBlank String answerContent) {
    }
}
