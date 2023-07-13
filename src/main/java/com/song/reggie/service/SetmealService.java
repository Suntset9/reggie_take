package com.song.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.song.reggie.dto.SetmealDto;
import com.song.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);

    //根据id查询套餐和对应的菜品信息，修改套餐操作
    public SetmealDto getByIdWithDish(Long id);

    //修改套餐功能
    public void updateWithSetmeal(SetmealDto setmealDto);

    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     * @param ids
     */
    public void removeWithDish(List<Long> ids);

}
