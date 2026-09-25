package com.netpilot.dto;

import lombok.Data;

@Data
public class AiResponse {
    private boolean success;
    private String content;   // AI 回复的具体内容
    private String model;     // 使用的模型名称
    // 可以在这里添加更多你需要的字段，比如消耗的 token 等
}