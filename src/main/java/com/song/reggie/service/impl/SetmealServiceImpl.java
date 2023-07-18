package com.song.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.song.reggie.common.CustomException;
import com.song.reggie.dto.DishDto;
import com.song.reggie.dto.SetmealDto;
import com.song.reggie.entity.Dish;
import com.song.reggie.entity.DishFlavor;
import com.song.reggie.entity.Setmeal;
import com.song.reggie.entity.SetmealDish;
import com.song.reggie.mapper.SetmealMapper;
import com.song.reggie.service.SetmealDishService;
import com.song.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    protected SetmealDishService setmealDishService;

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    @Transactional//操作两张表 开启事务，避免只删除了一半数据
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息，操作setmeal，执行insert操作
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //保存套餐和菜品的关联信息，操作setmeal_dish,执行insert操作
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 根据id查询套餐和对应的菜品信息
     * @param id
     * @return
     */
    @Override
    public SetmealDto getByIdWithDish(Long id) {
        //查询菜品基本信息
        //先根据id查询到对应的dish对象
        Setmeal byId = this.getById(id);
        //创建一个setmealDto对象
        SetmealDto setmealDto = new SetmealDto();
        //拷贝对象
        BeanUtils.copyProperties(byId, setmealDto);
        //条件构造器，对SetmealDish表查询
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();

        //查询菜品对应口味信息
        //根据dish_id来查询对应的菜品口味数据
        queryWrapper.eq(SetmealDish::getSetmealId, id);
        //获取查询的结果
        List<SetmealDish> flavors = setmealDishService.list(queryWrapper);
        //并将其赋给setmealDto
        setmealDto.setSetmealDishes(flavors);
        //作为结果返回给前端
        return setmealDto;
    }

    /**
     * 修改套餐数据
     * @param setmealDto
     */
    @Override
    @Transactional
    public void updateWithSetmeal(SetmealDto setmealDto) {
        //更新当前套餐数据（dish表）
        this.updateById(setmealDto);
        //下面是更新当前菜品的口味数据
        //条件构造器
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        //条件是当前套餐id
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        //将其删除掉
        setmealDishService.remove(queryWrapper);
        //获取传入的新的套餐数据
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //这些口味数据还是没有setmealId，所以需要赋予其setmealId
        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        //再重新加入到表中
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     * @param ids
     */
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        //select count(*) from setmeal where id in (1,2,3) and status = 1
        //查询套餐状态，确定是否可用删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);

        int count = this.count(queryWrapper);
        if(count > 0){
            //如果不能删除，抛出一个业务异常
            throw new CustomException("套餐正在售卖中，不能删除");
        }

        //如果可以删除，先删除套餐表中的数据---setmeal
        this.removeByIds(ids);

        //delete from setmeal_dish where setmeal_id in (1,2,3)
        //先查再删，因为当前id不是套餐关系表的主键值
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        //删除关系表中的数据----setmeal_dish
        setmealDishService.remove(lambdaQueryWrapper);
    }

}
