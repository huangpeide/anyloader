package com.mindai.util;

import java.util.UUID;

public class UuidGenerator
{
    private static volatile UuidGenerator _instance = null;
    
    private UuidGenerator()
    {
        
    }
    
    public static UuidGenerator getInstance()
    {
        if (_instance == null)
        {
            synchronized (UuidGenerator.class)
            {
                if (_instance == null)
                {
                    _instance = new UuidGenerator();
                }
            }
        }
        return _instance;
    }
    
    public synchronized String nextIdentity()
    {
        String uuid = UUID.randomUUID().toString();
        return uuid.replaceAll("-", "");
    }
}
