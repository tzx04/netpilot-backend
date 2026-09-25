package com.netpilot.service;

import com.netpilot.dto.DeviceRequest;
import com.netpilot.entity.Device;

import java.util.List;

public interface DeviceService {

    List<Device> listAllDevices();

    Device getDeviceById(Long id);

    boolean addDevice(DeviceRequest request);

    boolean updateDevice(Long id, DeviceRequest request);

    boolean deleteDevice(Long id);
}