package com.mindai.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class JobListService
{
    public Map<String, Object> getList(int offset, int limit,String search)
    {
        int total = Db.queryLong("select count(id) from job_list where jobName like ?",search).intValue();
        if (total == 0)
        {
            return null;
        }
        else
        {
            List<Record> records = Db.find("select j.*,d.sourceName from job_list j left join data_source d on j.refSrcDbSourceId=d.id "
                + "where j.jobName like ? order by j.id desc limit ?,?",search, offset, limit);
            Map<String, Object> datas = new HashMap<String, Object>(2);
            datas.put("rows", records);
            datas.put("total", total);
            return datas;
        }
    }
    
    public Map<String, Object> getAll()
    {
        Map<String, Object> datas = new HashMap<String, Object>();
        List<Record> records = Db.find("select * from job_list");
        //递归保存
        datas.put("allrows", records);
        return datas;
    }
    
     
    
    public boolean exists(String categoryName)
    {
        int total = Db.queryLong("select count(id) from job_list where jobName='"+categoryName+"'").intValue();
        if (total == 0)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    

    public void deleteRefSchedule(String id)
    {
        Db.update("DELETE FROM schedule_info WHERE refJobId='"+id+"'");
    }
    
    
    
}
