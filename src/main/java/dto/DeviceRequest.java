package com.netpilot.dto;

import lombok.Data;

@Data
public class DeviceRequest {
    private String name;
    private String ipAddress;
    private String deviceType;
    private String snmpCommunity;
    private String snmpVersion;
    private String sshUsername;
    private String sshPassword;
    private Integer status;
}