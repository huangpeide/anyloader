package com.mindai.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.quartz.CronExpression;

import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.mindai.model.ExecuteList;
import com.mindai.model.ScheduleInfo;
import com.mindai.service.ScheduleInfoService;



/**
 * 本 demo 仅表达最为粗浅的 jfinal 用法，更为有价值的实用的企业级用法 详见 JFinal 俱乐部: http://jfinal.com/club
 * 
 * IndexController
 */
public class ScheduleInfoController extends Controller
{
    private static ScheduleInfoService service = new ScheduleInfoService();
    
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
        ScheduleInfo data = ScheduleInfo.dao.findById(id);
        renderJson(data);
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
        ScheduleInfo.dao.deleteById(id);
        service.deleteRefExecute(id);
        renderMsg(true,"success");
    }
    
    public void createExecute()
    {
        String id = getPara("id");
        if (service.existsExecute(id))
        {
            renderMsg(false,"存在相关执行计划，请稍后再试！");
            return;
        }
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ExecuteList executeList = new ExecuteList();
        executeList.setRefScheduleId(Integer.valueOf(id));
        executeList.setStartTime(sdf.format(new Date()));
        executeList.setRemarks("手动启动");
        executeList.setExecuteLog("");
        executeList.save();
        renderMsg(true,"success");
    }
    
    public void save()
    {
        String scheduleId = getPara("scheduleId");
        String scheduleName = getPara("scheduleName");
        String refJobId = getPara("refJobId");
        String scheduleType = getPara("scheduleType");
        String schedule = getPara("schedule");
        String remarks = getPara("remarks");
        
        if (StrKit.isBlank(scheduleName))
        {
            renderMsg(false,"计划名称不能为空！");
            return;
        }
        
        if (StrKit.isBlank(refJobId))
        {
            renderMsg(false,"所属任务不能为空！");
            return;
        }
        
        if (StrKit.isBlank(scheduleType))
        {
            renderMsg(false,"调度类型不能为空！");
            return;
        }
        
        if (!StrKit.isBlank(schedule))
        {
            try
            {
                // 初始化cron表达式解析器
                CronExpression exp = new CronExpression(schedule);
            }
            catch (ParseException e)
            {
                renderMsg(false,schedule+" 表达式无法解析！常见表达式例子如下:\n"+
                        "0 15 10 * * ? *     每天10点15分触发\n"+
                        "0 15 10 * * ? 2017  2017年每天10点15分触发\n"+
                        "0 * 14 * * ?        每天下午的 2点到2点59分每分触发\n"+
                        "0 0/5 14 * * ?      每天下午的 2点到2点59分(整点开始，每隔5分触发)\n"+
                        "0 0/5 14,18 * * ?   每天下午的 2点到2点59分、18点到18点59分(整点开始，每隔5分触发)\n"+
                        "0 0-5 14 * * ?      每天下午的 2点到2点05分每分触发\n"+
                        "0 15 10 ? * 6L      每月最后一周的星期五的10点15分触发\n"+
                        "0 15 10 ? * 6#3     每月的第三周的星期五开始触发"/*+e.getMessage()*/);
                return;
            }
        }
        
        ScheduleInfo scheduleInfo = new ScheduleInfo();
        scheduleInfo.setScheduleName(scheduleName);
        scheduleInfo.setRefJobId(Integer.valueOf(refJobId));
        scheduleInfo.setScheduleType(Integer.valueOf(scheduleType));
        scheduleInfo.setSchedule(schedule);
        scheduleInfo.setRemarks(remarks);
        
        if ("".equals(scheduleId))
        {
            //检查表名是否存在
            boolean flag = service.exists(scheduleName);
            if (flag)
            {
                renderMsg(false,"调度计划已经存在！");
                return;
            }
            //新增加
            scheduleInfo.save();
        }
        else
        {
            scheduleInfo.setId(Long.valueOf(scheduleId));
            scheduleInfo.update();
        }

        renderMsg(true,"success");
    }
}
