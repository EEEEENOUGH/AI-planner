package com.zxy.aiplanner.service.impl;

import com.zxy.aiplanner.dto.Message;
import com.zxy.aiplanner.entity.TAiQaHistory;
import com.zxy.aiplanner.exception.BusinessException;
import com.zxy.aiplanner.service.AiChatBusinessService;
import com.zxy.aiplanner.service.DeepSeekService;
import com.zxy.aiplanner.service.TAiQaHistoryService;
import com.zxy.aiplanner.utils.UserContext;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI答疑组合业务服务实现。
 */
@Service
public class AiChatBusinessServiceImpl implements AiChatBusinessService {

    private final DeepSeekService deepSeekService;
    private final TAiQaHistoryService aiQaHistoryService;

    public AiChatBusinessServiceImpl(DeepSeekService deepSeekService, TAiQaHistoryService aiQaHistoryService) {
        this.deepSeekService = deepSeekService;
        this.aiQaHistoryService = aiQaHistoryService;
    }

    @Override
    public String processChatAndSaveHistory(List<Message> messages) {
        String aiAnswer = deepSeekService.chatWithHistory(messages);
        saveChatHistory(messages, aiAnswer);
        return aiAnswer;
    }

    @Override
    public void saveChatHistory(List<Message> messages, String aiAnswer) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        if (messages == null || messages.isEmpty()) {
            throw new BusinessException(400, "对话消息不能为空");
        }
        if (aiAnswer == null || aiAnswer.isBlank()) {
            throw new BusinessException(400, "AI回答不能为空");
        }

        String questionContent = extractLastUserQuestion(messages);
        TAiQaHistory history = new TAiQaHistory();
        history.setUserId(userId);
        history.setQuestionText(questionContent);
        history.setAiAnswerText(aiAnswer);
        history.setAnswerStatus(1);
        // createTime / updateTime / isDeleted 由 MybatisPlusMetaObjectHandler 自动填充
        aiQaHistoryService.save(history);
    }

    private String extractLastUserQuestion(List<Message> messages) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message message = messages.get(i);
            if (message == null) continue;
            String role = message.getRole();
            if ("user".equalsIgnoreCase(role)) {
                String content = message.getContent();
                if (content == null || content.isBlank()) {
                    throw new BusinessException(400, "用户问题不能为空");
                }
                return content;
            }
        }
        throw new BusinessException(400, "对话中未找到用户提问");
    }
}
