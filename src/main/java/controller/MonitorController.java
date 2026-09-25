package com.netpilot.controller;

import com.netpilot.entity.Device;
import com.netpilot.entity.MonitorHistory;
import com.netpilot.mapper.DeviceMapper;
import com.netpilot.service.MonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/monitor")
@RequiredArgsConstructor
public class MonitorController {

    private final MonitorService monitorService;
    private final DeviceMapper deviceMapper;

    /**
     * 获取设备最新监控数据
     */
    @GetMapping("/latest/{deviceId}")
    public MonitorHistory getLatest(@PathVariable Long deviceId) {
        return monitorService.getLatestMonitor(deviceId);
    }

    /**
     * 获取设备历史监控数据（默认 24 小时）
     */
    @GetMapping("/history/{deviceId}")
    public List<MonitorHistory> getHistory(@PathVariable Long deviceId,
                                           @RequestParam(defaultValue = "24") int hours) {
        return monitorService.getHistoryMonitor(deviceId, hours);
    }

    /**
     * 获取所有设备的最新监控数据（监控大屏用）
     */
    @GetMapping("/latest-all")
    public List<MonitorHistory> getLatestAll() {
        List<Device> devices = deviceMapper.selectList(null);
        List<MonitorHistory> result = new ArrayList<>();
        for (Device d : devices) {
            MonitorHistory latest = monitorService.getLatestMonitor(d.getId());
            if (latest != null) {
                result.add(latest);
            }
        }
        return result;
    }

    /**
     * 手动触发采集
     */
    @PostMapping("/collect")
    public String collect() {
        monitorService.collectAllDevices();
        return "采集任务已触发";
    }
}