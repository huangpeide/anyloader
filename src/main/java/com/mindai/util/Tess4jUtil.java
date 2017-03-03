package com.mindai.util;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.util.ImageHelper;

public class Tess4jUtil
{
    private final static Logger log = Logger.getLogger(Tess4jUtil.class);
    
    private static final Tess4jUtil instance = new Tess4jUtil();
    
    private final ITesseract tessInstance = new Tesseract();
    
    private Tess4jUtil()
    {
        //得到资源路径
        File resFile = new File(Tess4jUtil.class.getClassLoader().getResource("app.properties").getPath());
        tessInstance.setDatapath(resFile.getParent()+"/tessdata");
        tessInstance.setLanguage("chi_sim");
    }
    
    public static Tess4jUtil getInstance()
    {
        return instance;
    }
    
    public String doORCFile(String filePath)
    {
        String result = "";
        try
        {
            File imageFile = new File(filePath);
            BufferedImage bi = ImageIO.read(imageFile);
            BufferedImage textImage = ImageHelper.convertImageToGrayscale(ImageHelper.getSubImage(bi, bi.getMinX(), bi.getMinY(), bi.getWidth(), bi.getHeight()));
            // 图片锐化,自己使用中影响识别率的主要因素是针式打印机字迹不连贯,所以锐化反而降低识别率
            textImage = ImageHelper.convertImageToBinary(textImage);
            // 图片放大5倍,增强识别率(很多图片本身无法识别,放大5倍时就可以轻易识,但是考滤到客户电脑配置低,针式打印机打印不连贯的问题,这里就放大5倍)
            textImage = ImageHelper.getScaledInstance(textImage, bi.getWidth() * 5, bi.getHeight() * 5);
            //写入文件
            File file = new File("picture/convert/"+UuidGenerator.getInstance().nextIdentity()+".png");
            ImageIO.write(textImage, "png", file);
            result = tessInstance.doOCR(file);
        }
        //这里用Throwable补货更多异常
        catch (Throwable e) 
        {
            log.error("doORCFile error!filePath is "+filePath,e);
        }
        return result;
    }
}
