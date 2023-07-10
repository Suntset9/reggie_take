package com.song.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.song.reggie.common.BaseContext;
import com.song.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import java.io.IOException;

/**
 * 检查用户是或否完成登录
 */
//
@Slf4j
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*") //filterName:指定过滤器的名称。在同一个应用程序中，过滤器的名称必须是唯一的。按照自己命名喜好
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        //将请求和响应强转为HTTP的请求和响应对象，从而能够使用更多与HTTP相关的方法和功能。例如获取请求头、获取请求参数、设置响应状态码等。
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;


        //将拦截到的URI输出到日志，{}是占位符，将自动填充request.getRequestURI()的内容
        log.info("拦截到的URI：{}",httpServletRequest.getRequestURI());
        //chain.doFilter(httpServletRequest, httpServletResponse);

        // 1.获取本次请求的URI
        String uri = httpServletRequest.getRequestURI();

        //定义不需要被拦截的请求
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**"

        };

        /**
         * 2.判断本次请求是否需要处理 ，使用Spring 概念模型 : PathMatcher 路径匹配器
         * urls：不需要拦截
         * uri: 获取的请求
         */
        boolean check = check(urls, uri);
        // 3.如果不需要处理，则直接放行
        if (check){
            //log.info("本次请求：{}，不需要处理",uri);
            chain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }

        //4、判断登录状态，如果已登录，则直接放行
        //当初存的session是employee，所以这里就拿它判断
        if (httpServletRequest.getSession().getAttribute("employee") != null) {
            log.info("用户已登录，id为:{}",httpServletRequest.getSession().getAttribute("employee"));
            //判断线程id是否相同
            long id = Thread.currentThread().getId();
            log.info("线程id为：{}",id);

            //将session中的id传递给Threadlocal，
            Long empId = (Long) httpServletRequest.getSession().getAttribute("employee");
            //使用BaseContext封装id
            BaseContext.setThreadLocal(empId);

            chain.doFilter(httpServletRequest,httpServletResponse);
            return;
        }

        //5、如果未登录则返回未登录结果，通过输出流方式向客户端页面响应数据
        // 因为前端页面判断if (res.data.code === 0 && res.data.msg === 'NOTLOGIN') {// 返回登录页面
        //所以我们这里未登录返回NOTLOGIN
        log.info("用户未登录");
        log.info("用户id:{}",httpServletRequest.getSession().getAttribute("employee"));
        httpServletResponse.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));


    }

    /**
     * 2.判断本次请求是否需要处理 ，使用Spring 概念模型 : PathMatcher 路径匹配器
     * 路径匹配，检查本次请求是否需要放行
     * @param urls
     * @param uri
     * @return
     */
    public boolean check(String[] urls, String uri){
        for (String url : urls) {
            boolean math = PATH_MATCHER.match(url,uri);
            if (math){
                //匹配
                return true;
            }
        }
        //不匹配
        return false;
    }

    /**
     * 登出功能
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }
}
