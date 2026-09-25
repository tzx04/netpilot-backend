package com.netpilot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("diagnosis_history")
public class DiagnosisHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long deviceId;
    private String deviceName;
    private String diagnosis;
    private LocalDateTime createTime;
}