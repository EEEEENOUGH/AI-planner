package com.zxy.aiplanner.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DeepSeek Chat Completions 请求体
 */
@Data
public class ChatRequest {

    private String model;
    private List<Message> messages;
    private Boolean stream;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    private Double temperature;
}
