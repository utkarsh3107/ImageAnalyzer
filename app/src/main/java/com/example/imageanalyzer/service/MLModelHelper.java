package com.example.imageanalyzer.service;

import android.content.Context;
import android.util.Log;

import com.example.imageanalyzer.beans.ImageData;
import com.example.imageanalyzer.beans.enums.ModelTypes;
import com.example.imageanalyzer.ml.models.TesseractTextDetection;
import com.example.imageanalyzer.ml.models.YoloV5Detector;
import com.example.imageanalyzer.utils.Constants;

import java.util.List;

public class MLModelHelper {

    private Context context;

    private List<ImageData> imageNames;

    public MLModelHelper(Context context, List<ImageData> imageNames){
        this.context = context;
        this.imageNames = imageNames;
    }

    public void executeModels(List<ModelTypes> modelTypes){
        for(ModelTypes eachModel : modelTypes){
            switch (eachModel){
                case YOLOV5_IITJ:
                    executeIITJModel();
                    break;
                case YOLOV5_COCO:
                    executeCocoModel();
                    break;
                case TESSERACT_ANDROID:
                    executeTesseractModel();
                    break;
            }
        }
    }

    public void executeCocoModel(){
        try {
            YoloV5Detector objectDetector = new YoloV5Detector(this.context, Constants.COCO_YOLOV5_MODEL, Constants.COCO_YOLOV5_CLASSES, 6300, 85, 320);
            for (ImageData imageData : imageNames) {
                Log.i(Constants.OBJECT_FINDER_HELPER_CLASS, "executeCocoModel: Finding COCO images");
                objectDetector.detectImages(imageData);
            }
        } catch (Exception ex) {
            Log.i(Constants.OBJECT_FINDER_HELPER_CLASS, "executeCocoModel: Got exception: " + ex);
        }
    }

    public void executeIITJModel(){
        try {
            YoloV5Detector objectDetector = new YoloV5Detector(this.context, Constants.IITJ_YOLOV5_MODEL, Constants.IITJ_YOLOV5_CLASSES, 6300, 41, 320);
            for (ImageData imageData : imageNames) {
                Log.i(Constants.DASHBOARD_ACTIVITY, "executeIITJModel: Finding IITJ images");
                objectDetector.detectImages(imageData);
            }
        } catch (Exception ex) {
            Log.i(Constants.OBJECT_FINDER_HELPER_CLASS, "executeIITJModel: Got exception: " + ex);
        }
    }

    public void executeTesseractModel(){
        try{
            TesseractTextDetection tessTxtDetection = new TesseractTextDetection(this.context);
            tessTxtDetection.copyTessDataFiles();

            for (ImageData imageData : imageNames) {
                Log.i(Constants.DASHBOARD_ACTIVITY, "Finding text using tesseracts");
                tessTxtDetection.performOCR(imageData);
            }
        }catch(Exception ex){
            Log.i(Constants.DASHBOARD_ACTIVITY, "Got exception in tesseracts: " + ex);
        }
    }
}
