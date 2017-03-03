package com.mindai.schedule;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class TestJson
{
    private final static Logger log = Logger.getLogger(TestJson.class);
    
    public static void main(String[] args)
    {
        Map<String, Object> lkMap = new LinkedHashMap<String, Object>();
        Map<String, Object> valMap = new HashMap<String, Object>();
        valMap.put("aa", "aa");
        valMap.put("bb", 11);
        valMap.put("cc", 99);
        int[] ratio = {7,9,10,5,8,4,2,1,6,3,7,9,10,5,8,4,2};
        valMap.put("dd", ratio);
        String[] aa = {"ds","ss"};
        valMap.put("ee", aa);
        Map<String, Object> valMap1 = new HashMap<String, Object>();
        valMap1.putAll(valMap);
        valMap.put("oo", valMap1);
        Map<String, Object> valMap2 = new HashMap<String, Object>();
        valMap2.putAll(valMap1);
        valMap1.put("ooo", valMap2);
        valMap1.put("dt", new Date());
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        list.add(valMap1);
        //list.add(valMap);
        list.add(valMap2);
        valMap.put("ll", list);
        JSONObject json = new JSONObject(valMap);
        //System.out.println(json);
        
        /*Set<String> keys = json.keySet();
        for (String key : keys) 
        {  
            //判断数据类型
            Object object = json.get(key);
            if (object instanceof List)
            {
                System.out.println("list:"+object);
                List<Object> list2 = (List<Object>)object;
                Object qqq = list2.get(0);
                List<Object> list3 = (List<Object>)qqq;
                Object qqq1 = list3.get(0);
            }
            if (object instanceof String[])
            {
                System.out.println("String[]:"+object);
            }
            //System.out.println(object);
        }*/
        
        JSONObject al = new JSONObject();
        al.put("al", null);
        al.put("mpa", new HashMap<>());
        System.out.println(al);
        System.out.println(al.toJSONString(al,SerializerFeature.WriteMapNullValue));
        //JSONObject rrr = JSON.parseObject(al.get("al").toString(),SerializerFeature.WriteMapNullValue);
        //System.out.println(rrr.toString());
        
        List<Object> listrr = new ArrayList<Object>();
        listrr.add("sss");
        listrr.add("sss");
        System.out.println(listrr.toString().replaceAll("\\[|\\]", ""));
        log.info("xxxxx");
        
    }
    
}
