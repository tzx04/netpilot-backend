package com.netpilot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("net_device")
public class Device {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String ipAddress;

    private String deviceType;

    private String snmpCommunity;

    private String snmpVersion;

    private String sshUsername;

    private String sshPassword;

    private Integer status;

    private LocalDateTime lastPingTime;

    @TableField(exist = false)  // 不在数据库中，用于前端展示
    private Boolean online;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}