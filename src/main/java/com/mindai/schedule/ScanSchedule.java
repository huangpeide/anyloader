package com.mindai.schedule;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.CronExpression;


import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.mindai.model.ExecuteList;
import com.mindai.service.ScheduleInfoService;

public class ScanSchedule implements Runnable
{
    private final Logger log = Logger.getLogger(this.getClass());

    private long sleepTime = 30000L;
    
    public String getNextTime(String schedule) 
    {
        if (null == schedule && "".equals(schedule))
        {
            return null;
        }
        else
        {
            String time = "";
            try
            {
                CronExpression cronExpression = new CronExpression(schedule);
                Date date = cronExpression.getNextValidTimeAfter(new Date());
                SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                time = sdf.format(date);
            }
            catch (Exception e)
            {
                log.error("parse CronExpression error! "+schedule,e);
            }
            return time;
        }
    }
    
    @Override
    public void run()
    {
        while (true)
        {
            //一分钟扫描一次
            try
            {
                ScheduleInfoService service = new ScheduleInfoService();
                //扫描时间是否到了
                List<Record> records = Db.find("select * from schedule_info where scheduleType=2");
                for (Record record : records)
                {
                    String id = String.valueOf(record.getLong("id"));
                    //判断现在还有没有执行的
                    if(service.existsExecute(id))
                    {
                        log.warn("schedule "+id+" is execute!");
                        continue;
                    }
                    String schedule = record.getStr("schedule");
                    String time = getNextTime(schedule);
                    ExecuteList executeList = new ExecuteList();
                    executeList.setRefScheduleId(Integer.valueOf(id));
                    executeList.setStartTime(time);
                    executeList.setRemarks("定时器启动");
                    executeList.save();
                }
            }
            catch (Exception e)
            {
                log.error("ScanSchedule error!",e);
            }
            finally
            {
                try 
                {
                    Thread.sleep(sleepTime);
                } 
                catch (InterruptedException e1)
                {
                }
            }
            
        }
    }
    
}
