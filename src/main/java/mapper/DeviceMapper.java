package com.netpilot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.netpilot.entity.Device;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeviceMapper extends BaseMapper<Device> {
}