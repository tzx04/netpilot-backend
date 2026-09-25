package com.netpilot.service.impl;

import com.netpilot.entity.Device;
import com.netpilot.mapper.DeviceMapper;
import com.netpilot.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class AiServiceImpl implements AiService {

    @Value("${ai.api-key}")
    private String apiKey;

    private final String baseUrl = "https://api.deepseek.com/v1";
    private final String model = "deepseek-chat";

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private DeviceMapper deviceMapper;

    @Override
    public String chat(String userMessage) {
        return callDeepSeek("You are a helpful assistant.", userMessage);
    }

    @Override
    public String chat(String systemPrompt, String userMessage) {
        return callDeepSeek(systemPrompt, userMessage);
    }

    @Override
    public String diagnoseDevice(Long deviceId) {
        // 1. 查数据库拿到设备的详细信息
        Device device = deviceMapper.selectById(deviceId);
        if (device == null) {
            return "未找到 ID 为 " + deviceId + " 的设备。";
        }

        // 2. 根据设备真实数据拼装 Prompt
        String onlineStatus = device.getOnline() != null && device.getOnline() ? "在线" : "离线";
        String prompt = String.format(
                "请帮我诊断以下网络设备：\n" +
                        "设备名称：%s\n" +
                        "IP 地址：%s\n" +
                        "设备类型：%s\n" +
                        "在线状态：%s\n" +
                        "SNMP 版本：%s\n" +
                        "SNMP 团体字：%s\n" +
                        "请根据以上信息，给出具体的故障诊断建议。",
                device.getName(),
                device.getIpAddress(),
                device.getDeviceType(),
                onlineStatus,
                device.getSnmpVersion(),
                device.getSnmpCommunity()
        );

        // 3. 调用 DeepSeek
        return callDeepSeek("你是一名资深网络工程师，擅长诊断网络设备故障。", prompt);
    }

    // ⭐ 给 AiController 里的流式接口用
    @Override
    public String getApiKey() {
        return apiKey;
    }

    private String callDeepSeek(String systemPrompt, String userMessage) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userMessage)
                    )
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(baseUrl + "/chat/completions", request, Map.class);

            if (response.getBody() == null || response.getBody().get("choices") == null) {
                return "AI 服务返回了空响应。";
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            Map<String, Object> firstChoice = choices.get(0);
            Map<String, Object> messageObj = (Map<String, Object>) firstChoice.get("message");
            return (String) messageObj.get("content");

        } catch (Exception e) {
            e.printStackTrace();
            return "AI服务调用失败: " + e.getMessage();
        }
    }
}