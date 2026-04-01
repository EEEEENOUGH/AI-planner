package com.zxy.aiplanner.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zxy.aiplanner.config.DeepSeekProperties;
import com.zxy.aiplanner.dto.ChatRequest;
import com.zxy.aiplanner.dto.ChatResponse;
import com.zxy.aiplanner.dto.Message;
import com.zxy.aiplanner.exception.BusinessException;
import com.zxy.aiplanner.service.DeepSeekService;
import com.zxy.aiplanner.utils.DeepSeekUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class DeepSeekServiceImpl implements DeepSeekService {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekServiceImpl.class);
    private static final int MAX_CONTEXT_MESSAGES = 200;
    private static final int MAX_CONTENT_CHARS = 2000000;

    private final RestTemplate deepSeekRestTemplate;
    private final DeepSeekProperties properties;
    private final ObjectMapper objectMapper;

    public DeepSeekServiceImpl(RestTemplate deepSeekRestTemplate, DeepSeekProperties properties, ObjectMapper objectMapper) {
        this.deepSeekRestTemplate = deepSeekRestTemplate;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public String chatSingle(String prompt) {
        List<Message> messages = new ArrayList<>();
        messages.add(DeepSeekUtils.buildSystemMessage(properties));
        messages.add(new Message("user", prompt));
        return chatWithHistory(messages);
    }

    @Override
    public String chatWithHistory(List<Message> messages) {
        try {
            List<Message> normalizedMessages = normalizeMessages(messages);
            ChatRequest request = DeepSeekUtils.buildRequest(properties, normalizedMessages, false);
            HttpEntity<ChatRequest> entity = new HttpEntity<>(request, buildHeaders());
            ResponseEntity<ChatResponse> response = deepSeekRestTemplate.postForEntity(
                    properties.getBaseUrl(),
                    entity,
                    ChatResponse.class
            );
            String content = DeepSeekUtils.extractText(response.getBody());
            if (content == null || content.isBlank()) {
                return DeepSeekUtils.fallbackMessage();
            }
            return content;
        } catch (HttpClientErrorException e) {
            log.error("DeepSeek 调用失败，状态码={}, 响应体={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new BusinessException(502, DeepSeekUtils.fallbackMessage());
        } catch (ResourceAccessException e) {
            log.error("DeepSeek 调用超时或网络异常", e);
            throw new BusinessException(504, DeepSeekUtils.fallbackMessage());
        } catch (Exception e) {
            log.error("DeepSeek 调用异常", e);
            throw new BusinessException(500, DeepSeekUtils.fallbackMessage());
        }
    }

    @Override
    public SseEmitter chatStream(String prompt) {
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("user", prompt));
        return chatStreamWithHistory(messages);
    }

    @Override
    public SseEmitter chatStreamWithHistory(List<Message> messages) {
        SseEmitter emitter = new SseEmitter(0L);

        CompletableFuture.runAsync(() -> {
            List<Message> normalizedMessages = normalizeMessages(messages);
            ChatRequest request = DeepSeekUtils.buildRequest(properties, normalizedMessages, true);

            try {
                deepSeekRestTemplate.execute(
                        properties.getBaseUrl(),
                        HttpMethod.POST,
                        clientHttpRequest -> {
                            HttpHeaders headers = buildHeaders();
                            headers.setContentType(MediaType.APPLICATION_JSON);
                            clientHttpRequest.getHeaders().putAll(headers);
                            objectMapper.writeValue(clientHttpRequest.getBody(), request);
                        },
                        response -> {
                            consumeStream(response, emitter);
                            return null;
                        }
                );
            } catch (Exception e) {
                log.error("DeepSeek 流式调用异常", e);
                sendSafe(emitter, DeepSeekUtils.fallbackMessage(), "error");
                emitter.completeWithError(new BusinessException(500, DeepSeekUtils.fallbackMessage()));
            }
        });

        return emitter;
    }

    private void consumeStream(ClientHttpResponse response, SseEmitter emitter) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || !line.startsWith("data:")) {
                    continue;
                }
                String data = line.substring(5).trim();
                if ("[DONE]".equals(data)) {
                    emitter.complete();
                    return;
                }

                try {
                    JsonNode root = objectMapper.readTree(data);
                    JsonNode contentNode = root.path("choices").path(0).path("delta").path("content");
                    if (!contentNode.isMissingNode() && !contentNode.isNull()) {
                        String content = contentNode.asText();
                        if (!content.isBlank()) {
                            sendSafe(emitter, content, "message");
                        }
                    }
                } catch (Exception parseEx) {
                    log.error("解析 DeepSeek 流式数据失败，raw={}", data, parseEx);
                }
            }
            emitter.complete();
        } catch (Exception e) {
            log.error("读取 DeepSeek 流式响应失败", e);
            sendSafe(emitter, DeepSeekUtils.fallbackMessage(), "error");
            emitter.completeWithError(new BusinessException(500, DeepSeekUtils.fallbackMessage()));
        }
    }

    private void sendSafe(SseEmitter emitter, String data, String event) {
        try {
            emitter.send(SseEmitter.event().name(event).data(data));
        } catch (Exception sendEx) {
            log.error("SSE 发送失败", sendEx);
        }
    }

    private List<Message> normalizeMessages(List<Message> messages) {
        List<Message> normalized = new ArrayList<>();
        if (messages != null) {
            for (Message message : messages) {
                if (message == null || message.getRole() == null || message.getContent() == null) {
                    continue;
                }
                String role = message.getRole().trim();
                if (!"user".equals(role) && !"assistant".equals(role)) {
                    continue;
                }
                String content = message.getContent().trim();
                if (content.isEmpty()) {
                    continue;
                }
                if (content.length() > MAX_CONTENT_CHARS) {
                    content = content.substring(content.length() - MAX_CONTENT_CHARS);
                }
                normalized.add(new Message(role, content));
            }
        }

        if (normalized.size() > MAX_CONTEXT_MESSAGES) {
            normalized = new ArrayList<>(normalized.subList(normalized.size() - MAX_CONTEXT_MESSAGES, normalized.size()));
        }

        normalized.add(0, DeepSeekUtils.buildSystemMessage(properties));
        return normalized;
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.getApiKey());
        return headers;
    }
}
