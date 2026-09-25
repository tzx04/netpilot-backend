package com.netpilot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;  // ← 改成这个

@Data
@Component
@ConfigurationProperties(prefix = "ai.llm")
public class AiConfig {
    private String apiKey;
    private String baseUrl;
    private String model;

    @PostConstruct
    public void check() {
        System.out.println("==================== AI CONFIG ====================");
        System.out.println("apiKey: " + (apiKey != null ? "已配置(长度=" + apiKey.length() + ")" : "NULL"));
        System.out.println("baseUrl: " + (baseUrl != null ? baseUrl : "NULL"));
        System.out.println("model: " + (model != null ? model : "NULL"));
        System.out.println("===================================================");
    }
}