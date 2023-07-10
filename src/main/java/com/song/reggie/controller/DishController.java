package com.song.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.song.reggie.common.R;
import com.song.reggie.dto.DishDto;
import com.song.reggie.entity.Category;
import com.song.reggie.entity.Dish;
import com.song.reggie.entity.DishFlavor;
import com.song.reggie.entity.Employee;
import com.song.reggie.service.CategoryService;
import com.song.reggie.service.DishFlavorService;
import com.song.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Result;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * 菜品管理
 */

/**
 * tips：@RestController注解实际上是@Controller和@ResponseBody注解的结合，
 * 这意味着被注解的类将被识别为控制器，并且所有方法的返回值将直接作为HTTP响应的内容返回，而无需额外的转换或包装。
 * @RestController 还可以配合其他注解（例如@RequestMapping）一起使用，以更精确地定义URL路径、请求方法和其他请求属性。这样，你就可以方便地创建一个基于RESTful风格的API，并且能够轻松处理不同类型的HTTP请求（如GET、POST、PUT、DELETE等）。
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    public DishService dishService;

    @Autowired
    public DishFlavorService dishFlavorService;


    @Autowired
    public CategoryService categoryService;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        return R.success("新增菜品成功");
    }

    /**
     * 菜品信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //构造分页构造器对象
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        //这个就是我们到时候返回的结果
        Page<DishDto> dishDtoPage = new Page<>(page, pageSize);
        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.like(name != null, Dish::getName, name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //执行分页查询
        dishService.page(pageInfo, queryWrapper);

        //对象拷贝，这里只需要拷贝一下查询到的条目数
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");

        //获取原records数据
        List<Dish> records = pageInfo.getRecords();

        //遍历每一条records数据
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            //将数据赋给dishDto对象
            BeanUtils.copyProperties(item, dishDto);
            //然后获取一下dish对象的category_id属性
            Long categoryId = item.getCategoryId();  //分类id
            //根据这个属性，获取到Category对象（这里需要用@Autowired注入一个CategoryService对象）
            Category category = categoryService.getById(categoryId);
            //随后获取Category对象的name属性，也就是菜品分类名称
            String categoryName = category.getName();
            //最后将菜品分类名称赋给dishDto对象就好了
            dishDto.setCategoryName(categoryName);
            //结果返回一个dishDto对象
            return dishDto;
            //并将dishDto对象封装成一个集合，作为我们的最终结果
        }).collect(Collectors.toList());

        //将转换后的DishDto列表设置到dishDtoPage对象的records属性中，然后将dishDtoPage作为成功响应的数据返回。
        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")    //Result风格
    public R<DishDto> getByIdWithFlaovor(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        log.info("查询到的数据为：{}",dishDto);

        return R.success(dishDto);

    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);
        return R.success("修改菜品成功");
    }


    /**
     * 对菜品批量或者是单个 进行停售或者是起售
     * 0?ids=1678085611082313730 发送了id和状态，使用两个参数接收
     * @return
     */
    @PostMapping("/status/{status}")
    //这个参数这里一定记得加注解才能获取到参数，否则这里非常容易出问题
    public R<String> status(@PathVariable("status") Integer status, @RequestParam List<Long> ids) {
        //log.info("status:{}",status);
        //log.info("ids:{}",ids);
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //查询条件是根据Dish实体类的id字段，匹配ids列表中的值。如果ids列表为null，则不添加该查询条件。
        queryWrapper.in(ids != null, Dish::getId, ids);
        //根据数据进行批量查询
        List<Dish> list = dishService.list(queryWrapper);
        //循环遍历
        for (Dish dish : list) {

            if (dish != null) {
                //判断不为空，则根据前端三元运算符，传回来的值写入数据库
                dish.setStatus(status);
                dishService.updateById(dish);
            }
        }
        return R.success("售卖状态修改成功");
    }

    /**
     * 需删除两张表
     * 套餐批量删除或单个删除
     * 前端为：dish?ids=1413384757047271425
     * 请求方法:DELETE
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam("ids") List<Long> ids){
        log.info("ids{}",ids);
        //对菜品进行逻辑删除
        dishService.deleteByIds(ids);
        //删除菜品对应口味，也是逻辑删除
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DishFlavor::getDishId,ids);
        dishFlavorService.remove(queryWrapper);
        return R.success("菜品删除成功");
    }



}
