package com.song.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.song.reggie.common.CustomException;
import com.song.reggie.dto.DishDto;
import com.song.reggie.entity.Dish;
import com.song.reggie.entity.DishFlavor;
import com.song.reggie.mapper.DishMapper;
import com.song.reggie.service.DishFlavorService;
import com.song.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品，同时保存对应的口味数据
     * @param dishDto
     */
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //调用继承的save方法保存菜品的基本信息到菜品表dish
        this.save(dishDto);
        //菜品id：dishId，获取的是保存在dish表的id
        Long dishId = dishDto.getId();
        //菜品口味，将获取到的dishId赋值给dishFlavor的dishId属性
        List<DishFlavor> flavors = dishDto.getFlavors();//get方法
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息
        //先根据id查询到对应的dish对象
        Dish dish = this.getById(id);
        //创建一个dishDao对象
        DishDto dishDto = new DishDto();
        //拷贝对象
        BeanUtils.copyProperties(dish, dishDto);
        //条件构造器，对DishFlavor表查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();

        //查询菜品对应口味信息
        //根据dish_id来查询对应的菜品口味数据
        queryWrapper.eq(DishFlavor::getDishId, id);
        //获取查询的结果
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        //并将其赋给dishDto
        dishDto.setFlavors(flavors);
        //作为结果返回给前端
        return dishDto;
    }

    /**
     * 既需要更新dish菜品基本信息表，
     * 还需要更新dish_flavor菜品口味表。
     * 实现思路： 先删除，在添加
     * @param dishDto
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新当前菜品数据（dish表）
        this.updateById(dishDto);
        //下面是更新当前菜品的口味数据
        //条件构造器
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        //条件是当前菜品id
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        //将其删除掉
        dishFlavorService.remove(queryWrapper);
        //获取传入的新的口味数据
        List<DishFlavor> flavors = dishDto.getFlavors();
        //这些口味数据还是没有dish_id，所以需要赋予其dishId
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        //再重新加入到表中
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 套餐批量删除或单个删除
     * @param ids
     */
    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        //构造条件查询器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //先查询改菜品是否在售卖，如果是则抛出业务异常
        queryWrapper.in(ids != null,Dish::getId,ids);
        List<Dish> list = this.list(queryWrapper);
        for (Dish dish : list) {
            //Integer status = dish.getStatus();
            if (dish.getStatus() == 0){
                this.removeById(dish.getId());
            }else {
                //此时应该回滚，因为前的可能删除了，后面的正在售卖，进行回滚操作
                throw new CustomException("删除菜品中有正在售卖菜品,无法全部删除");
            }
        }

    }


}
