package com.mindai.validate;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

/**
 * Created by CHEN on 16/4/29.
 */
public class LoginValidator extends Validator
{
    @Override
    protected void validate(Controller c)
    {
        // 校验输入的信息是否为空
        validateRequiredString("username", "msg", "请输入用户名");
        validateRequiredString("password", "msg", "请输入密码");
    }
    
    @Override
    protected void handleError(Controller c)
    {
        c.render("index.html");
    }
}