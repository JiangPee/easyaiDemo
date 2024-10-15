package me.jiangp;

import cn.hutool.core.collection.ListUtil;
import org.wlld.yolo.OutBox;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: ImgProcessor
 * @Author: JiangP
 * @Date: 2024/10/14
 */
public class ImgProcessor {

    public static void drawBoundingBoxes(String imagePath, String outputPath, List<OutBox> boxes) {
        try {
            // 读取原始图片
            BufferedImage image = ImageIO.read(new File(imagePath));
            if (image == null) {
                System.out.println("Failed to load image.");
                return;
            }

            // 创建 Graphics2D 对象，允许在图片上绘制
            Graphics2D g2d = image.createGraphics();

            // 设置绘制颜色、线宽和字体
            g2d.setColor(Color.RED);  // 检测框颜色
            g2d.setStroke(new BasicStroke(3));  // 线宽
            g2d.setFont(new Font("Arial", Font.BOLD, 20));  // 字体

            // 遍历所有的检测框
            for (OutBox box : boxes) {
                // 获取坐标、宽度、高度和类别名称
                int x = box.getX();
                int y = box.getY();
                int width = box.getWidth();
                int height = box.getHeight();
                String className = box.getTypeID();

                // 绘制检测框
                g2d.drawRect(x, y, width, height);

                // 绘制类别名称
                g2d.drawString(className, x, y - 5);  // 类别名称放在框的上方
            }

            // 释放资源
            g2d.dispose();

            // 保存绘制后的图片
            File outputfile = new File(outputPath);
            outputfile.createNewFile();
            ImageIO.write(image, "jpg", outputfile);

            System.out.println("Image saved to " + outputPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // 示例：创建多个OutBox对象的列表
        List<OutBox> boxes = new ArrayList<>();
        OutBox outBox = new OutBox();
        outBox.setX(59);
        outBox.setY(0);
        outBox.setWidth(144);
        outBox.setHeight(278);
        outBox.setTypeID("yilaguan");
        boxes.add(outBox);
        outBox.setX(196);
        outBox.setY(0);
        outBox.setWidth(142);
        outBox.setHeight(274);
        outBox.setTypeID("yilaguan");
        boxes.add(outBox);

        // 输入图像路径和输出图像路径
        String imagePath = "C:\\Users\\retoo\\Desktop\\yj\\easyAiDemoe\\src\\main\\resources\\datasets\\images\\4.jpg";
        String outputPath = "C:\\Users\\retoo\\Desktop\\yj\\easyAiDemoe\\src\\main\\resources\\output\\4.jpg";

        // 调用绘制方法
        drawBoundingBoxes(imagePath, outputPath, boxes);
    }
}
