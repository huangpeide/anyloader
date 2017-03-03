package com.mindai.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class ScheduleInfoService
{
    public Map<String, Object> getList(int offset, int limit,String search)
    {
        int total = Db.queryLong("select count(id) from schedule_info where scheduleName like ?",search).intValue();
        if (total == 0)
        {
            return null;
        }
        else
        {
            Map<String, Object> datas = new HashMap<String, Object>(2);
            List<Record> records = Db.find("select s.*,j.jobName from schedule_info s left join job_list j on s.refJobId=j.id"
                + " where s.scheduleName like ? order by s.id desc limit ?,?",search,offset,limit);
            datas.put("rows", records);
            datas.put("total", total);
            return datas;
        }
    }
    
    public boolean existsExecute(String id)
    {
        int total = Db.queryLong("select count(id) from execute_list where status in (0,1) and refScheduleId='"+id+"'").intValue();
        if (total == 0)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
     
    
    public boolean exists(String tagName)
    {
        int total = Db.queryLong("select count(id) from schedule_info where scheduleName='"+tagName+"'").intValue();
        if (total == 0)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    
    
    public Map<String, Object> getAll()
    {
        Map<String, Object> datas = new HashMap<String, Object>();
        List<Record> records = Db.find("select * from schedule_info");
        //递归保存
        datas.put("allrows", records);
        return datas;
    }
   
    public void deleteRefExecute(String id)
    {
        Db.update("DELETE FROM execute_list WHERE refScheduleId='"+id+"'");
    }
    
}
