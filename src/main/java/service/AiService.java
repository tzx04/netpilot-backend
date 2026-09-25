package com.netpilot.service;

public interface AiService {

    /**
     * 通用对话
     */
    String chat(String userMessage);

    /**
     * 系统提示 + 用户对话
     */
    String chat(String systemPrompt, String userMessage);

    /**
     * 网络故障诊断
     */
    String diagnoseDevice(Long deviceId);

    /**
     * 获取 API Key（供流式接口使用）
     */
    String getApiKey();
}