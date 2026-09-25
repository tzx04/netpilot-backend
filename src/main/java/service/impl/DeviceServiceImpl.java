package com.netpilot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.netpilot.dto.DeviceRequest;
import com.netpilot.entity.Device;
import com.netpilot.mapper.DeviceMapper;
import com.netpilot.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceMapper deviceMapper;

    @Override
    public List<Device> listAllDevices() {
        return deviceMapper.selectList(null);
    }

    @Override
    public Device getDeviceById(Long id) {
        return deviceMapper.selectById(id);
    }

    @Override
    public boolean addDevice(DeviceRequest request) {
        // 检查 IP 是否已存在
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Device::getIpAddress, request.getIpAddress());
        if (deviceMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("设备IP已存在: " + request.getIpAddress());
        }

        Device device = new Device();
        BeanUtils.copyProperties(request, device);
        return deviceMapper.insert(device) > 0;
    }

    @Override
    public boolean updateDevice(Long id, DeviceRequest request) {
        Device existing = deviceMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("设备不存在");
        }

        // 如果修改了 IP，检查新 IP 是否被其他设备占用
        if (!existing.getIpAddress().equals(request.getIpAddress())) {
            LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Device::getIpAddress, request.getIpAddress());
            wrapper.ne(Device::getId, id);
            if (deviceMapper.selectCount(wrapper) > 0) {
                throw new RuntimeException("设备IP已存在: " + request.getIpAddress());
            }
        }

        BeanUtils.copyProperties(request, existing);
        return deviceMapper.updateById(existing) > 0;
    }

    @Override
    public boolean deleteDevice(Long id) {
        return deviceMapper.deleteById(id) > 0;
    }
}