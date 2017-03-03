package com.mindai.controller;

import java.util.Map;

import com.jfinal.core.Controller;
import com.mindai.model.ExecuteList;
import com.mindai.service.ExecuteListService;



/**
 * 本 demo 仅表达最为粗浅的 jfinal 用法，更为有价值的实用的企业级用法 详见 JFinal 俱乐部: http://jfinal.com/club
 * 
 * IndexController
 */
public class ExecuteListController extends Controller
{
    private static ExecuteListService service = new ExecuteListService();
    
    public void list()
    {
        int limit=getParaToInt("limit");
        int offset=getParaToInt("offset");
        String search = getPara("search","%");
        Map<String, Object> data = service.getList(offset, limit,search);
        renderJson(data);
    }
    
    
    public void getById()
    {
        String id = getPara("id");
        ExecuteList data = ExecuteList.dao.findById(id);
        renderJson(data);
    }
    
    
    /*private void renderMsg(boolean status,String msg)
    {
        Map<String, Object> datas = new HashMap<String, Object>(2);
        datas.put("status", status?"success":"fail");
        datas.put("message", msg);
        renderJson(datas);
    }*/
    
    
}
