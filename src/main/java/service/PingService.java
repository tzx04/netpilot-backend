package com.netpilot.service;

import com.netpilot.entity.Device;

import java.util.List;
import java.util.Map;

public interface PingService {

    /**
     * Ping 单个设备
     */
    boolean pingDevice(Device device);

    /**
     * Ping 所有设备（并发）
     */
    Map<Long, Boolean> pingAllDevices(List<Device> devices);

    /**
     * 定时任务：自动 Ping 所有设备并更新状态
     */
    void scheduledPing();
}