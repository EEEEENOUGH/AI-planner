package com.zxy.aiplanner.service;
import com.zxy.aiplanner.dto.Message;
import java.util.List;
/**
 * AI答疑组合业务服务。
 */
public interface AiChatBusinessService {
    /**
     * 处理多轮对话并落库问答历史。
     *
     * @param messages 多轮对话消息
     * @return AI回答
     */
    String processChatAndSaveHistory(List<Message> messages);

    /**
     * 仅保存问答历史（用于流式返回后前端回传最终答案）。
     *
     * @param messages 对话上下文
     * @param aiAnswer AI最终回答
     */
    void saveChatHistory(List<Message> messages, String aiAnswer);
}
