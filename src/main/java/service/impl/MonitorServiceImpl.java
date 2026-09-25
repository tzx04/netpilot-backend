package com.netpilot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.netpilot.entity.Device;
import com.netpilot.entity.MonitorHistory;
import com.netpilot.mapper.DeviceMapper;
import com.netpilot.mapper.MonitorHistoryMapper;
import com.netpilot.service.MonitorService;
import com.netpilot.util.SnmpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorServiceImpl implements MonitorService {

    private final DeviceMapper deviceMapper;
    private final MonitorHistoryMapper monitorHistoryMapper;

    @Override
    public MonitorHistory collectDeviceData(Device device) {
        String ip = device.getIpAddress();
        String community = device.getSnmpCommunity();

        // 如果设备没有配置 SNMP 团体名，跳过
        if (community == null || community.isEmpty()) {
            log.warn("设备 {} 未配置 SNMP，跳过采集", device.getName());
            return null;
        }

        try {
            MonitorHistory history = new MonitorHistory();
            history.setDeviceId(device.getId());

            // 采集 CPU
            Double cpu = SnmpUtil.getCpuUsage(ip, community);
            if (cpu != null) {
                history.setCpuUsage(BigDecimal.valueOf(cpu));
            }

            // 采集内存
            Double memory = SnmpUtil.getMemoryUsage(ip, community);
            if (memory != null) {
                history.setMemoryUsage(BigDecimal.valueOf(memory));
            }

            // 采集流量（暂时用模拟数据，后续可扩展）
            // 真实场景需要用接口 OID 遍历所有接口汇总

            history.setCollectTime(LocalDateTime.now());
            return history;
        } catch (Exception e) {
            log.error("采集设备 {} ({}) 数据失败: {}", device.getName(), ip, e.getMessage());
            return null;
        }
    }

    @Override
    @Scheduled(fixedDelay = 60000)  // 每 60 秒执行一次
    public void collectAllDevices() {
        log.info("开始定时采集 SNMP 数据...");

        // 1. 查询所有启用的设备
        List<Device> devices = deviceMapper.selectList(null);
        if (devices.isEmpty()) {
            log.info("没有设备需要采集");
            return;
        }

        int successCount = 0;
        for (Device device : devices) {
            // 只采集启用的、配置了 SNMP 的设备
            if (device.getStatus() == 1 && device.getSnmpCommunity() != null) {
                MonitorHistory data = collectDeviceData(device);
                if (data != null) {
                    monitorHistoryMapper.insert(data);
                    successCount++;
                }
            }
        }

        log.info("定时采集 SNMP 完成: 成功采集 {} 台设备", successCount);
    }

    @Override
    public MonitorHistory getLatestMonitor(Long deviceId) {
        LambdaQueryWrapper<MonitorHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MonitorHistory::getDeviceId, deviceId)
                .orderByDesc(MonitorHistory::getCollectTime)
                .last("LIMIT 1");
        return monitorHistoryMapper.selectOne(wrapper);
    }

    @Override
    public List<MonitorHistory> getHistoryMonitor(Long deviceId, int hours) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
        LambdaQueryWrapper<MonitorHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MonitorHistory::getDeviceId, deviceId)
                .ge(MonitorHistory::getCollectTime, startTime)
                .orderByAsc(MonitorHistory::getCollectTime);
        return monitorHistoryMapper.selectList(wrapper);
    }
}