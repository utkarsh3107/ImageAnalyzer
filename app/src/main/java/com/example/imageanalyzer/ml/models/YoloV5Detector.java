package com.example.imageanalyzer.ml.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.Size;

import com.example.imageanalyzer.beans.ImageData;
import com.example.imageanalyzer.beans.Recognition;
import com.example.imageanalyzer.utils.Constants;
import com.example.imageanalyzer.utils.ImageUtils;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class YoloV5Detector {

    private final Size INPUT_SIZE;
    private final int[] OUTPUT_SIZE;
    private Interpreter tflite;
    private List<String> associatedAxisLabels;
    private final Context context;

    public YoloV5Detector(Context context, String model, String classes, int classSizeDim, int totalClasses, int inputSize){
        Log.d(Constants.YOLOV5_CLASS, "Loading yolo_v5 model");
        INPUT_SIZE = new Size(inputSize, inputSize);
        OUTPUT_SIZE = new int[]{1, classSizeDim, totalClasses};
        this.context = context;
        Interpreter.Options options = new Interpreter.Options();
        try{
            ByteBuffer tfliteModel = FileUtil.loadMappedFile(context, model);
            tflite = new Interpreter(tfliteModel, options);
            associatedAxisLabels = FileUtil.loadLabels(context, classes);
            Log.i(Constants.YOLOV5_CLASS, "Loading yolo_v5 model successful");
        }catch(Exception ex){
            Log.i(Constants.YOLOV5_CLASS, "Error initializing class ", ex);
        }
    }

    public void detectImages(ImageData imageProps) {
        try{
            Bitmap bitmap = ImageUtils.convertToBitmap(this.context, imageProps.getImagePath());

            ImageProcessor imageProcessor = new ImageProcessor.Builder()
                            .add(new ResizeOp(INPUT_SIZE.getHeight(), INPUT_SIZE.getWidth(), ResizeOp.ResizeMethod.BILINEAR))
                            .add(new NormalizeOp(0, 255))
                            .build();
            TensorImage yolov5sTfliteInput = new TensorImage(DataType.FLOAT32);

            yolov5sTfliteInput.load(bitmap);
            yolov5sTfliteInput = imageProcessor.process(yolov5sTfliteInput);

            TensorBuffer probabilityBuffer;
            probabilityBuffer = TensorBuffer.createFixedSize(OUTPUT_SIZE, DataType.FLOAT32);
            Log.i(Constants.YOLOV5_CLASS, "detectImages: " +yolov5sTfliteInput.getTensorBuffer().getFlatSize() + " " + probabilityBuffer.getFlatSize());

            tflite.run(yolov5sTfliteInput.getBuffer(), probabilityBuffer.getBuffer());

            List<Recognition> allRecognitions = processRecognitions(probabilityBuffer.getFloatArray());
            List<Recognition> nmsRecognitions = applyNms(allRecognitions);

            ImageUtils.initObjects(imageProps, nmsRecognitions);
        }catch(Exception ex){
            Log.e(Constants.YOLOV5_CLASS, "detectImages: Error detectObjects class ", ex);
        }

    }

    private List<Recognition> processRecognitions(float[] recognitionArray) {
        List<Recognition> allRecognitions = new ArrayList<>();

        int numDetections = OUTPUT_SIZE[1];
        int numClasses = OUTPUT_SIZE[2];

        for (int i = 0; i < numDetections; i++) {
            int detectionOffset = i * numClasses;

            float confidence = recognitionArray[4 + detectionOffset];
            float[] classScores = Arrays.copyOfRange(recognitionArray, 5 + detectionOffset, 5 + detectionOffset + numClasses);

            Recognition recognition = createRecognition(classScores, confidence);
            allRecognitions.add(recognition);
        }

        return allRecognitions;
    }

    private Recognition createRecognition(float[] classScores, float confidence) {
        int labelId = findMaxScoreClass(classScores);
        String labelName = "";
        if(labelId < associatedAxisLabels.size()){
            labelName = associatedAxisLabels.get(labelId);
        }

        float maxLabelScore = classScores[labelId];
        return new Recognition(labelId, labelName, maxLabelScore, confidence);
    }

    private int findMaxScoreClass(float[] classScores) {
        int maxLabelId = 0;
        float maxLabelScore = 0.0f;

        for (int j = 0; j < classScores.length; j++) {
            if (classScores[j] > maxLabelScore) {
                maxLabelScore = classScores[j];
                maxLabelId = j;
            }
        }

        return maxLabelId;
    }

    protected PriorityQueue<Recognition> initializePriorityQueue(List<Recognition> allRecognitions, int classId, float detectThreshold){
        PriorityQueue<Recognition> pq = new PriorityQueue<>(allRecognitions.size(),
                Comparator.comparing(Recognition::getConfidence).reversed());

        for (Recognition recognition : allRecognitions) {
            if (recognition.getLabelId() == classId && recognition.getConfidence() > detectThreshold) {
                pq.add(recognition);
            }
        }
        return  pq;
    }

    private List<Recognition> performNMS(PriorityQueue<Recognition> pq) {
        List<Recognition> nmsRecognitions = new ArrayList<>();
        while (!pq.isEmpty()) {
            nmsRecognitions.add(pq.poll());
        }
        return nmsRecognitions;
    }

    protected List<Recognition> applyNms(List<Recognition> allRecognitions){
        List<Recognition> nmsRecognitions = new ArrayList<>();

        int numClasses = OUTPUT_SIZE[2] - 5;
        float detectThreshold = 0.25f;

        for (int classId = 0; classId < numClasses; classId++) {
            PriorityQueue<Recognition> pq = initializePriorityQueue(allRecognitions, classId, detectThreshold);
            nmsRecognitions.addAll(performNMS(pq));
        }
        return nmsRecognitions;
    }

}
