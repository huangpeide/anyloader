package com.mindai.controller;

import java.util.HashMap;
import java.util.Map;

import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.mindai.model.DataSource;
import com.mindai.service.DataSourceService;



/**
 * 本 demo 仅表达最为粗浅的 jfinal 用法，更为有价值的实用的企业级用法 详见 JFinal 俱乐部: http://jfinal.com/club
 * 
 * IndexController
 */
public class DataSourceController extends Controller
{
    private static DataSourceService service = new DataSourceService();
    
    public void list()
    {
        int limit=getParaToInt("limit");
        int offset=getParaToInt("offset");
        String search = getPara("search");
        Map<String, Object> data = service.getList(offset, limit,search);
        renderJson(data);
    }
    
    public void getDataSourceById()
    {
        String id = getPara("id");
        renderJson(DataSource.dao.findById(id));
    }
    
    
    public void getAll()
    {
        Map<String, Object> data = service.getAll();
        renderJson(data);
    }

    
    private void renderMsg(boolean status,String msg)
    {
        Map<String, Object> datas = new HashMap<String, Object>(2);
        datas.put("status", status?"success":"fail");
        datas.put("message", msg);
        renderJson(datas);
    }
    
    public void delete()
    {
        String id = getPara("id");
        DataSource.dao.deleteById(id);
        renderMsg(true,"success");
    }
    
    public void save()
    {
        String dbsourceId = getPara("dbsourceId");
        String sourceName = getPara("sourceName");
        String ip = getPara("ip");
        String port = getPara("port");
        String userName = getPara("userName");
        String password = getPara("password");
        String remarks = getPara("remarks");
        String dbType = getPara("dbType");
   
        if (StrKit.isBlank(sourceName))
        {
            renderMsg(false,"数据源名称不能为空！");
            return;
        }
        if (StrKit.isBlank(ip))
        {
            renderMsg(false,"IP不能为空！");
            return;
        }
        
        if (StrKit.isBlank(port))
        {
            renderMsg(false,"端口不能为空！");
            return;
        }
        else
        {
            try
            {
                Integer.valueOf(port);
            }
            catch (Exception e)
            {
                renderMsg(false,"端口必须为数字！");
                return;
            }
        }
        
        if (StrKit.isBlank(dbType))
        {
            renderMsg(false,"数据源类型不能为空！");
            return;
        }
        
        DataSource ds = new DataSource();
        ds.setSourceName(sourceName);
        ds.setIp(ip);
        ds.setPort(Integer.valueOf(port));
        ds.setUserName(userName);
        ds.setPassword(password);
        ds.setDbType(Integer.valueOf(dbType));
        ds.setRemarks(remarks);
        
        if ("".equals(dbsourceId))
        {
            //检查表名是否存在
            boolean flag = service.dbExists(sourceName);
            if (flag)
            {
                renderMsg(false,"数据源名称已经存在！");
                return;
            }
            //新增加
            ds.save();
        }
        else
        {
            ds.setId(Long.valueOf(dbsourceId));
            ds.update();
        }
        
        renderMsg(true,"success");
    }
}
