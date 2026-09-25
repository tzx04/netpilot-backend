package com.netpilot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.netpilot.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper  // ⚠️ 确保有这个注解
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    User selectByUsername(String username);
}