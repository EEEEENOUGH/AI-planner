package com.zxy.aiplanner.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DeepSeek 配置项
 */
@ConfigurationProperties(prefix = "app.deepseek")
public class DeepSeekProperties {

    /**
     * API Key
     */
    private String apiKey;

    /**
     * 完整请求地址，如：https://api.deepseek.com/v1/chat/completions
     */
    private String baseUrl;

    /**
     * 模型名称，默认 deepseek-chat
     */
    private String model = "deepseek-chat";

    /**
     * 连接超时（毫秒）
     */
    private int connectTimeoutMs = 5000;

    /**
     * 读取超时（毫秒）
     */
    private int readTimeoutMs = 60000;

    /**
     * 最大 token
     */
    private Integer maxTokens = 2048;

    /**
     * 温度
     */
    private Double temperature = 0.7;

    /**
     * 系统提示词模板
     */
    private String systemPrompt = "你是一个专业、耐心、结构化的考研复习规划师。请结合用户输入，给出可执行的学习建议。";

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }
}
