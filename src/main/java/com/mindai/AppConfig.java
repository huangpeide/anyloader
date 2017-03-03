package com.mindai;

import java.io.File;

import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.core.JFinal;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.template.Engine;
import com.mindai.controller.DataSourceController;
import com.mindai.controller.ExecuteListController;
import com.mindai.controller.IndexController;
import com.mindai.controller.JobListController;
import com.mindai.controller.ScheduleInfoController;
import com.mindai.interceptor.PermissionHandler;
import com.mindai.model._MappingKit;
import com.mindai.schedule.ScanExecuteList;
import com.mindai.schedule.ScanSchedule;



/**
 * JFinal总配置文件，挂接所有接口与插件
 *
 */
public class AppConfig extends JFinalConfig 
{
    
    // 项目启动
    public static void main(String[] args)
    {
        //String userDir = System.getProperty("user.dir");
        // 适应测试阶段的jetty，日志写入目录，tomcat写入tomcat/logs下
        //String catalinaBase = System.getProperty("catalina.base", userDir);
        //System.setProperty("catalina.base", catalinaBase);
        JFinal.start("src/main/webapp", 8080, "/", 10);
    }

    @Override
    public void configConstant(Constants me)
    {
        // 加载少量必要配置，随后可用PropKit.get(...)获取值
        PropKit.use("app.properties");
        me.setDevMode(PropKit.getBoolean("devMode", true));
    }

    @Override
    public void configEngine(Engine me)
    {
        //me.addSharedFunction("/common/_paginate.html");
    }

    @Override
    public void configHandler(Handlers me)
    {
        me.add(new PermissionHandler());
    }

    @Override
    public void configInterceptor(Interceptors me)
    {
        //me.addGlobalActionInterceptor(new AuthInterceptor());
    }

    public static DruidPlugin createDruidPlugin()
    {
        return new DruidPlugin(PropKit.get("db.url"), PropKit.get("db.username"), PropKit.get("db.password").trim());
    }
    
    @Override
    public void configPlugin(Plugins me)
    {
        // 配置C3p0数据库连接池插件
        DruidPlugin druidPlugin = createDruidPlugin();
        me.add(druidPlugin);
        
        // 配置Cron4jPlugin插件
        //me.add(new Cron4jPlugin());
        
        // 配置ActiveRecord插件
        ActiveRecordPlugin arp = new ActiveRecordPlugin(druidPlugin);
        // 所有映射在 MappingKit 中自动化搞定
        _MappingKit.mapping(arp);
        me.add(arp);
    }

    @Override
    public void configRoute(Routes me)
    {
        me.add("/", IndexController.class);
        me.add("/datasource", DataSourceController.class);
        me.add("/job", JobListController.class);
        me.add("/schedule", ScheduleInfoController.class);
        me.add("/execute", ExecuteListController.class);
    }
    
    @Override
    public void afterJFinalStart() 
    {
        //启动扫描线程
        new Thread(new ScanSchedule()).start();
        new Thread(new ScanExecuteList()).start();
        new File("picture/convert").mkdirs();
        System.out.println("========afterJFinalStart========");
    }
}