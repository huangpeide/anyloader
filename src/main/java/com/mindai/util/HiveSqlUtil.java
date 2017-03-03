package com.mindai.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.jfinal.kit.PropKit;

public class HiveSqlUtil
{
    private final static Logger log = Logger.getLogger(HiveSqlUtil.class);
    
    private final String DB_USER = PropKit.get("hive.name");
    
    private final String DB_PWD= PropKit.get("hive.password");
    
    private final String DB_URL = PropKit.get("hive.url");
    
    private static final HiveSqlUtil instance = new HiveSqlUtil();

    /**
     * 获取唯一实例
     * @return DBProxyJDBCImp
     */
    public static HiveSqlUtil getInstance ()
    {
        return instance;
    }
    
    public Connection getConnection() throws Exception
    {
        try
        {
            Class.forName("org.apache.hive.jdbc.HiveDriver");
        }
        catch (ClassNotFoundException e)
        {
            log.error("Where is your Hive JDBC Driver? Include in your library path!",e);
            throw e;
        }
        
        log.info("Hive JDBC Driver Registered!");
        Connection connection = null;
        try
        {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PWD);
        }
        catch (SQLException e)
        {
            log.error("Get connection Failed!",e);
            throw e;
        }
        return connection;
    }
    
    
    /**
     * 数据查询
     * @param sqlStr
     * @return
     * @throws SQLException
     */
    public void executeByCon(String sqlStr, Connection conn) throws SQLException
    {
        Statement stmt = null;

        //数据库访问时间变量
        long startTime = 0;
        long endTime = 0;
        long interval = 0;

        startTime = System.currentTimeMillis();
        try
        {
            if (conn == null)
            {
                throw new SQLException("Can't get db Connection!");
            }
            else
            {
                stmt = conn.createStatement();
            }
        }
        catch (SQLException e)
        {
            throw e;
        }

        // 执行数据库查询操作
        try
        {
            if (stmt != null)
            {
                stmt.execute(sqlStr);
            }
        }
        catch (SQLException e)
        {
            throw e;
        }

        endTime = System.currentTimeMillis();
        interval = endTime - startTime;
        //将数据库访问时间记录日志
        log.info("DB access interval is:"+interval);
    }
    
    /**
     * 数据查询
     * @param sqlStr
     * @return
     * @throws SQLException
     */
    public ResultSet queryByCon(String sqlStr, Connection conn) throws SQLException
    {
        Statement stmt = null;
        ResultSet rs = null;

        //数据库访问时间变量
        long startTime = 0;
        long endTime = 0;
        long interval = 0;

        startTime = System.currentTimeMillis();
        try
        {
            if (conn == null)
            {
                throw new SQLException("Can't get db Connection!");
            }
            else
            {
                stmt = conn.createStatement();
            }
        }
        catch (SQLException e)
        {
            throw e;
        }

        // 执行数据库查询操作
        try
        {
            if (stmt != null)
            {
                rs = stmt.executeQuery(sqlStr);
            }
        }
        catch (SQLException e)
        {
            throw e;
        }

        endTime = System.currentTimeMillis();
        interval = endTime - startTime;
        //将数据库访问时间记录日志
        log.info("DB access interval is:"+interval);
        return rs;
    }
    
    
}
