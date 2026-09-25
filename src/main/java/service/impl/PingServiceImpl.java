package com.netpilot.service.impl;

import com.netpilot.entity.Device;
import com.netpilot.mapper.DeviceMapper;
import com.netpilot.service.PingService;
import com.netpilot.util.IcmpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PingServiceImpl implements PingService {

    private final DeviceMapper deviceMapper;

    // 创建线程池，用于并发 Ping
    private static final ExecutorService PING_EXECUTOR =
            Executors.newFixedThreadPool(20);

    @Override
    public boolean pingDevice(Device device) {
        return IcmpUtil.ping(device.getIpAddress());
    }

    @Override
    public Map<Long, Boolean> pingAllDevices(List<Device> devices) {
        Map<Long, Boolean> results = new HashMap<>();

        // 使用 CompletableFuture 并发执行 Ping
        List<CompletableFuture<Map.Entry<Long, Boolean>>> futures = devices.stream()
                .map(device -> CompletableFuture.supplyAsync(() -> {
                    boolean alive = IcmpUtil.ping(device.getIpAddress());
                    return Map.entry(device.getId(), alive);
                }, PING_EXECUTOR))
                .collect(Collectors.toList());

        // 等待所有任务完成（最多 5 秒）
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .join();

        // 收集结果
        for (CompletableFuture<Map.Entry<Long, Boolean>> future : futures) {
            try {
                Map.Entry<Long, Boolean> entry = future.get(5, TimeUnit.SECONDS);
                results.put(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                // 超时或异常，标记为离线
                log.warn("Ping 任务执行异常: {}", e.getMessage());
            }
        }

        return results;
    }

    @Override
    @Scheduled(fixedDelay = 60000)  // 每 60 秒执行一次
    public void scheduledPing() {
        log.info("开始定时 Ping 检测...");

        // 1. 查询所有启用的设备
        List<Device> devices = deviceMapper.selectList(null);
        if (devices.isEmpty()) {
            log.info("没有设备需要 Ping");
            return;
        }

        // 2. 并发 Ping 所有设备
        Map<Long, Boolean> results = pingAllDevices(devices);

        // 3. 更新数据库中的 last_ping_time
        int onlineCount = 0;
        for (Device device : devices) {
            Boolean alive = results.get(device.getId());
            if (alive != null && alive) {
                // 在线：更新最后 Ping 时间
                device.setLastPingTime(LocalDateTime.now());
                deviceMapper.updateById(device);
                onlineCount++;
            }
        }

        log.info("定时 Ping 完成: 总设备={}, 在线={}, 离线={}",
                devices.size(), onlineCount, devices.size() - onlineCount);
    }
}