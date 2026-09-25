package com.netpilot.service;

import com.netpilot.entity.Device;
import com.netpilot.entity.MonitorHistory;

import java.util.List;

public interface MonitorService {

    /**
     * 采集单个设备的监控数据
     */
    MonitorHistory collectDeviceData(Device device);

    /**
     * 采集所有在线设备的监控数据
     */
    void collectAllDevices();

    /**
     * 获取设备最新监控数据
     */
    MonitorHistory getLatestMonitor(Long deviceId);

    /**
     * 获取设备历史监控数据（最近 24 小时）
     */
    List<MonitorHistory> getHistoryMonitor(Long deviceId, int hours);
}