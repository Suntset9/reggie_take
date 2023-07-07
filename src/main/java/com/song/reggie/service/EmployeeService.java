package com.song.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.song.reggie.entity.Employee;

/*
EmployeeService 接口继承 IService<Employee>，可以自定义业务逻辑的方法，并可以继承和扩展父接口中的方法。
* */
//项目的Service接口, 在定义时需要继承自MybatisPlus提供的Service层接口 IService, 这样就可以直接调用 父接口的方法直接执行业务操作, 简化业务层代码实现。
public interface EmployeeService extends IService<Employee> {
}
