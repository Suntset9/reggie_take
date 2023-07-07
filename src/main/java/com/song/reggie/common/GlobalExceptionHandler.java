package com.song.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

//全局异常捕获
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error(ex.getMessage());
        //如果包含Duplicate entry，则说明有条目重复
        if (ex.getMessage().contains("Duplicate entry")) {
            //对字符串切片
            String[] split = ex.getMessage().split(" ");
            //字符串格式是固定的，所以这个位置必然是username
            String username = split[2];
            //拼串作为错误信息返回
            return R.error("用户名" + username + "已存在");
        }
        //如果是别的错误那我也没招儿了
        return R.error("未知错误");
    }


}
