package com.netpilot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("monitor_history")
public class MonitorHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long deviceId;

    private BigDecimal cpuUsage;

    private BigDecimal memoryUsage;

    private Long bandwidthIn;

    private Long bandwidthOut;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime collectTime;
}