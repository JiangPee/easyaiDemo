package me.jiangp;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import org.wlld.entity.ThreeChannelMatrix;
import org.wlld.tools.Picture;
import org.wlld.yolo.*;

import java.util.List;

/**
 * @Author: JiangP
 * @Date: 2024/10/14
 */
public class Main {

    public static final String MODEL_JSON_PATH = "C:\\Users\\retoo\\Desktop\\yj\\easyAiDemoe\\src\\main\\resources\\model\\yoloModel.json";

    public static void main(String[] args) throws Exception{
        //训练模型
//        trainingModel();

        //初始化
        FastYolo fastYolo = initModel();

        //识别
        String detectionImgPath = "C:\\Users\\retoo\\Desktop\\yj\\easyAiDemoe\\src\\main\\resources\\datasets\\images\\4.jpg";
        List<OutBox> detection = detection(fastYolo, detectionImgPath);

        //保存
        String imagePath = "C:\\Users\\retoo\\Desktop\\yj\\easyAiDemoe\\src\\main\\resources\\datasets\\images\\3.jpg";
        String outputPath = "C:\\Users\\retoo\\Desktop\\yj\\easyAiDemoe\\src\\main\\resources\\output\\3.jpg";
        ImgProcessor.drawBoundingBoxes(imagePath, outputPath, detection);
    }

    public static void trainingModel() throws Exception{
        YoloConfig yoloConfig = new YoloConfig();//创建配置参数类
        yoloConfig.setEnhance(20);
        FastYolo yolo=new FastYolo(yoloConfig);//初始化图像识别类
        List<YoloSample> data = YoloSampleProcessor.process();//获取目标标注类(样本数据实体类)集合
        yolo.toStudy(data);//开始训练，训练耗时较长
        YoloModel yoloModel=yolo.getModel();//训练完毕获取模型
        FileUtil.appendUtf8String(JSONUtil.toJsonStr(yoloModel) ,MODEL_JSON_PATH);//将模型序列化为JSON字符串，保存到磁盘文件
    }

    public static FastYolo initModel() throws Exception{
        YoloConfig yoloConfig = new YoloConfig();//创建配置参数类
        FastYolo yolo = new FastYolo(yoloConfig); //初始化图像识别类
        String modelJson = FileUtil.readUtf8String(MODEL_JSON_PATH);
        YoloModel yoloModel = JSONUtil.toBean(modelJson, YoloModel.class);
        yolo.insertModel(yoloModel);//识别类注入模型
        return yolo;
    }

    public static List<OutBox> detection(FastYolo yolo, String imgPath) throws Exception{
        Picture picture = new Picture();//初始化图像解析类
        ThreeChannelMatrix th = picture.getThreeMatrix(imgPath);//将图像解析为三通道矩阵
        return yolo.look(th,12456);//对该图像矩阵进行识别，并返回识别结果
    }
}