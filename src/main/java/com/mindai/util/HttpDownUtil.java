package com.mindai.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

public class HttpDownUtil
{
    private final static Logger log = Logger.getLogger(HttpDownUtil.class);
    
    public static void download(String url, String filePath)throws Exception
    {
        CloseableHttpClient httpclient = null;
        InputStream in = null;
        FileOutputStream fout = null;
        try
        {
            httpclient = HttpClients.createDefault();
            
            HttpGet httpget = new HttpGet(url);
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            in = entity.getContent();
            File file = new File(filePath);
            
            fout = new FileOutputStream(file);
            int l = -1;
            byte[] tmp = new byte[1024];
            while ((l = in.read(tmp)) != -1)
            {
                fout.write(tmp, 0, l);
            }
        }
        catch (Exception e)
        {
            log.error("download error!Url is "+url,e);
            throw e;
        }
        finally 
        {
            try
            {
                if (null != fout)
                {
                    fout.flush();
                    fout.close();
                }
                if (null != in)
                {
                    in.close();
                }
                if (null != httpclient)
                {
                    httpclient.close();
                }
            }
            catch (Exception e) 
            {
                // TODO: handle exception
            }
        }
    }
    
}
