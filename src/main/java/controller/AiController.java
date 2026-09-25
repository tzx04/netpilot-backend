package com.netpilot.controller;

import com.netpilot.entity.Device;
import com.netpilot.entity.DiagnosisHistory;
import com.netpilot.mapper.DeviceMapper;
import com.netpilot.mapper.DiagnosisHistoryMapper;
import com.netpilot.service.AiService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final DeviceMapper deviceMapper;
    private final DiagnosisHistoryMapper diagnosisHistoryMapper;

    // ========== 1. 普通 AI 对话 ==========
    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String result = aiService.chat(message);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("content", result);
        return response;
    }

    // ========== 2. ⭐ 流式对话（打字机效果） ==========
    @PostMapping(value = "/chat/stream", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter chatStream(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        SseEmitter emitter = new SseEmitter(120_000L);

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL("https://api.deepseek.com/v1/chat/completions");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + aiService.getApiKey());
                conn.setDoOutput(true);

                String body = String.format(
                        "{\"model\":\"deepseek-chat\",\"stream\":true,\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}]}",
                        message.replace("\"", "\\\"").replace("\n", "\\n")
                );

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.getBytes(StandardCharsets.UTF_8));
                }

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6);
                            if ("[DONE]".equals(data)) {
                                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                                break;
                            }
                            int idx = data.indexOf("\"content\":\"");
                            if (idx >= 0) {
                                int start = idx + 11;
                                int end = data.indexOf("\"", start);
                                if (end > start) {
                                    String chunk = data.substring(start, end)
                                            .replace("\\n", "\n")
                                            .replace("\\\"", "\"");
                                    emitter.send(SseEmitter.event().name("message").data(chunk));
                                }
                            }
                        }
                    }
                }
                emitter.complete();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    emitter.send(SseEmitter.event().name("error").data(e.getMessage()));
                } catch (Exception ignored) {}
                emitter.completeWithError(e);
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();

        return emitter;
    }

    // ========== 3. ⭐ AI 设备诊断（自动存历史记录） ==========
    @GetMapping("/diagnose/{deviceId}")
    public Map<String, Object> diagnose(@PathVariable Long deviceId) {
        String result = aiService.diagnoseDevice(deviceId);

        // 存历史记录
        Device device = deviceMapper.selectById(deviceId);
        if (device != null) {
            DiagnosisHistory history = new DiagnosisHistory();
            history.setDeviceId(deviceId);
            history.setDeviceName(device.getName());
            history.setDiagnosis(result);
            history.setCreateTime(LocalDateTime.now());
            diagnosisHistoryMapper.insert(history);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("deviceId", deviceId);
        response.put("diagnosis", result);
        return response;
    }

    // ========== 4. ⭐ 获取某设备的诊断历史 ==========
    @GetMapping("/history/{deviceId}")
    public List<DiagnosisHistory> history(@PathVariable Long deviceId) {
        return diagnosisHistoryMapper.selectList(
                new LambdaQueryWrapper<DiagnosisHistory>()
                        .eq(DiagnosisHistory::getDeviceId, deviceId)
                        .orderByDesc(DiagnosisHistory::getCreateTime)
        );
    }

    // ========== 5. Ping 健康检查 ==========
    @GetMapping("/ping")
    public String ping() {
        return "AI 服务正常";
    }
}