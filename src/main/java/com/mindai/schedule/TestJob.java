package com.mindai.schedule;

import java.util.Date;

/**
 * Created by Chen on 15/4/29.
 */
public class TestJob implements Runnable
{
    
    public void run()
    {
        System.out.println("Current system time: " + new Date());
        System.out.println("Another minute ticked away...");
    }
    
}