package com.song.reggie.dto;

import com.song.reggie.entity.Dish;
import com.song.reggie.entity.DishFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 *  因为Dish实体类不满足接收flavor参数，即需要导入DishDto，用于封装页面提交的数据
 *  DTO，全称为Data Transfer Object，即数据传输对象，一般用于展示层与服务层之间的数据传输。
 *
 */
@Data
public class DishDto extends Dish {
    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;

    /**
     *
     */
}