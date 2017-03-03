package com.mindai.controller;

import java.util.HashMap;
import java.util.Map;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Results;

import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.mindai.model.JobList;
import com.mindai.service.JobListService;



/**
 * 本 demo 仅表达最为粗浅的 jfinal 用法，更为有价值的实用的企业级用法 详见 JFinal 俱乐部: http://jfinal.com/club
 * 
 * IndexController
 */
public class JobListController extends Controller
{
    private static JobListService service = new JobListService();
    
    public void list()
    {
        int limit=getParaToInt("limit");
        int offset=getParaToInt("offset");
        String search = getPara("search","%");
        Map<String, Object> data = service.getList(offset, limit,search);
        renderJson(data);
    }
    
    public void getAll()
    {
        Map<String, Object> data = service.getAll();
        renderJson(data);
    }
    
    public void getById()
    {
        String id = getPara("id");
        renderJson(JobList.dao.findById(id));
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
        JobList.dao.deleteById(id);
        service.deleteRefSchedule(id);
        renderMsg(true,"success");
    }
    
    public void save()
    {
        String jobId = getPara("jobId");
        String jobName = getPara("jobName");
        String refSrcDbSourceId = getPara("refSrcDbSourceId");
        String srcBase = getPara("srcBase");
        String srcTable = getPara("srcTable");
        String refDstDbSourceId = getPara("refDstDbSourceId");
        String dstBase = getPara("dstBase");
        String dstTable = getPara("dstTable");
        String incrementColumn = getPara("incrementColumn");
        String incrementColumnVal = getPara("incrementColumnVal");
        String jobRule = getPara("jobRule");
        String remarks = getPara("remarks");
        
        if (StrKit.isBlank(jobName))
        {
            renderMsg(false,"任务名称不能为空！");
            return;
        }
        if (StrKit.isBlank(refSrcDbSourceId))
        {
            renderMsg(false,"所属数据源不能为空！");
            return;
        }
        if (StrKit.isBlank(srcBase))
        {
            renderMsg(false,"来源库不能为空！");
            return;
        }
        if (StrKit.isBlank(srcTable))
        {
            renderMsg(false,"来源表不能为空！");
            return;
        }
        if (StrKit.isBlank(refDstDbSourceId))
        {
            renderMsg(false,"目标数据源不能为空！");
            return;
        }
        if (StrKit.isBlank(dstBase))
        {
            renderMsg(false,"目标库不能为空！");
            return;
        }
        if (StrKit.isBlank(dstTable))
        {
            renderMsg(false,"目标表不能为空！");
            return;
        }
        
        if (!StrKit.isBlank(jobRule))
        {
            //检查规则正确与否
            KieServices kieServices = KieServices.Factory.get();
            KieFileSystem kfs = kieServices.newKieFileSystem();
            kfs.write("src/main/resources/rules/rules.drl", jobRule.getBytes());
            KieBuilder kieBuilder = kieServices.newKieBuilder(kfs).buildAll();
            Results results = kieBuilder.getResults();
            if (results.hasMessages(org.kie.api.builder.Message.Level.ERROR)) 
            {
                renderMsg(false,"任务规则错误！"+results.getMessages());
                return;
            }
        }
        
        JobList job = new JobList();
        job.setJobName(jobName);
        job.setRefSrcDbSourceId(Integer.valueOf(refSrcDbSourceId));
        job.setSrcBase(srcBase);
        job.setSrcTable(srcTable);
        job.setRefDstDbSourceId(Integer.valueOf(refDstDbSourceId));
        job.setDstBase(dstBase);
        job.setDstTable(dstTable);
        job.setIncrementColumn(incrementColumn);
        job.setIncrementColumnVal(incrementColumnVal);
        job.setJobRule(jobRule);
        job.setRemarks(remarks);
        
        if ("".equals(jobId))
        {
            //检查表名是否存在
            boolean flag = service.exists(jobId);
            if (flag)
            {
                renderMsg(false,"任务名称已经存在！");
                return;
            }
            //新增加
            job.save();
        }
        else
        {
            job.setId(Long.valueOf(jobId));
            job.update();
        }

        renderMsg(true,"success");
    }
}
