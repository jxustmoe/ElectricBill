package cn.jxust.electricBill.tools;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * 验证码解析类
 */
public class Ocr {

    private static ITesseract instance;

    /**
     * 初始化Tess4J配置
     */
    static {
        instance = new Tesseract();
        //设置tessdata目录位置
        instance.setDatapath(Ocr.class.getResource("/tessdata").getPath().substring(1));
        //设置只识别数字
        instance.setTessVariable("tessedit_char_whitelist", "1234567890");
        //设置识别模式
        instance.setPageSegMode(8);
        //设置识别库
        instance.setLanguage("num");
    }

    /**
     * 对外暴露解析验证码的方法
     */
    public static String parse(InputStream is) throws IOException, TesseractException {

        return doParse(ImageIO.read(is));

    }

    /**
     * 将图片灰度化,提高识别精度
     */
    private static BufferedImage grayImage(BufferedImage image) {

        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);//重点，技巧在这个参数BufferedImage.TYPE_BYTE_GRAY
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int rgb = image.getRGB(i, j);
                grayImage.setRGB(i, j, rgb);
            }
        }

        return grayImage;
    }

    /**
     * 解析验证码
     */
    private static String doParse(BufferedImage image) throws TesseractException {
        return instance.doOCR(grayImage(image)).trim();
    }
}
