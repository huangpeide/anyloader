package com.mindai.schedule;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Results;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.mindai.drools.CommonBean;
import com.mindai.model.DataSource;
import com.mindai.model.ExecuteList;
import com.mindai.model.JobList;
import com.mindai.model.ScheduleInfo;
import com.mindai.util.FileWriteUtil;
import com.mindai.util.HdfsUtil;
import com.mindai.util.HiveSqlUtil;
import com.mindai.util.Tools;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class ExecuteJob implements Runnable
{
    private final Logger log = Logger.getLogger(this.getClass());

    private ExecuteList executeList = null;
    
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
    
    StringBuffer exeLog = new StringBuffer();
    
    public ExecuteJob(ExecuteList executeList)
    {
        this.executeList = executeList;
    }
    
    @Override
    public void run()
    {
        long st = System.currentTimeMillis();
        ScheduleInfo scheduleInfo = null;
        try
        {
            Integer refJobId = executeList.getRefScheduleId();
            scheduleInfo = ScheduleInfo.dao.findById(refJobId);
            JobList jobList = JobList.dao.findById(scheduleInfo.getRefJobId());
            DataSource dataSource = DataSource.dao.findById(jobList.getRefSrcDbSourceId());
            
            exeLog.append("=============execute ").append(scheduleInfo.getScheduleName()).append(" job start=========== ").append("\n");
            
            //判断是mogo 还是mysql 1为Mysql 2为mongodb
            if (1 == dataSource.getDbType())
            {
                executeJobByMysql(dataSource, jobList);
            }
            else
            {
                executeJobByMongo(dataSource, jobList);
            }
            
            exeLog.append("execute ").append(scheduleInfo.getScheduleName()).append(" job success!").append("\n");
            executeList.setStatus(2);
            
            scheduleInfo.setStatus(2);
        }
        catch (Exception e)
        {
            exeLog.append("execute ").append(scheduleInfo.getScheduleName()).append(" job fail!").append(e).append("\n");
            executeList.setStatus(3);
            scheduleInfo.setStatus(3);
            log.info("ExecuteJob "+scheduleInfo.getScheduleName()+" error!",e);
        }
        long interval = System.currentTimeMillis()-st;
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        executeList.setEndTime(sdf.format(new Date()));
        exeLog.append("execute ").append(scheduleInfo.getScheduleName()).append(" cost time: ").append(interval).append("\n");
        exeLog.append("=============execute ").append(scheduleInfo.getScheduleName()).append(" job end=========== ").append("\n");
        executeList.setExecuteLog(exeLog.toString());
        executeList.update();
        //更新最后状态
        scheduleInfo.update();
        
        log.info("execute job "+scheduleInfo.getScheduleName()+" cost time:"+interval);
    }
    
    private KieBase getKieBase(String rules) throws Exception
    {
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kfs = kieServices.newKieFileSystem();
        kfs.write("src/main/resources/rules/rules.drl", rules.getBytes());
        KieBuilder kieBuilder = kieServices.newKieBuilder(kfs).buildAll();
        Results results = kieBuilder.getResults();
        if (results.hasMessages(org.kie.api.builder.Message.Level.ERROR)) 
        {
            System.out.println(results.getMessages());
        }
        KieContainer kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
        KieBase kieBase = kieContainer.getKieBase();
        return kieBase;
    }
    
    
    private void executeJobByMysql(DataSource dataSource,JobList jobList)throws Exception
    {
        Connection conn = null;
        Connection hive = null;
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
            String db_url = "jdbc:mysql://"+dataSource.getIp()+":"+dataSource.getPort()+"/"+jobList.getSrcBase()+"?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull";
            conn = DriverManager.getConnection(db_url, dataSource.getUserName(),dataSource.getPassword());
            if (null == conn)
            {
                exeLog.append("Db conn is null!"+db_url).append("\n");
                //executeList.setExecuteLog("Db conn is null!"+db_url);
                executeList.setStatus(3);
                return;
            }
            
            Statement stmt = conn.createStatement();
            String sql = "select * from "+jobList.getSrcTable();
            ResultSet rs = null;
            if (!StrKit.isBlank(jobList.getIncrementColumn()) && !StrKit.isBlank(jobList.getIncrementColumnVal()))
            {
                rs = stmt.executeQuery(sql +" where "+jobList.getIncrementColumn()
                +"='"+jobList.getIncrementColumnVal()+"' order by "+jobList.getIncrementColumn()+" asc");
            }
            else
            {
                if (!StrKit.isBlank(jobList.getIncrementColumn()))
                {
                    rs = stmt.executeQuery(sql+" order by "+jobList.getIncrementColumn()+" asc");
                }
                else
                {
                    rs = stmt.executeQuery(sql);
                }
            }
            
            ResultSetMetaData metaData = rs.getMetaData();  
            int columnCount = metaData.getColumnCount();
            
            //加载规则
            KieBase kieBase = getKieBase(jobList.getJobRule());
            
            int total = 0;
            JSONObject json = null;
            
            String localPath = PropKit.get("localhost.path");
            //创建文件
            File baseFile = new File(localPath);
            if (!baseFile.isDirectory())
            {
                baseFile.mkdirs();
            }
            //System.out.println(baseFile.getAbsolutePath());
            
            String time = format.format(new Date());
            String filePath = localPath+"/"+jobList.getJobName()+"_"+time;
            while (rs.next()) 
            {  
                json = new JSONObject();
                // 遍历每一列  
                for (int i = 1; i <= columnCount; i++)
                {  
                    String columnName =metaData.getColumnLabel(i);
                    String value = Tools.replaceStr(rs.getString(columnName));  
                    json.put(columnName, value);  
                }
                total++;
                CommonBean bean = new CommonBean(json);
                KieSession kieSession = kieBase.newKieSession();
                kieSession.insert(new CommonBean(json));  
                kieSession.fireAllRules();
                kieSession.dispose();
                
                //写文件
                FileWriteUtil.writeFile(filePath, bean.getJson());
            }
            if (!StrKit.isBlank(jobList.getIncrementColumn()) && total>0)
            {
                jobList.setIncrementColumnVal(json.getString(jobList.getIncrementColumn()));
                jobList.update();
            }
            //上传到hdfs
            if (total>0)
            {
                log.info("Scan "+jobList.getSrcBase()+"."+jobList.getSrcTable()+" "+total+" size!");
                exeLog.append("Scan ").append(jobList.getSrcBase()+"."+jobList.getSrcTable()+" "+total+" size!").append("\n");
                
                String dstFile = PropKit.get("hdfs.default.name")+PropKit.get("hdfs.bigtable.path")+"/"+jobList.getJobName()+"_"+time;
                //上传到hdfs
                HdfsUtil.upLoad(filePath, dstFile);
                //生成hive脚本，并执行
                Set<String> keys = json.keySet();
                StringBuffer sBuffer = new StringBuffer();
                sBuffer.append("create table if not exists ").append(jobList.getDstBase()).append(".").append(jobList.getDstTable()).append("(");
                for (String key : keys) 
                {  
                    //判断数据类型
                    Object object = json.get(key);
                    if (object instanceof List)
                    {
                        sBuffer.append(key).append(" ").append("array<MAP<STRING, STRING>>").append(",");
                    }
                    else if (object instanceof Map)
                    {
                        sBuffer.append(key).append(" ").append("MAP<STRING, STRING>").append(",");
                    }
                    else if (object instanceof String[])
                    {
                        sBuffer.append(key).append(" ").append("array<string>").append(",");
                    }
                    else if (object instanceof Integer[])
                    {
                        sBuffer.append(key).append(" ").append("array<string>").append(",");
                    }
                    else
                    {
                        sBuffer.append(key).append(" ").append("string").append(",");
                    }
                }
                sBuffer.delete(sBuffer.length()-1, sBuffer.length());
                sBuffer.append(")ROW FORMAT SERDE 'org.openx.data.jsonserde.JsonSerDe' STORED AS TEXTFILE");
                
                log.info("The "+jobList.getJobName()+" create table sql is "+sBuffer.toString());
                
                exeLog.append("The "+jobList.getJobName()+" create table sql is "+sBuffer.toString()).append("\n");
                
                //执行hive
                hive = HiveSqlUtil.getInstance().getConnection();
                HiveSqlUtil.getInstance().executeByCon("add jar hdfs://node1/user/admin/lib/json-serde-1.3.7-jar-with-dependencies.jar", hive);
                HiveSqlUtil.getInstance().executeByCon(sBuffer.toString(), hive);
                //执行load表
                HiveSqlUtil.getInstance().executeByCon("LOAD DATA INPATH '"+PropKit.get("hdfs.bigtable.path")+"/"+jobList.getJobName()+"_"+time+"' INTO TABLE "+jobList.getDstBase()+"."+jobList.getDstTable(), hive);
            }
            else
            {
                log.info("Scan "+jobList.getSrcBase()+"."+jobList.getSrcTable()+" nothing!");
                exeLog.append("Scan ").append(jobList.getSrcBase()+"."+jobList.getSrcTable()+" nothing!").append("\n");
            }
        }
        finally
        {
            if (null != conn)
            {
                conn.close();
            }
            if (null != hive)
            {
                hive.close();
            }
        }
    }
    
    private void executeJobByMongo(DataSource dataSource,JobList jobList)throws Exception
    {
        Connection hive = null;
        MongoClient mongoClient = null;
        try
        {
            if (!StrKit.isBlank(dataSource.getUserName()))
            {
                ServerAddress serverAddress = new ServerAddress(dataSource.getIp(),dataSource.getPort());  
                List<ServerAddress> addrs = new ArrayList<ServerAddress>();  
                addrs.add(serverAddress);  
                  
                //MongoCredential.createScramSha1Credential()三个参数分别为 用户名 数据库名称 密码  
                MongoCredential credential = MongoCredential.createScramSha1Credential(dataSource.getUserName(), jobList.getSrcBase(), dataSource.getPassword().toCharArray());  
                List<MongoCredential> credentials = new ArrayList<MongoCredential>();  
                credentials.add(credential);
                mongoClient = new MongoClient(addrs,credentials); 
            }
            else
            {
                mongoClient = new MongoClient(dataSource.getIp(),dataSource.getPort()); 
            }
            
            // 连接到数据库
            MongoDatabase mongoDatabase = mongoClient.getDatabase(jobList.getSrcBase());
            log.info("Connect to "+jobList.getSrcBase()+" database successfully");
            
            MongoCollection<Document> collection = mongoDatabase.getCollection(jobList.getSrcTable());

            log.info("Connect to "+jobList.getSrcTable()+" collection successfully");
            FindIterable<Document> findIterable = null;
            
            
            if (!StrKit.isBlank(jobList.getIncrementColumn()) && !StrKit.isBlank(jobList.getIncrementColumnVal()))
            {
                BasicDBObject queryObject = new BasicDBObject(jobList.getIncrementColumn(),new BasicDBObject("$gt",jobList.getIncrementColumnVal()));
                //1 为升序排列，而-1是用于降序排列
                findIterable = collection.find(queryObject).sort(new BasicDBObject(jobList.getIncrementColumn(),1));
            }
            else
            {
                if (!StrKit.isBlank(jobList.getIncrementColumn()))
                {
                    findIterable = collection.find().sort(new BasicDBObject(jobList.getIncrementColumn(),1));
                }
                else
                {
                    findIterable = collection.find();
                }
            }
            
            //加载规则
            KieBase kieBase = getKieBase(jobList.getJobRule());
            
            int total = 0;
            JSONObject json = null;
            
            String localPath = PropKit.get("localhost.path");
            //创建文件
            File baseFile = new File(localPath);
            if (!baseFile.isDirectory())
            {
                baseFile.mkdirs();
            }
            //System.out.println(baseFile.getAbsolutePath());
            
            String time = format.format(new Date());
            String filePath = localPath+"/"+jobList.getJobName()+"_"+time;
            MongoCursor<Document> mongoCursor = findIterable.iterator();
            while (mongoCursor.hasNext()) 
            {  
                Document mDoc = mongoCursor.next();
                Set<String> keys = mDoc.keySet();
                
                json = new JSONObject();
                // 遍历每一列  
                for (String key : keys)
                {
                    Object val = mDoc.get(key);
                    if (val instanceof String)
                    {
                        val = Tools.replaceStr(val.toString());
                    }
                    
                    if (key.startsWith("_"))
                    {
                        if ("_id".equals(key))
                        {
                            //json.put("uid", val);
                        }
                        else
                        {
                            json.put(key.substring(1,key.length()), val);
                        }
                    }
                    else
                    {
                        json.put(key, val);
                    }
                }
                total++;
                CommonBean bean = new CommonBean(json);
                KieSession kieSession = kieBase.newKieSession();
                kieSession.insert(new CommonBean(json));  
                kieSession.fireAllRules();
                kieSession.dispose();
                
                //写文件
                FileWriteUtil.writeFile(filePath, bean.getJson());
            }
            if (!StrKit.isBlank(jobList.getIncrementColumn()) && total>0)
            {
                jobList.setIncrementColumnVal(json.getString(jobList.getIncrementColumn()));
                jobList.update();
            }
            //上传到hdfs
            if (total>0)
            {
                log.info("Scan "+jobList.getSrcBase()+"."+jobList.getSrcTable()+" "+total+" size!");
                exeLog.append("Scan ").append(jobList.getSrcBase()+"."+jobList.getSrcTable()+" "+total+" size!").append("\n");
                String dstFile = PropKit.get("hdfs.default.name")+PropKit.get("hdfs.bigtable.path")+"/"+jobList.getJobName()+"_"+time;
                //上传到hdfs
                HdfsUtil.upLoad(filePath, dstFile);
                //生成hive脚本，并执行
                Set<String> keys = json.keySet();
                StringBuffer sBuffer = new StringBuffer();
                sBuffer.append("create table if not exists ").append(jobList.getDstBase()).append(".").append(jobList.getDstTable()).append("(");
                for (String key : keys) 
                {  
                    //判断数据类型
                    Object object = json.get(key);
                    if (object instanceof List)
                    {
                        sBuffer.append(key).append(" ").append("array<MAP<STRING, STRING>>").append(",");
                    }
                    else if (object instanceof Map)
                    {
                        sBuffer.append(key).append(" ").append("MAP<STRING, STRING>").append(",");
                    }
                    else if (object instanceof String[])
                    {
                        sBuffer.append(key).append(" ").append("array<string>").append(",");
                    }
                    else if (object instanceof Integer[])
                    {
                        sBuffer.append(key).append(" ").append("array<string>").append(",");
                    }
                    else
                    {
                        sBuffer.append(key).append(" ").append("string").append(",");
                    }
                }
                sBuffer.delete(sBuffer.length()-1, sBuffer.length());
                sBuffer.append(")ROW FORMAT SERDE 'org.openx.data.jsonserde.JsonSerDe' STORED AS TEXTFILE");
                
                log.info("The "+jobList.getJobName()+" create table sql is "+sBuffer.toString());
                exeLog.append("The "+jobList.getJobName()+" create table sql is "+sBuffer.toString()).append("\n");
                
                //执行hive
                hive = HiveSqlUtil.getInstance().getConnection();
                HiveSqlUtil.getInstance().executeByCon("add jar hdfs://node1/user/admin/lib/json-serde-1.3.7-jar-with-dependencies.jar", hive);
                HiveSqlUtil.getInstance().executeByCon(sBuffer.toString(), hive);
                //执行load表
                HiveSqlUtil.getInstance().executeByCon("LOAD DATA INPATH '"+PropKit.get("hdfs.bigtable.path")+"/"+jobList.getJobName()+"_"+time+"' INTO TABLE "+jobList.getDstBase()+"."+jobList.getDstTable(), hive);
            }
            else
            {
                log.info("Scan "+jobList.getSrcBase()+"."+jobList.getSrcTable()+" nothing!");
                exeLog.append("Scan ").append(jobList.getSrcBase()+"."+jobList.getSrcTable()+" nothing!").append("\n");
            }
        }
        finally
        {
            if (null != mongoClient)
            {
                mongoClient.close();
            }
            if (null != hive)
            {
                hive.close();
            }
        }
    }
    
    
    
}
