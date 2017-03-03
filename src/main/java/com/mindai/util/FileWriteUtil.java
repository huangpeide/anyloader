package com.mindai.util;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class FileWriteUtil
{
    
    public static void writeFile(String filePath, String content)
    {
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        try
        {
            // 构造函数中的第二个参数true表示以追加形式写文件
            fos = new FileOutputStream(filePath, true);
            osw = new OutputStreamWriter(fos, "utf-8");
            osw.write(content+ "\n");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (null != osw)
                {
                    osw.close();
                }
                if (null != fos)
                {
                    fos.close();
                }
            }
            catch (IOException e)
            {
            }
        }
    }
    
    /**
     * B方法追加文件：使用FileWriter fileChaseFOS
     */
    public static void fileChaseFOS(String fileName, String content)
    {
        FileWriter writer = null;
        try
        {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            writer = new FileWriter(fileName, true);
            writer.write(content + "\n");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (null != writer)
                {
                    writer.flush();
                    writer.close();
                }
            }
            catch (IOException e)
            {
                
            }
        }
    }
    
}
