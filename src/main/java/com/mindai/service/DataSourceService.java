package com.mindai.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class DataSourceService
{
  
    
    public Map<String, Object> getList(int offset, int limit,String search)
    {
        int total = 0;
        if (null == search || "".equals(search.trim()))
        {
            total = Db.queryLong("select count(id) from data_source").intValue();
        }
        else
        {
            total = Db.queryLong("select count(id) from data_source where sourceName like ?",search).intValue();
        }
        if (total == 0)
        {
            return null;
        }
        else
        {
            List<Record> records = null;
            if (null == search || "".equals(search.trim()))
            {
                records = Db.find("select * from data_source order by id desc limit ?,?", offset,limit);
            }
            else
            {
                records = Db.find("select * from data_source where sourceName like ? order by id desc limit ?,?", search,offset, limit);
            }
            Map<String, Object> datas = new HashMap<String, Object>(2);
            datas.put("rows", records);
            datas.put("total", total);
            return datas;
        }
    }
    
    
    public Map<String, Object> getAll()
    {
        Map<String, Object> datas = new HashMap<String, Object>();
        List<Record> records = Db.find("select * from data_source");
        //递归保存
        datas.put("allrows", records);
        return datas;
    }

    
    public boolean dbExists(String tableName)
    {
        int total = Db.queryLong("select count(id) from data_source where sourceName='"+tableName+"'").intValue();
        if (total == 0)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    
 
    
    
}
