package com.example.imageanalyzer.ml.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.SystemClock;
import android.util.Log;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OcrModelExecutor {

    private List<String> characterSet;

    private static final String TAG = "OcrMExec";
    private static final int CONTENT_IMAGE_WIDTH = 200;
    private static final int CONTENT_IMAGE_HEIGHT = 31;
    private static final String OCR_MODEL = "ocr_dr.tflite";

    private int numberThreads = 7;
    private long fullExecutionTime = 0L;
    private final Interpreter interpreterPredict;

    public OcrModelExecutor(Context context, boolean useGPU) {
        interpreterPredict = getInterpreter(context, OCR_MODEL, useGPU);
        characterSet = loadLabels(context, "alphabets.txt");
    }

    private List<String> loadLabels(Context context, String fileName) {
        List<String> labels = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(context.getAssets().open(fileName)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                labels.add(line.trim());
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading labels file: " + fileName, e);
        }
        return labels;
    }

    private Interpreter getInterpreter(Context context, String modelName, boolean useGPU) {
        try {
            MappedByteBuffer model = loadModelFile(context, modelName);
            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(numberThreads);
            if (useGPU) {
                // Configure GPU delegate if needed
            }
            return new Interpreter(model, options);
        } catch (IOException e) {
            Log.e(TAG, "Error loading model", e);
            throw new RuntimeException(e);
        }
    }

    private MappedByteBuffer loadModelFile(Context context, String modelName) throws IOException {
        FileInputStream inputStream = new FileInputStream(context.getAssets().openFd(modelName).getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = context.getAssets().openFd(modelName).getStartOffset();
        long declaredLength = context.getAssets().openFd(modelName).getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public long[][] predict(Bitmap bitmap) {
        // Resize the bitmap to match model input dimensions
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, CONTENT_IMAGE_WIDTH, CONTENT_IMAGE_HEIGHT, true);

        // Convert the resized bitmap to ByteBuffer
        ByteBuffer inputBuffer = convertBitmapToByteBuffer(resizedBitmap);

        // Adjust output array to match INT64 output type
        long[][] result = new long[1][48]; // Output shape is 1x48 based on the model's output

        long startTime = SystemClock.uptimeMillis();
        interpreterPredict.run(inputBuffer, result); // Pass the correct type of output array
        fullExecutionTime = SystemClock.uptimeMillis() - startTime;

        return result;
    }

    public String predictAndDecode(Bitmap bitmap) {
        // Resize the bitmap to match model input dimensions
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, CONTENT_IMAGE_WIDTH, CONTENT_IMAGE_HEIGHT, true);

        // Convert the resized bitmap to ByteBuffer
        ByteBuffer inputBuffer = convertBitmapToByteBuffer(resizedBitmap);

        // Prepare the output array
        long[][] output = new long[1][48]; // Adjust output shape to match the model

        // Run inference
        long startTime = SystemClock.uptimeMillis();
        interpreterPredict.run(inputBuffer, output);
        fullExecutionTime = SystemClock.uptimeMillis() - startTime;

        // Decode the output into text
        return decodeOutput(output);
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        // Allocate buffer for a single-channel image
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * CONTENT_IMAGE_WIDTH * CONTENT_IMAGE_HEIGHT);
        byteBuffer.order(ByteOrder.nativeOrder());

        int[] intValues = new int[CONTENT_IMAGE_WIDTH * CONTENT_IMAGE_HEIGHT];
        bitmap.getPixels(intValues, 0, CONTENT_IMAGE_WIDTH, 0, 0, CONTENT_IMAGE_WIDTH, CONTENT_IMAGE_HEIGHT);

        for (int pixelValue : intValues) {
            // Extract grayscale value from ARGB pixel and normalize to [0, 1]
            int r = (pixelValue >> 16) & 0xFF;
            int g = (pixelValue >> 8) & 0xFF;
            int b = pixelValue & 0xFF;
            float grayscaleValue = (r + g + b) / 3.0f / 255.0f;

            byteBuffer.putFloat(grayscaleValue);
        }

        return byteBuffer;
    }

    public long getFullExecutionTime() {
        return fullExecutionTime;
    }

    public String decodeOutput(long[][] output) {
        StringBuilder decodedText = new StringBuilder();
        for(long i = 0; i < output.length; i++){
            for (long index : output[(int)i]) {
                if (index >= 0 && index < characterSet.size()) {
                    decodedText.append(characterSet.get((int) index));
                } else {
                    //Log.w(TAG, "Invalid character index: " + index);
                }
            }
        }

        return decodedText.toString();
    }
}
