package com.mindai.drools;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.mindai.util.HttpDownUtil;
import com.mindai.util.Tess4jUtil;
import com.mindai.util.UuidGenerator;
import com.mindai.util.XpathUtil;

import us.codecraft.webmagic.selector.Html;

import org.apache.log4j.Logger;

public class CommonBean
{
    private final Logger log = Logger.getLogger(this.getClass());
    
    private JSONObject json = null;
    
    public CommonBean(JSONObject json)
    {
        this.json = json;
    }
    
    public String getJson()
    {
        return json.toJSONString(json,SerializerFeature.WriteMapNullValue);
    }
    
    public Object getByKey(String key)
    {
        if (null == key || "".equals(key.trim()))
        {
            return null;
        }
        return json.get(key);
    }
    
    public Object getJsonByKey(String json,String key)
    {
        if (null == json || null == key)
        {
            return null;
        }
        JSONObject jsonObject = JSON.parseObject(json);
        return jsonObject.get(key);
    }
    
    public void put(String key,Object value)
    {
        if (null == key || "".equals(key.trim()))
        {
            return;
        }
        json.put(key, value);
    }
    
    public void remove(String key)
    {
        if (null == key || "".equals(key.trim()))
        {
            return;
        }
        json.remove(key);
    }
    
    public String doORCByUrl(String url)
    {
        String val = "";
        if (null == url || "".equals(url.trim()))
        {
            return val;
        }
        try
        {
            String filePath = "picture/"+UuidGenerator.getInstance().nextIdentity()+".png";
            HttpDownUtil.download(url, filePath);
            val = Tess4jUtil.getInstance().doORCFile(filePath);
        }
        catch (Exception e)
        {
            log.error("doORCByUrl error!url is "+url,e);
        }
        return val;
    }
    
    public String selector(Object html,String sel)
    {
        String val = "";
        if (null == html)
        {
            return val;
        }
        try
        {
            Html doc = new Html(html.toString());
            val = doc.$(sel,"text").get();
        }
        catch (Exception e)
        {
            log.error("selector error!Sel is "+sel,e);
        }
        return val == null?"":val;
    }
    
    public List<String> xpath(Object html,String xpath)
    {
        List<String> list = new ArrayList<String>();
        if (null == html)
        {
            return list;
        }
        try
        {
            Html doc = new Html(html.toString());
            return doc.xpath(xpath).all();
        }
        catch (Exception e)
        {
            log.error("xpath error!Xpath is "+xpath,e);
        }
        return list;
    }
    
    public String xpathToStr(Object html,String xpath)
    {
        String val = "";
        if (null == html)
        {
            return val;
        }
        try
        {
            Html doc = new Html(html.toString());
            return doc.xpath(xpath).get();
        }
        catch (Exception e)
        {
            log.error("xpathToStr error!Xpath is "+xpath,e);
        }
        return val;
    }
    
    public String parseHtmlBySel(Object html,String sel)
    {
        String val = "";
        if (null == html)
        {
            return val;
        }
        try
        {
            Document doc = Jsoup.parse(html.toString(), "UTF-8");
            val = doc.select(sel).text();
        }
        catch (Exception e)
        {
            log.error("parseHtmlBySel error!Sel is "+sel,e);
        }
        return val == null?"":val;
    }
    
    public List<String> parseHtmlByXpath(Object html,String xpath)
    {
        List<String> list = new ArrayList<String>();
        if (null == html)
        {
            return list;
        }
        try
        {
            return XpathUtil.fetchXpath(html.toString(), xpath);
        }
        catch (Exception e)
        {
            log.error("parseHtmlByXpath error!Xpath is "+xpath,e);
        }
        return list;
    }
    
    public String parseHtmlByXpathToStr(Object html,String xpath)
    {
        String val = "";
        if (null == html)
        {
            return val;
        }
        try
        {
            List<String> list = XpathUtil.fetchXpath(html.toString(), xpath);
            return list.toString().replaceAll("\\[|\\]", "");
        }
        catch (Exception e)
        {
            log.error("parseHtmlByXpathToStr error!Xpath is "+xpath,e);
        }
        return val;
    }
    
    
    /*public List<Object> parseHtmlByXpath(Object html,String xpath)
    {
        List<Object> list = new ArrayList<Object>();
        if (null == html)
        {
            return list;
        }
        try
        {
            JXDocument doubanTest = new JXDocument(html.toString());
            return doubanTest.sel(xpath);
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
        return list;
    }
    
    public String parseHtmlByXpathToStr(Object html,String xpath)
    {
        String val = "";
        if (null == html)
        {
            return val;
        }
        try
        {
            JXDocument doubanTest = new JXDocument(html.toString());
            List<Object> list = doubanTest.sel(xpath);
            return list.toString().replaceAll("\\[|\\]", "");
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
        return val;
    }*/
}
