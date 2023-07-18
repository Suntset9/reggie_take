package com.song.reggie.controller;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.song.reggie.common.R;
import com.song.reggie.dto.DishDto;
import com.song.reggie.dto.SetmealDto;
import com.song.reggie.entity.Category;
import com.song.reggie.entity.Dish;
import com.song.reggie.entity.Setmeal;
import com.song.reggie.entity.SetmealDish;
import com.song.reggie.service.CategoryService;
import com.song.reggie.service.DishService;
import com.song.reggie.service.SetmealDishService;
import com.song.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


/**
 * 套餐管理
 */
@RestController//将一个类标记为处理HTTP请求的控制器，并自动将返回的数据转换为适合HTTP响应的格式。
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;//套餐

    @Autowired
    private CategoryService categoryService;////套餐分类

    @Autowired
    private SetmealDishService setmealDishService;//套餐菜品关系

    @Autowired
    private DishService dishService;

    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息：{}",setmealDto);

        setmealService.saveWithDish(setmealDto);

        return R.success("新增套餐成功");
    }

    /**
     * 套餐信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     * 主要了解数据表中的关系
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //构造分页构造器对象
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        //这个就是我们到时候返回的结果
        Page<SetmealDto> setmealDtoPage = new Page<>();
        //条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.like(name != null, Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //执行分页查询
        setmealService.page(pageInfo,queryWrapper);

        //对象拷贝，这里只需要拷贝一下查询到的条目数
        BeanUtils.copyProperties(pageInfo, setmealDtoPage, "records");

        //获取原records数据
        List<Setmeal> records = pageInfo.getRecords();

        //遍历每一条records数据
        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            //将数据赋给dishDto对象
            BeanUtils.copyProperties(item, setmealDto);
            //然后获取一下dish对象的category_id属性
            Long categoryId = item.getCategoryId();  //分类id
            //根据这个属性，获取到Category对象（这里需要用@Autowired注入一个CategoryService对象）
            Category category = categoryService.getById(categoryId);
            if (category != null){
                //随后获取Category对象的name属性，也就是套餐分类名称
                String categoryName = category.getName();
                //最后将套餐分类名称赋给dishDto对象就好了
                setmealDto.setCategoryName(categoryName);
            }
            //结果返回一个dishDto对象
            return setmealDto;
            //并将dishDto对象封装成一个集合，作为我们的最终结果
        }).collect(Collectors.toList());

        //将转换后的DishDto列表设置到dishDtoPage对象的records属性中，然后将dishDtoPage作为成功响应的数据返回。
        setmealDtoPage.setRecords(list);

        return R.success(setmealDtoPage);
    }


    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids{}",ids);

        setmealService.removeWithDish(ids);

        return R.success("删除套餐成功");
    }



    /**
     * 根据Id查询套餐信息，修改套餐操作把数据回显前端
     * 在套餐管理列表页面点击修改按钮，跳转到修改套餐页面，在修改页面回显套餐相关信息并进行修改，最后点击保存按钮完成修改操作
     */
    @GetMapping("/{id}")
    public R<SetmealDto> getByIdWithSetmeal(@PathVariable Long id){
        SetmealDto byIdWithDish = setmealService.getByIdWithDish(id);
        return R.success(byIdWithDish);
    }



    /**
     * 对套餐批量或者是单个 进行停售或者是起售
     * 0?ids=1678085611082313730 发送了id和状态，使用两个参数接收
     * @return
     */
    @PostMapping("/status/{status}")
    //这个参数这里一定记得加注解才能获取到参数，否则这里非常容易出问题
    public R<String> status(@PathVariable("status") Integer status, @RequestParam List<Long> ids) {
        //log.info("status:{}",status);
        //log.info("ids:{}",ids);
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //查询条件是根据Dish实体类的id字段，匹配ids列表中的值。如果ids列表为null，则不添加该查询条件。
        queryWrapper.in(ids != null, Setmeal::getId, ids);
        //根据数据进行批量查询
        List<Setmeal> list = setmealService.list(queryWrapper);
        //循环遍历
        for (Setmeal setmeal : list) {

            if (setmeal != null) {
                //判断不为空，则根据前端三元运算符，传回来的值写入数据库
                setmeal.setStatus(status);
                setmealService.updateById(setmeal);
            }
        }
        return R.success("套餐状态修改成功");
    }



    /**
     * 修改套餐代码
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        log.info(setmealDto.toString());
        setmealService.updateWithSetmeal(setmealDto);
        return R.success("修改套餐成功");
    }

    /**
     * 根据条件查询套餐数据
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list (Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 点击图片查看套餐详情功能
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    public R<List<DishDto>> showSetmealDish(@PathVariable Long id) {
        //条件构造器
        LambdaQueryWrapper<SetmealDish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //手里的数据只有setmealId
        dishLambdaQueryWrapper.eq(SetmealDish::getSetmealId, id);
        //查询数据
        List<SetmealDish> records = setmealDishService.list(dishLambdaQueryWrapper);
        List<DishDto> dtoList = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            //copy数据
            BeanUtils.copyProperties(item,dishDto);
            //查询对应菜品id
            Long dishId = item.getDishId();
            //根据菜品id获取具体菜品数据，这里要自动装配 dishService
            Dish dish = dishService.getById(dishId);
            //其实主要数据是要那个图片，不过我们这里多copy一点也没事
            BeanUtils.copyProperties(dish,dishDto);
            return dishDto;
        }).collect(Collectors.toList());
        return R.success(dtoList);
    }
}
