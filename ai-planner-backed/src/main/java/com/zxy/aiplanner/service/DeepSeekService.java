package com.zxy.aiplanner.service;

import com.zxy.aiplanner.dto.Message;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface DeepSeekService {

    String chatSingle(String prompt);

    SseEmitter chatStream(String prompt);

    SseEmitter chatStreamWithHistory(List<Message> messages);

    String chatWithHistory(List<Message> messages);
}
