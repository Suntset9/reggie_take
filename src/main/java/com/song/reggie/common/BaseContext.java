package com.song.reggie.common;

/**
 * 基于Threadlocal封装工具类，用户保存获取登录用户id
 * 以线程为作用域，只在线程中有效
 */
public class BaseContext {

    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();
    /**
     * 设置值
     * @param id
     */
    public static void setThreadLocal(Long id){
        threadLocal.set(id);
    }

    /**
     * 获取值
     * @return
     */
    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
