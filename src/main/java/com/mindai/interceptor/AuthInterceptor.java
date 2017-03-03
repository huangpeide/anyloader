package com.mindai.interceptor;


import org.apache.log4j.Logger;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.mindai.common.Constants;

/**
 * 认证拦截器
 * 
 * @author hpd
 */
public class AuthInterceptor implements Interceptor
{
    private final Logger authlogger = Logger.getLogger(this.getClass());
    
    @Override
    public void intercept(Invocation inv)
    {
        authlogger.info("Before method invoking");
        Object login = inv.getController().getSession().getAttribute(Constants.ISLOGIN);
        authlogger.info("invoking:" + inv.getControllerKey() + "--" + inv.getMethodName() + "--" + inv.getViewPath());
        
        // 未登录跳转
        if (inv.getMethodName().equals("login") || inv.getMethodName().equals("exit") ||inv.getMethodName().equals("index") 
            || (null != login && Boolean.parseBoolean(login.toString())))
        {
            // //传递本次调用，调用剩下的拦截器与目标方法
            inv.invoke();
        }
        else
        {
            inv.getController().redirect("/login");;
        }
        authlogger.info("After method invoking");
    }
    
}
