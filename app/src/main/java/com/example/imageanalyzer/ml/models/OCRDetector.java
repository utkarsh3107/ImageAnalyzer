package com.example.imageanalyzer.ml.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.util.Size;

import com.example.imageanalyzer.ml.TextDetectionFp16;
import com.example.imageanalyzer.utils.ImageUtils;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import org.tensorflow.lite.DataType;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class OCRDetector {

    private final Size INPNUT_SIZE = new Size(320, 320);
    private final int[] OUTPUT_SIZE = new int[]{1, 6300, 85};
    private Interpreter textDetector;
    private final Context context;
    public OCRDetector(Context context){
        Log.d("OCRDetector", "Loading model");
        this.context = context;
        Interpreter.Options options = new Interpreter.Options();
        try{
            ByteBuffer tfliteModel = FileUtil.loadMappedFile(context, "text-detection-fp16.tflite");
            textDetector = new Interpreter(tfliteModel, options);
            Log.i("OCRDetector", "Loading yolo_v5 model successful");
        }catch(Exception ex){
            Log.i("OCRDetector", "Error initialzing class ", ex);
        }
    }

    public Bitmap detect(String path){
        try{
            Bitmap inputBitmap = ImageUtils.convertToBitmap(this.context, path);
            inputBitmap = Bitmap.createScaledBitmap(inputBitmap, 320, 320, false);

            TextDetectionFp16 model = TextDetectionFp16.newInstance(context);


            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 320, 320, 3}, DataType.FLOAT32);
            inputFeature0.loadBuffer(bitmapToByteBuffer(inputBitmap));

            TextDetectionFp16.Outputs outputs = model.process(inputFeature0);
            TensorBuffer boundingBoxes = outputs.getOutputFeature0AsTensorBuffer();
            TensorBuffer outputFeature0 = outputs.getOutputFeature1AsTensorBuffer();

            model.close();
            Bitmap outputBitmap = inputBitmap.copy(inputBitmap.getConfig(), true);

            //Bitmap resizedBitmap = Bitmap.createScaledBitmap(inputBitmap, 320, 320, false);

            Canvas canvas = new Canvas(outputBitmap);
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);

            int imageWidth = inputBitmap.getWidth();
            System.out.println("imageWidth:" + imageWidth);
            int imageHeight = inputBitmap.getHeight();
            System.out.println("imageHeight:" + imageHeight);


            for (int y = 0; y < 80; y++) {
                for (int x = 0; x < 80; x++) {

                    int index = y * 80 + x;

                    try {

                        float boxScore = outputFeature0.getFloatArray()[index];
                        float top = boundingBoxes.getFloatValue(index) * imageHeight;
                        float left = boundingBoxes.getFloatValue(index + 1) * imageWidth;
                        float bottom = boundingBoxes.getFloatValue(index + 2) * imageHeight;
                        float right = boundingBoxes.getFloatValue(index + 3) * imageWidth;


                        if (boxScore > 250) {
                            System.out.println("(" + left + "," + top + "," + right + "," + bottom + "): Score: " + boxScore);

                            canvas.drawRect(left, top, right, bottom, paint);
                        }
                    } catch (IndexOutOfBoundsException ex) {
                        ex.getMessage();
                    }
                }
            }

            return outputBitmap;
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public ByteBuffer bitmapToByteBuffer(Bitmap bitmap) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 320, 320, false);

        int batchSize = 1;
        int byteSize = batchSize * 320 * 320 * 3 * 4; // 4 bytes per float
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(byteSize).order(ByteOrder.nativeOrder());

        int[] intValues = new int[320 * 320];
        resizedBitmap.getPixels(intValues, 0, 320, 0, 0, 320, 320);

        for (int i = 0; i < 320 * 320; ++i) {
            int pixelValue = intValues[i];

            inputBuffer.putFloat(((pixelValue >> 16) & 0xFF) / 255.0f); // R
            inputBuffer.putFloat(((pixelValue >> 8) & 0xFF) / 255.0f); // G
            inputBuffer.putFloat((pixelValue & 0xFF) / 255.0f); // B
        }

        return inputBuffer;
    }
}
