package com.zxy.aiplanner.utils;

import com.zxy.aiplanner.config.DeepSeekProperties;
import com.zxy.aiplanner.dto.ChatRequest;
import com.zxy.aiplanner.dto.ChatResponse;
import com.zxy.aiplanner.dto.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * DeepSeek 轻量工具类
 */
public final class DeepSeekUtils {

    private DeepSeekUtils() {
    }

    public static Message buildSystemMessage(DeepSeekProperties properties) {
        return new Message("system", properties.getSystemPrompt());
    }

    public static ChatRequest buildRequest(DeepSeekProperties properties, List<Message> messages, boolean stream) {
        ChatRequest request = new ChatRequest();
        request.setModel(properties.getModel());
        request.setMessages(new ArrayList<>(messages));
        request.setStream(stream);
        request.setMaxTokens(properties.getMaxTokens());
        request.setTemperature(properties.getTemperature());
        return request;
    }

    public static String extractText(ChatResponse response) {
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            return "";
        }
        ChatResponse.Choice first = response.getChoices().get(0);
        if (first.getMessage() == null || first.getMessage().getContent() == null) {
            return "";
        }
        return first.getMessage().getContent();
    }

    public static String fallbackMessage() {
        return "AI 导师正在休息，请稍后再试";
    }
}
