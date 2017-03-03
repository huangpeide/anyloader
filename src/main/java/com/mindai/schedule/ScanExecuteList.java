package com.mindai.schedule;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.mindai.model.ExecuteList;

public class ScanExecuteList implements Runnable
{
    private final Logger log = Logger.getLogger(this.getClass());

    private long sleepTime = 10000L;
    
    int job_num = 2;
    
    ExecutorService mainService = Executors.newFixedThreadPool(job_num);
    
    
    public static long getCurrTime(String time)
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        long result = 0l;
        
        if(null == time || "".equals(time))
        {
            return result;
        }
        try
        {
            return format.parse(time).getTime();
        }
        catch (Exception e)
        {
            
        }
        return result;
    }
    
    @Override
    public void run()
    {
        while (true)
        {
            //一分钟扫描一次
            try
            {
                //更新状态 执行
                int threadCount = ((ThreadPoolExecutor)mainService).getActiveCount();
                int size = job_num - threadCount;
                //0个活动表示空闲
                if (0 < size)
                {
                    //扫描时间是否到了
                    List<Record> records = Db.find("select * from execute_list where status=0");
                    if (!records.isEmpty())
                    {
                        for (int i = 0; i < records.size(); i++)
                        {
                            if (i < size)
                            {
                                Record record = records.get(i);
                                String id = String.valueOf(record.getLong("id"));
                                
                                String startTime = record.getStr("startTime");
                                //判断当前时间是否大于开始时间
                                if (new Date().getTime() >= getCurrTime(startTime))
                                {
                                    //更新状态执行
                                    ExecuteList executeList = ExecuteList.dao.findById(id);
                                    executeList.setStatus(1);
                                    executeList.update();
                                    mainService.execute(new ExecuteJob(executeList));
                                }
                            }
                        }
                    }
                }
                else
                {
                    log.info("Some job is runing,so wait!");
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
