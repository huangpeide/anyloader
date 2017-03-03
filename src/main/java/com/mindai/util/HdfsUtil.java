package com.mindai.util;

import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;


public class HdfsUtil
{
    
    private final static Logger log = Logger.getLogger(HdfsUtil.class);
    
    private static Configuration conf = null;
    
    static
    {
        conf = new Configuration();
        //conf.set("fs.hdfs.impl",org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
        //conf.set("fs.file.impl",org.apache.hadoop.fs.LocalFileSystem.class.getName());
    }
    
    public static void main(String[] args) throws IOException
    {
        HdfsUtil.upLoad("c:\\aa.html", "hdfs://192.168.2.207:8020/user/bigtable/aa.html");
    }
    
    public static void upLoad(String src, String dst) throws IOException
    {
        FileSystem fs = FileSystem.get(URI.create(dst), conf);
        fs.copyFromLocalFile(true,new Path(src), new Path(dst));
        
        log.info("upload file:" + src + " to hdfs " + dst + " success!");
    }
    
    
    
    
    
}