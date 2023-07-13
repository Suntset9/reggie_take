package com.song.reggie.dto;

import com.song.reggie.entity.Setmeal;
import com.song.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

/**
 * 套
 */
@Data
public class SetmealDto extends Setmeal {
    private List<SetmealDish> setmealDishes; //套餐关联菜品列表
    private String categoryName;//套餐分类名称
}