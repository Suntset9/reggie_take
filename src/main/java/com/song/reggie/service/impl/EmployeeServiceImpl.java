package com.song.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.song.reggie.entity.Employee;
import com.song.reggie.mapper.EmployeeMapper;
import com.song.reggie.service.EmployeeService;
import org.springframework.stereotype.Service;

/**
 * EmployeeServiceImpl 类继承 ServiceImpl<EmployeeMapper, Employee>，可以直接调用父类中的方法对数据库进行操作
 */
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

}
