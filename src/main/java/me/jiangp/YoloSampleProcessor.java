package me.jiangp;

import org.wlld.yolo.YoloBody;
import org.wlld.yolo.YoloSample;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.concurrent.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * @Description: YoloSampleProcessor
 * @Author: JiangP
 * @Date: 2024/10/14
 */

public class YoloSampleProcessor {

    // 定义文件路径
    private static final String IMAGE_DIR = "C:\\Users\\retoo\\Desktop\\yj\\easyAiDemoe\\src\\main\\resources\\datasets\\images\\";
    private static final String LABEL_DIR = "C:\\Users\\retoo\\Desktop\\yj\\easyAiDemoe\\src\\main\\resources\\datasets\\ImageSets\\";
    private static final String CLASSES_FILE = "C:\\Users\\retoo\\Desktop\\yj\\easyAiDemoe\\src\\main\\resources\\datasets\\ImageSets\\classes.txt";
    private static final int THREAD_COUNT = 8; // 定义线程数

    // 读取类别信息
    private static Map<Integer, String> loadClasses() throws IOException {
        Map<Integer, String> classMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(CLASSES_FILE))) {
            String line;
            int classId = 0;
            while ((line = br.readLine()) != null) {
                classMap.put(classId++, line.trim());
            }
        }
        return classMap;
    }

    // 解析标注文件并生成 YoloBody 对象
    private static List<YoloBody> parseLabels(String labelFile, int imgWidth, int imgHeight) throws IOException {
        List<YoloBody> bodies = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(labelFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                int typeID = Integer.parseInt(parts[0]);

                // 归一化后的中心点坐标和宽高
                double normCenterX = Double.parseDouble(parts[1]);
                double normCenterY = Double.parseDouble(parts[2]);
                double normWidth = Double.parseDouble(parts[3]);
                double normHeight = Double.parseDouble(parts[4]);

                // 反归一化：将归一化的值乘以图片的实际宽度和高度，得到实际的尺寸
                double centerX = normCenterX * imgWidth;
                double centerY = normCenterY * imgHeight;
                double width = normWidth * imgWidth;
                double height = normHeight * imgHeight;

                // 计算左上角坐标
                int x = (int) (centerX - width / 2);
                int y = (int) (centerY - height / 2);

                // 创建 YoloBody 对象
                YoloBody body = new YoloBody();
                body.setX(x);
                body.setY(y);
                body.setWidth((int) width);
                body.setHeight((int) height);
                body.setTypeID(typeID);
                bodies.add(body);
            }
        }
        return bodies;
    }

    // 创建 YoloSample 对象，动态获取图片宽高
    private static YoloSample createYoloSample(String imageFile, String labelFile) throws IOException {
        // 使用 ImageIO 获取图片的宽度和高度
        BufferedImage image = ImageIO.read(new File(imageFile));
        int imgWidth = image.getWidth();
        int imgHeight = image.getHeight();

        // 创建 YoloSample 对象
        YoloSample sample = new YoloSample();
        sample.setLocationURL(imageFile);
        sample.setYoloBodies(parseLabels(labelFile, imgWidth, imgHeight));
        return sample;
    }

    // 使用线程池并行处理
    public static List<YoloSample> process() throws IOException, InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT); // 创建线程池

        // 加载类别映射
        Map<Integer, String> classMap = loadClasses();

        List<Future<YoloSample>> futures = new ArrayList<>();
        List<YoloSample> yoloSamples = new ArrayList<>(); // 最终的结果列表

        // 获取所有图片文件
        Path imageDir = Paths.get(IMAGE_DIR);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(imageDir, "*.jpg")) {
            for (Path imagePath : stream) {
                // 对应的标注文件
                String imageFileName = imagePath.getFileName().toString();
                String labelFileName = LABEL_DIR + imageFileName.replace(".jpg", ".txt");

                // 提交任务到线程池，异步处理
                Future<YoloSample> future = executor.submit(() -> createYoloSample(imagePath.toString(), labelFileName));
                futures.add(future);
            }
        }

        // 获取所有结果并收集到 yoloSamples 列表中
        for (Future<YoloSample> future : futures) {
            YoloSample sample = future.get(); // 阻塞等待结果
            yoloSamples.add(sample); // 将结果添加到列表中
        }

        executor.shutdown(); // 关闭线程池

        return yoloSamples; // 返回结果列表
    }

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        YoloSampleProcessor processor = new YoloSampleProcessor();
        List<YoloSample> samples = processor.process(); // 获取所有处理结果

        // 输出处理结果
        for (YoloSample sample : samples) {
            System.out.println("Processed: " + sample.getLocationURL());
        }
    }
}