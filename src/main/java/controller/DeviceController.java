package com.netpilot.controller;

import com.netpilot.dto.DeviceRequest;
import com.netpilot.entity.Device;
import com.netpilot.service.DeviceService;
import com.netpilot.service.PingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;
    private final PingService pingService;  // 注入 PingService

    @GetMapping
    public List<Device> listDevices() {
        List<Device> devices = deviceService.listAllDevices();
        // 为每个设备查询在线状态
        for (Device device : devices) {
            if (device.getStatus() == 1) {  // 只检测启用的设备
                boolean online = pingService.pingDevice(device);
                device.setOnline(online);
            } else {
                device.setOnline(false);
            }
        }
        return devices;
    }

    @GetMapping("/{id}")
    public Device getDevice(@PathVariable Long id) {
        Device device = deviceService.getDeviceById(id);
        if (device != null && device.getStatus() == 1) {
            boolean online = pingService.pingDevice(device);
            device.setOnline(online);
        }
        return device;
    }

    @PostMapping
    public Map<String, Object> addDevice(@RequestBody DeviceRequest request) {
        deviceService.addDevice(request);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "设备添加成功");
        return result;
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateDevice(@PathVariable Long id, @RequestBody DeviceRequest request) {
        deviceService.updateDevice(id, request);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "设备更新成功");
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "设备删除成功");
        return result;
    }
}