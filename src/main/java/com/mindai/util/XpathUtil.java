package com.mindai.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XpathUtil
{
    private final static Logger log = Logger.getLogger(XpathUtil.class);
    
    public static List<String> fetchXpath(String html, String exp)
    {
        List<String> list = new ArrayList<String>();
        try
        {
            if (StringUtils.isBlank(html) || StringUtils.isBlank(exp))
            {
                return list;
            }
            try
            {
                HtmlCleaner hc = new HtmlCleaner();
                TagNode tn = hc.clean(html);
                org.w3c.dom.Document dom = new DomSerializer(new CleanerProperties()).createDOM(tn);
                XPath xPath = XPathFactory.newInstance().newXPath();
                Object result;
                try
                {
                    result = xPath.evaluate(exp, dom, XPathConstants.NODESET);
                    if (result instanceof NodeList)
                    {
                        NodeList nodeList = (NodeList)result;
                        for (int i = 0; i < nodeList.getLength(); i++)
                        {
                            Node node = nodeList.item(i);
                            list.add(StringEscapeUtils.unescapeHtml4(
                                node.getNodeValue() == null ? node.getTextContent() : node.getNodeValue()));
                        }
                    }
                }
                catch (Exception e)
                {
                    if (exp.contains("substring"))
                    {
                        try
                        {
                            result = xPath.evaluate(exp, dom);
                            list.add(StringEscapeUtils.unescapeHtml3((String)result));
                        }
                        catch (Exception e1)
                        {
                            log.error("fetchXpath error!Xpath is "+exp,e);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                log.error("fetchXpath error!Xpath is "+exp,e);
            }
            
            list = getRealContent(list);
            try
            {
                if (list.isEmpty())
                {
                    HtmlCleaner htmlCleaner = new HtmlCleaner();
                    TagNode tagNode = htmlCleaner.clean(html);
                    Object[] objects = tagNode.evaluateXPath(exp);
                    for (Object object : objects)
                    {
                        TagNode node = (TagNode)object;
                        CharSequence text = node.getText();
                        list.add(StringEscapeUtils.unescapeHtml3(text.toString()));
                    }
                }
            }
            catch (Exception e)
            {
                log.error("fetchXpath error!Xpath is "+exp,e);
            }
            list = getRealContent(list);
        }
        catch (Exception e)
        {
            log.error("fetchXpath error!Xpath is "+exp,e);
        }
        return list;
    }
    
    private static List<String> getRealContent(List<String> list)
    {
        boolean b = true;
        
        for (String str : list)
        {
            if (StringUtils.isNotBlank(str) && !isallBank(str))
            {
                b = false;
            }
        }
        if (b)
        {
            list.clear();
        }
        return list;
    }
    
    private static boolean isWhitespace(char ch)
    {
        Pattern compile = Pattern.compile("\\s");
        Matcher matcher = compile.matcher(String.valueOf(ch));
        if (matcher.find())
        {
            return true;
        }
        if (ch == ' ')
        {
            return true;
        }
        if (ch == '　')
        {
            return true;
        }
        if (Character.isWhitespace(ch))
        {
            return true;
        }
        return false;
    }
    
    private static boolean isallBank(String str)
    {
        boolean b = true;
        for (int i = 0; i < str.length(); i++)
        {
            if (!isWhitespace(str.charAt(i)))
            {
                b = false;
            }
        }
        return b;
    }
}
