package com.example.imageanalyzer.ml.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;


import com.example.imageanalyzer.beans.ModelExecutionResult;
import com.example.imageanalyzer.utils.ImageUtils;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRotatedRect;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.Utils;

import org.opencv.utils.Converters;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.flex.FlexDelegate;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class OCRModelExecutor implements AutoCloseable {
    private static final String TAG = "TfLiteOCRDemo";
    private static final String textDetectionModel = "text_detection.tflite";
    private static final String textRecognitionModel = "text_recognition.tflite";
    private static final int numberThreads = 4;
    private static final String alphabets = "0123456789abcdefghijklmnopqrstuvwxyz";
    private static final int displayImageSize = 257;
    private static final int detectionImageHeight = 320;
    private static final int detectionImageWidth = 320;
    private static final float[] detectionImageMeans = {103.94f, 116.78f, 123.68f};
    private static final float[] detectionImageStds = {1.0f, 1.0f, 1.0f};
    private static final int detectionOutputNumRows = 80;
    private static final int detectionOutputNumCols = 80;
    private static final double detectionConfidenceThreshold = 0.5;
    private static final double detectionNMSThreshold = 0.4;
    private static final int recognitionImageHeight = 31;
    private static final int recognitionImageWidth = 200;
    private static final float recognitionImageMean = 0.0f;
    private static final float recognitionImageStd = 255.0f;
    private static final int recognitionModelOutputSize = 48;

    private GpuDelegate gpuDelegate;
    private ByteBuffer recognitionResult;
    private Interpreter detectionInterpreter;
    private Interpreter recognitionInterpreter;
    private float ratioHeight;
    private float ratioWidth;
    private MatOfInt indicesMat;
    private MatOfRotatedRect boundingBoxesMat;
    private HashMap<String, Integer> ocrResults;

    private final Context context;

    public OCRModelExecutor(Context context, boolean useGPU) {
        try {
            if (!OpenCVLoader.initDebug()) {
                throw new Exception("Unable to load OpenCV");
            } else {
                Log.d(TAG, "OpenCV loaded");
            }
        } catch (Exception e) {
            String exceptionLog = "OpenCVLoader something went wrong: " + e.getMessage();
            Log.d(TAG, exceptionLog);
        }

        try{
            this.detectionInterpreter = getInterpreter(context, textDetectionModel, useGPU);
        }catch(Exception ex){
            String exceptionLog = "detectionInterpreter something went wrong: " + ex.getMessage();
            Log.d(TAG, exceptionLog);
        }

        try{
            FlexDelegate flexDelegate = new FlexDelegate();
            Interpreter.Options options = new Interpreter.Options().addDelegate(flexDelegate);
            this.recognitionInterpreter = getInterpreter(context, textRecognitionModel, false, options);
        }catch(Exception ex){
            String exceptionLog = "recognitionInterpreter something went wrong: " + ex.getMessage();
            Log.d(TAG, exceptionLog);
        }

        this.recognitionResult = ByteBuffer.allocateDirect(recognitionModelOutputSize * 8);
        this.recognitionResult.order(ByteOrder.nativeOrder());
        this.indicesMat = new MatOfInt();
        this.boundingBoxesMat = new MatOfRotatedRect();
        this.ocrResults = new HashMap<>();
        this.context = context;
    }

    public ModelExecutionResult execute(String path) {
        try {
            Bitmap data = ImageUtils.convertToBitmap(this.context, path);
            this.ratioHeight = data.getHeight() / (float) detectionImageHeight;
            this.ratioWidth = data.getWidth() / (float) detectionImageWidth;
            this.ocrResults.clear();

            detectTexts(data);

            Bitmap bitmapWithBoundingBoxes = recognizeTexts(data, boundingBoxesMat, indicesMat);

            return new ModelExecutionResult(bitmapWithBoundingBoxes, "OCR result", ocrResults);
        } catch (Exception e) {
            e.printStackTrace();
            String exceptionLog = "execute something went wrong: " + e.getMessage();
            Log.d(TAG, exceptionLog);

            Bitmap emptyBitmap = ImageUtils.createEmptyBitmap(displayImageSize, displayImageSize, 0);
            return new ModelExecutionResult(emptyBitmap, exceptionLog, new HashMap<>());
        }
    }

    private void detectTexts(Bitmap data) {
        TensorImage detectionTensorImage = ImageUtils.bitmapToTensorImageForDetection(
                data,
                detectionImageWidth,
                detectionImageHeight,
                detectionImageMeans,
                detectionImageStds
        );

        //TODO check below detectionTensorImage.buffer.rewind()
        TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, detectionImageHeight, detectionImageWidth, 3}, DataType.FLOAT32);
        inputFeature0.loadBuffer(detectionTensorImage.getBuffer());
        Object[] detectionInputs = {inputFeature0};
        HashMap<Integer, Object> detectionOutputs = new HashMap<>();

        float[][][] detectionScores = new float[1][detectionOutputNumRows][detectionOutputNumCols];
        float[][][] detectionGeometries = new float[1][detectionOutputNumRows][detectionOutputNumCols];
        detectionOutputs.put(0, detectionScores);
        detectionOutputs.put(1, detectionGeometries);

        detectionInterpreter.runForMultipleInputsOutputs(detectionInputs, detectionOutputs);

        // Transpose detection output tensors
        float[][][][] transposedDetectionScores = new float[1][1][detectionOutputNumRows][detectionOutputNumCols];
        float[][][][] transposedDetectionGeometries = new float[1][5][detectionOutputNumRows][detectionOutputNumCols];

        for (int i = 0; i < transposedDetectionScores[0][0].length; i++) {
            for (int j = 0; j < transposedDetectionScores[0][0][0].length; j++) {
                transposedDetectionScores[0][0][i][j] = detectionScores[0][i][j];
                for (int k = 0; k < 5; k++) {
                    //TODO check below  transposedDetectionGeometries[0][k][i][j] = detectionGeometries[0][i][j][k]
                    transposedDetectionGeometries[0][k][i][j] = detectionGeometries[0][i][j];
                }
            }
        }

        ArrayList<RotatedRect> detectedRotatedRects = new ArrayList<>();
        ArrayList<Float> detectedConfidences = new ArrayList<>();

        for (int y = 0; y < transposedDetectionScores[0][0].length; y++) {
            float[] detectionScoreData = transposedDetectionScores[0][0][y];
            float[] detectionGeometryX0Data = transposedDetectionGeometries[0][0][y];
            float[] detectionGeometryX1Data = transposedDetectionGeometries[0][1][y];
            float[] detectionGeometryX2Data = transposedDetectionGeometries[0][2][y];
            float[] detectionGeometryX3Data = transposedDetectionGeometries[0][3][y];
            float[] detectionRotationAngleData = transposedDetectionGeometries[0][4][y];

            for (int x = 0; x < transposedDetectionScores[0][0][0].length; x++) {
                if (detectionScoreData[x] < 0.5f) {
                    continue;
                }

                float offsetX = x * 4.0f;
                float offsetY = y * 4.0f;

                float h = detectionGeometryX0Data[x] + detectionGeometryX2Data[x];
                float w = detectionGeometryX1Data[x] + detectionGeometryX3Data[x];

                float angle = detectionRotationAngleData[x];
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);

                Point offset = new Point(
                        offsetX + cos * detectionGeometryX1Data[x] + sin * detectionGeometryX2Data[x],
                        offsetY - sin * detectionGeometryX1Data[x] + cos * detectionGeometryX2Data[x]
                );
                Point p1 = new Point(-sin * h + offset.x, -cos * h + offset.y);
                Point p3 = new Point(-cos * w + offset.x, sin * w + offset.y);
                Point center = new Point(0.5 * (p1.x + p3.x), 0.5 * (p1.y + p3.y));

                RotatedRect textDetection = new RotatedRect(center, new Size(w, h), (float) (-angle * 180.0 / Math.PI));
                detectedRotatedRects.add(textDetection);
                detectedConfidences.add(detectionScoreData[x]);
            }
        }

        Mat detectedConfidencesMat = Converters.vector_float_to_Mat(detectedConfidences);
        MatOfFloat detectedConfidencesMatFloat = new MatOfFloat(detectedConfidencesMat);

        boundingBoxesMat = new MatOfRotatedRect(Converters.vector_RotatedRect_to_Mat(detectedRotatedRects));
        Dnn.NMSBoxesRotated(boundingBoxesMat, detectedConfidencesMatFloat, (float) detectionConfidenceThreshold, (float) detectionNMSThreshold, indicesMat);
    }

    private Bitmap recognizeTexts(Bitmap data, MatOfRotatedRect boundingBoxesMat, MatOfInt indicesMat) {
        Bitmap bitmapWithBoundingBoxes = data.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmapWithBoundingBoxes);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        paint.setColor(Color.GREEN);

        int[] indices = indicesMat.toArray();
        RotatedRect[] boundingBoxes = boundingBoxesMat.toArray();

        for (int i : indices) {
            RotatedRect boundingBox = boundingBoxes[i];
            ArrayList<Point> srcVertices = new ArrayList<>();
            srcVertices.add(new Point(0, recognitionImageHeight - 1));
            srcVertices.add(new Point(0, 0));
            srcVertices.add(new Point(recognitionImageWidth - 1, 0));
            srcVertices.add(new Point(recognitionImageWidth - 1, recognitionImageHeight - 1));

            MatOfPoint2f srcVerticesMat = new MatOfPoint2f();
            srcVerticesMat.fromList(srcVertices);

            MatOfPoint2f targetVerticesMat = new MatOfPoint2f();
            targetVerticesMat.fromArray(
                    new Point(0, recognitionImageHeight - 1),
                    new Point(0, 0),
                    new Point(recognitionImageWidth - 1, 0),
                    new Point(recognitionImageWidth - 1, recognitionImageHeight - 1)
            );

            Mat rotationMatrix = Imgproc.getPerspectiveTransform(srcVerticesMat, targetVerticesMat);
            Mat recognitionBitmapMat = new Mat();
            Mat srcBitmapMat = new Mat();
            Utils.bitmapToMat(data, srcBitmapMat);
            Imgproc.warpPerspective(srcBitmapMat, recognitionBitmapMat, rotationMatrix, new Size(recognitionImageWidth, recognitionImageHeight));

            Bitmap recognitionBitmap = Bitmap.createBitmap(recognitionImageWidth, recognitionImageHeight, Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(recognitionBitmapMat, recognitionBitmap);

            ByteBuffer recognitionTensorImage = ImageUtils.bitmapToTensorImageForRecognition(
                    recognitionBitmap,
                    recognitionImageWidth,
                    recognitionImageHeight,
                    recognitionImageMean,
                    recognitionImageStd
            ).getBuffer();

            recognitionResult.rewind();
            recognitionInterpreter.run(recognitionTensorImage, recognitionResult);

            StringBuilder recognizedText = new StringBuilder();
            for (int k = 0; k < recognitionModelOutputSize; k++) {
                int alphabetIndex = recognitionResult.getInt(k * 8);
                if (alphabetIndex >= 0 && alphabetIndex < alphabets.length()) {
                    recognizedText.append(alphabets.charAt(alphabetIndex));
                }
            }
            Log.d("Recognition result:", recognizedText.toString());
            if (recognizedText.length() > 0) {
                ocrResults.put(recognizedText.toString(), getRandomColor());
            }

            for (int j = 0; j < 4; j++) {
                Point[] vertices = new Point[4];
                boundingBox.points(vertices); // Retrieve the vertices of the rotated rectangle

                Point p1 = vertices[j];
                Point p2 = vertices[(j + 1) % 4]; // Wrap around to the first vertex for the last edge

                // Draw a line between the current vertex (p1) and the next vertex (p2)
                canvas.drawLine(
                        (float) p1.x * ratioWidth, (float) p1.y * ratioHeight,
                        (float) p2.x * ratioWidth, (float) p2.y * ratioHeight,
                        paint
                );
            }
        }

        return bitmapWithBoundingBoxes;
    }

    private MappedByteBuffer loadModelFile(Context context, String modelFile) throws IOException {
        FileInputStream inputStream = new FileInputStream(context.getAssets().openFd(modelFile).getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = context.getAssets().openFd(modelFile).getStartOffset();
        long declaredLength = context.getAssets().openFd(modelFile).getDeclaredLength();
        MappedByteBuffer retFile = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        fileChannel.close();
        inputStream.close();
        return retFile;
    }

    private Interpreter getInterpreter(Context context, String modelName, boolean useGpu) throws IOException {
        Interpreter.Options tfliteOptions = new Interpreter.Options();
        tfliteOptions.setNumThreads(numberThreads);

        if (useGpu) {
            gpuDelegate = new GpuDelegate();
            tfliteOptions.addDelegate(gpuDelegate);
        }

        return new Interpreter(loadModelFile(context, modelName), tfliteOptions);
    }

    private Interpreter getInterpreter(Context context, String modelName, boolean useGpu, Interpreter.Options tfliteOptions) throws IOException {
        tfliteOptions.setNumThreads(numberThreads);

        if (useGpu) {
            gpuDelegate = new GpuDelegate();
            tfliteOptions.addDelegate(gpuDelegate);
        }

        return new Interpreter(loadModelFile(context, modelName), tfliteOptions);
    }

    public int getRandomColor() {
        Random random = new Random();
        int r = (int) (255 * random.nextFloat());
        int g = (int) (255 * random.nextFloat());
        int b = (int) (255 * random.nextFloat());
        return Color.argb(128, r, g, b);
    }

    @Override
    public void close() {
        detectionInterpreter.close();
        recognitionInterpreter.close();
        if (gpuDelegate != null) {
            gpuDelegate.close();
        }
    }
}
