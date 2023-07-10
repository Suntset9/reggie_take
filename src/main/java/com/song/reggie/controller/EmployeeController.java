package com.song.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.song.reggie.common.R;
import com.song.reggie.entity.Employee;
import com.song.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){// @RequestBody 自动将 JSON 数据转换为 Employee 对象
        //1、将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //  queryWrapper.eq(实体类::查询字段,条件值));
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        //3、如果没有查询到则返回登录失败结果
        if(emp == null){
            return R.error("登录失败");
        }

        //4、密码比对，如果不一致则返回登录失败结果
        if(!emp.getPassword().equals(password)){
            return R.error("登录失败");
        }

        //5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if(emp.getStatus() == 0){
            return R.error("账号已禁用");
        }

        //6、登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清理Session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("新增的员工信息：{}", employee.toString());
        //设置默认密码为123456，并采用MD5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        //设置createTime和updateTime 获取的是当前时间
        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());

        //根据session来获取创建人的id
        //Long empId = (Long) request.getSession().getAttribute("employee");

        //并设置当前登录用户的id，封装创建时间、修改时间，创建人、修改人信息(从session中获取当前登录用户)。
        //employee.setCreateUser(empId);
        //employee.setUpdateUser(empId);

        //存入数据库
        employeeService.save(employee);
        return R.success("添加员工成功");
    }

    /**
     * 员工信息分页查询
     * @param page 当前查询页码
     * @param pageSize 每页展示记录数
     * @param name 员工姓名 - 可选参数
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize, String name){
        //log.info("page = {}, pageSize = {}, name = {}",page,pageSize,name);
        //构造分页构造器
        Page<Employee> pageInfo = new Page<>(page,pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();

        //添加过滤条件,判断输入是否为空（当我们没有输入name时，就相当于查询所有了）
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName,name);

        //添加排序条件,按照更新时间进行降序排序
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService .page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 根据id修改员工信息
     * @param employee RequestBody 将参数转为json数据
     * @return
     */
    @PutMapping
    public R<String> updata(HttpServletRequest request,@RequestBody Employee employee){
        log.info(employee.toString());//日志打印id和status传值过来了，进行下一步

        //  状态修改已经在前面完成了，这里来编写更新时间和更新用户  通过之前存的session来获取当前user的id
        //获取会话对象的数据，并将数据转为long
        //Long empid = (Long) request.getSession().getAttribute("employee");
        //将更新的时间和更改人传入
        //employee.setUpdateTime(LocalDateTime.now());
        //employee.setUpdateUser(empid);

        //根据id把修改好的属性传入id
        employeeService.updateById(employee );
        //查看线程id
        long id = Thread.currentThread().getId();

        log.info("线程id为：{}",id);

        return R.success("员工信息修改成功");
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> geyById(@PathVariable Long id){//PathVariable,声明参数为路径变量
        log.info("根据员工id查询员工信息");
        Employee byId = employeeService.getById(id);
        if (id != null){
            return R.success(byId);
        }

        return R.error("没有查询到对应的员工信息");
    }

}
