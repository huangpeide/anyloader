package com.mindai.controller;



import org.apache.log4j.Logger;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.PropKit;
import com.mindai.common.Constants;
import com.mindai.validate.LoginValidator;



/**
 * 本 demo 仅表达最为粗浅的 jfinal 用法，更为有价值的实用的企业级用法 详见 JFinal 俱乐部: http://jfinal.com/club
 * 
 * IndexController
 */
public class IndexController extends Controller
{
    private final Logger log = Logger.getLogger(this.getClass());
    
    public void index()
    {
        render("index.html");
    }
    
    public void exit() 
    {
        getSession().removeAttribute(Constants.ISLOGIN);
        getSession().removeAttribute(Constants.USERNAME);
        redirect("/index.html");
    }
    
    @Before({LoginValidator.class})
    public void login()
    {
        String cu = PropKit.get("login.username");
        String cp = PropKit.get("login.password");
        String username=getPara("username");
        String password=getPara("password");
        
        if(cu.equals(username)&&cp.equals(password))
        {
            getSession().setAttribute(Constants.ISLOGIN, true);
            getSession().setAttribute(Constants.USERNAME, cu);
            setCookie(Constants.USERNAME, username, 86400);//1天免登陆
            redirect("/manage.html");
        }
        else
        {
            log.info("登陆失败!");
            redirect("/index.html");
        }
    }
}
