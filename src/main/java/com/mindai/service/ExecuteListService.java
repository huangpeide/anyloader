package com.mindai.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class ExecuteListService
{
    public Map<String, Object> getList(int offset, int limit,String search)
    {
        int total = Db.queryLong("select count(id) from execute_list").intValue();
        if (total == 0)
        {
            return null;
        }
        else
        {
            Map<String, Object> datas = new HashMap<String, Object>(2);
            List<Record> records = Db.find("select e.*,s.scheduleName from execute_list e left join schedule_info s on"
                + " e.refScheduleId=s.id order by e.id desc limit ?,?",offset,limit);
            datas.put("rows", records);
            datas.put("total", total);
            return datas;
        }
    }


    
}
