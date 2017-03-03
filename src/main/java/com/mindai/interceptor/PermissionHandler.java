package com.mindai.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jfinal.handler.Handler;
import com.jfinal.kit.HandlerKit;
import com.mindai.common.Constants;

public class PermissionHandler extends Handler
{

    @Override
    public void handle(String target, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled)
    {
        Object login = request.getSession().getAttribute(Constants.ISLOGIN);
        
        // 未登录跳转
        if ((null != login && Boolean.parseBoolean(login.toString())) || target.equals("/login") || target.equals("/index")
            || target.equals("/index.html") || target.endsWith(".css") || target.endsWith(".js") || target.endsWith(".jpg")
            || target.contains("/font"))
        {
            next.handle(target, request, response, isHandled);
        }
        else
        {
            HandlerKit.redirect(request.getContextPath()+"/index.html", request, response, isHandled);
        }
    }
    
}
