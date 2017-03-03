package com.mindai.validate;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

/**
 * Created by CHEN on 16/4/29.
 */
public class RegisterValidator extends Validator
{
    @Override
    protected void validate(Controller c)
    {
        if (c.getRequest().getMethod().equalsIgnoreCase("POST"))
        {
            validateEmail("email", "msg", "邮件格式不正确");
            validateString("username", 3, 6, "msg", "xx请输入用户名");
            validateRequiredString("password", "msg", "请输入密码");
        }
    }
    
    @Override
    protected void handleError(Controller c)
    {
        c.renderJson();
    }
}