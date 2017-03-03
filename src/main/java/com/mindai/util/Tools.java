package com.mindai.util;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tools
{
    
    public static String parseTime(String time)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMMyyyy HH:mm:ss", new Locale("US"));
        
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        String result = "";
        
        if (null == time || "".equals(time))
        {
            return result;
        }
        try
        {
            return format.format(sdf.parse(time));
        }
        catch (Exception e)
        {
            
        }
        return result;
    }
    
    public static String arrayToString(String[] values)
    {
        if (null == values)
        {
            return null;
        }
        StringBuffer sBuffer = new StringBuffer();
        for (String string : values)
        {
            if (!"".equals(string))
            {
                sBuffer.append(string).append(",");
            }
        }
        sBuffer.delete(sBuffer.length() - 1, sBuffer.length());
        
        return sBuffer.toString();
    }
    
    public static boolean isGoodTime(String time)
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        boolean result = true;
        
        if (null == time || "".equals(time))
        {
            return false;
        }
        try
        {
            format.parse(time);
        }
        catch (Exception e)
        {
            result = false;
        }
        return result;
    }
    
    /**
     * 是否属于一个范围
     */
    public static boolean isInnerTime(String st, String beginTime, String endTime)
    {
        try
        {
            long a = getCurrTime(beginTime);
            long b = getCurrTime(endTime);
            long c = getCurrTime(st);
            return (c >= a && c <= b);
        }
        catch (Exception e)
        {
            return false;
        }
    }
    
    public static String getTime()
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(new Date());
    }
    
    public static String getTime(long time)
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return format.format(calendar.getTime());
    }
    
    
    public static long getCurrTime(String time) throws ParseException
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        long result = 0l;
        
        if (null == time || "".equals(time))
        {
            return result;
        }
        return format.parse(time).getTime();
    }
    
    public static String replaceAllStr(String myString)
    {
        String newString = "";
        if (null != myString)
        {
            newString = myString.replaceAll("[\\s\\p{Zs}]+", "");
        }
        return newString;
    }
    
    public static void main(String[] args) throws ParseException
    {
        System.out.println("aa"+getCurrTime("2016-3-10 15:20:00"));
    }
    
    
    public static String replaceStr(String myString)
    {  
        String newString = ""; 
        char markA='\001';
        char markB='\002';
        char markC='\003';
        char markD='\004';
        char markE='\005';
        if (null !=  myString)
        {
            Pattern CRLF = Pattern.compile("\r\n|\r|\n|\n\r|"+markA+"|"+markB+"|"+markC+"|"+markD+"|"+markE);  
            Matcher m = CRLF.matcher(myString);  
            newString = m.replaceAll("");  
        }
        return newString;  
    } 
    
    
    
    /**
     * 是否属于一个范围
     */
    public static boolean isInnerIp(String userIp, String begin, String end)
    {
        try
        {
            BigInteger a = stringToBigInt(begin);
            BigInteger b = stringToBigInt(end);
            BigInteger c = stringToBigInt(userIp);
            return (c.longValue() >= a.longValue() && c.longValue() <= b.longValue());
        }
        catch (Exception e)
        {
            return false;
        }
        
    }
    
    public static long stringIpToLong(String ipInString)
    {
        if (null == ipInString || "".equals(ipInString))
        {
            return -1;
        }
        
        BigInteger ip = stringToBigInt(ipInString);
        
        if (null == ip)
        {
            return -1;
            
        }
        return ip.longValue();
    }
    
    /**
     * 是否属于一个范围
     */
    public static boolean isInnerPort(int userPort, int beginPort, int endPort)
    {
        return (userPort >= beginPort && userPort <= endPort);
    }
    
    /**
     * 将字符串形式的ip地址转换为BigInteger
     * 
     * @param ipInString 字符串形式的ip地址
     * @return 整数形式的ip地址
     */
    public static BigInteger stringToBigInt(String ipInString)
    {
        ipInString = ipInString.replace(" ", "");
        byte[] bytes;
        if (ipInString.contains(":"))
            bytes = ipv6ToBytes(ipInString);
        else
            bytes = ipv4ToBytes(ipInString);
        return new BigInteger(bytes);
    }
    
    /**
     * ipv6地址转有符号byte[17]
     */
    private static byte[] ipv6ToBytes(String ipv6)
    {
        byte[] ret = new byte[17];
        ret[0] = 0;
        int ib = 16;
        boolean comFlag = false;// ipv4混合模式标记
        if (ipv6.startsWith(":"))// 去掉开头的冒号
            ipv6 = ipv6.substring(1);
        String groups[] = ipv6.split(":");
        for (int ig = groups.length - 1; ig > -1; ig--)
        {// 反向扫描
            if (groups[ig].contains("."))
            {
                // 出现ipv4混合模式
                byte[] temp = ipv4ToBytes(groups[ig]);
                ret[ib--] = temp[4];
                ret[ib--] = temp[3];
                ret[ib--] = temp[2];
                ret[ib--] = temp[1];
                comFlag = true;
            }
            else if ("".equals(groups[ig]))
            {
                // 出现零长度压缩,计算缺少的组数
                int zlg = 9 - (groups.length + (comFlag ? 1 : 0));
                while (zlg-- > 0)
                {// 将这些组置0
                    ret[ib--] = 0;
                    ret[ib--] = 0;
                }
            }
            else
            {
                int temp = Integer.parseInt(groups[ig], 16);
                ret[ib--] = (byte)temp;
                ret[ib--] = (byte)(temp >> 8);
            }
        }
        return ret;
    }
    
    public static boolean isIPAddress(String ipaddr)
    {
        boolean flag = false;
        // String expression =
        // "!?\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b";
        // String expression =
        // "\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b";
        String expression =
            "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern pattern = Pattern.compile(expression);
        Matcher m = pattern.matcher(ipaddr);
        flag = m.matches();
        return flag;
    }
    

    public static boolean isValidNum(String num, int beginNum, int endNum)
    {
        int tmp = parseStrToNum(num);
        if (-1 == tmp)
        {
            return false;
        }
        return (tmp >= beginNum && tmp <= endNum);
    }
    
    /**
     * ipv4地址转有符号byte[5]
     */
    private static byte[] ipv4ToBytes(String ipv4)
    {
        byte[] ret = new byte[5];
        ret[0] = 0;
        // 先找到IP地址字符串中.的位置
        int position1 = ipv4.indexOf(".");
        int position2 = ipv4.indexOf(".", position1 + 1);
        int position3 = ipv4.indexOf(".", position2 + 1);
        // 将每个.之间的字符串转换成整型
        ret[1] = (byte)Integer.parseInt(ipv4.substring(0, position1));
        ret[2] = (byte)Integer.parseInt(ipv4.substring(position1 + 1, position2));
        ret[3] = (byte)Integer.parseInt(ipv4.substring(position2 + 1, position3));
        ret[4] = (byte)Integer.parseInt(ipv4.substring(position3 + 1));
        return ret;
    }
    
    /**
     * 将整数形式的ip地址转换为字符串形式
     * 
     * @param ipInBigInt 整数形式的ip地址
     * @return 字符串形式的ip地址
     */
    public static String bigIntToString(BigInteger ipInBigInt)
    {
        byte[] bytes = ipInBigInt.toByteArray();
        byte[] unsignedBytes = Arrays.copyOfRange(bytes, 1, bytes.length);
        if (bytes.length == 4 || bytes.length == 16)
        {
            unsignedBytes = bytes;
        }
        // 去除符号位
        try
        {
            String ip = InetAddress.getByAddress(unsignedBytes).toString();
            return ip.substring(ip.indexOf('/') + 1).trim();
        }
        catch (UnknownHostException e)
        {
            return "0.0.0.0";
            // throw new RuntimeException(e);
        }
    }
    
    public static int parseStrToNum(String str)
    {
        int num = -1;
        if (null == str || "".equals(str))
        {
            return num;
        }
        try
        {
            num = Integer.valueOf(str);
        }
        catch (Exception e)
        {
            // TODO: handle exception
        }
        return num;
    }
    
    
    
   
    
   
    
    public static String validIp(String attr, String[] values)
    {
        String msg = "ok";
        String[] ips;
        for (int i = 0; i < values.length; i++)
        {
            if (-1 < values[i].indexOf("~"))
            {
                ips = values[i].split("\\~");
                if (2 == ips.length)
                {
                    if (!Tools.isIPAddress(ips[0]) || !Tools.isIPAddress(ips[1]))
                    {
                        msg = "The " + attr + " value not IPAddress:" + values[i];
                        break;
                    }
                }
                else
                {
                    msg = "The " + attr + " value more than 2 '~'," + values[i];
                    break;
                }
                
            }
            else
            {
                if (!Tools.isIPAddress(values[i]))
                {
                    msg = "The " + attr + " value not IPAddress:" + values[i];
                    break;
                }
            }
        }
        return msg;
    }
    
    public static String validTime(String attr, String[] values)
    {
        String msg = "ok";
        String[] times;
        for (int i = 0; i < values.length; i++)
        {
            if (-1 < values[i].indexOf("~"))
            {
                times = values[i].split("\\~");
                if (2 == times.length)
                {
                    if (!Tools.isGoodTime(times[0]) || !Tools.isGoodTime(times[1]))
                    {
                        msg = "The " + attr + " value not good time,must yyyy-MM-dd HH:mm:ss,value:" + values[i];
                        break;
                    }
                }
                else
                {
                    msg = "The " + attr + " value more than 2 '~'," + values[i];
                    break;
                }
                
            }
            else
            {
                if (!Tools.isGoodTime(values[i]))
                {
                    msg = "The " + attr + " value not good time,must yyyy-MM-dd HH:mm:ss,value:" + values[i];
                    break;
                }
            }
        }
        return msg;
    }
    
    
    
    public static String validNumRange(String attr, String[] values, int beginNum, int endNum)
    {
        String msg = "ok";
        String[] ips;
        for (int i = 0; i < values.length; i++)
        {
            if (-1 < values[i].indexOf("~"))
            {
                ips = values[i].split("\\~");
                if (2 == ips.length)
                {
                    if (!Tools.isValidNum(ips[0], beginNum, endNum) || !Tools.isValidNum(ips[0], beginNum, endNum))
                    {
                        msg =
                            "The " + attr + " value not valid,must " + beginNum + "~" + endNum + ", value:" + values[i];
                        break;
                    }
                }
                else
                {
                    msg = "The " + attr + " value more than 2 '~'," + values[i];
                    break;
                }
                
            }
            else
            {
                if (!Tools.isValidNum(values[i], beginNum, endNum))
                {
                    msg = "The " + attr + " value not valid,must " + beginNum + "~" + endNum + ", value:" + values[i];
                    break;
                }
            }
        }
        return msg;
    }
    
}
