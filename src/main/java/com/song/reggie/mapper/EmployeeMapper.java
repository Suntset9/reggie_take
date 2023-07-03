package com.song.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.song.reggie.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

@Mapper //定义Mapper接口*
public interface EmployeeMapper extends BaseMapper<Employee> {
    //在MybatisPlus中, 自定义的Mapper接口, 需要继承自 BaseMapper。EmployeeMapper接口可以直接使用这些封装好的方法
}
