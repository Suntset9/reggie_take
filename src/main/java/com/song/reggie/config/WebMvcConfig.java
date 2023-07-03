package com.song.reggie.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

//@Slf4j
//@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {
    /**
     * 如果直接放在resources目录下，则需要配置一下资源映射
     * 放在静态资源，直接注释
     * @param registry
     */
    //@Override
    //protected void addResourceHandlers(ResourceHandlerRegistry registry) {
    //    log.info("开始进行静态资源映射...");
    //    registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
    //    registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
    //}873

}
