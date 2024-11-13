package com.example.imageanalyzer.utils;

import android.graphics.Bitmap;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
public class ImageProcessor {

    private static final int INPUT_SIZE = 640;  // Assuming your YOLOv5 model is trained with 640x640 images
    private static final int PIXEL_SIZE = 3;    // RGB

    public static ByteBuffer preprocessImage(Bitmap bitmap) {
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);

        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * PIXEL_SIZE);
        inputBuffer.order(ByteOrder.nativeOrder());

        int[] intValues = new int[INPUT_SIZE * INPUT_SIZE];
        scaledBitmap.getPixels(intValues, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE);

        int pixel = 0;
        for (int i = 0; i < INPUT_SIZE; ++i) {
            for (int j = 0; j < INPUT_SIZE; ++j) {
                final int val = intValues[pixel++];
                inputBuffer.putFloat((((val >> 16) & 0xFF) / 255.0f));  // R
                inputBuffer.putFloat((((val >> 8) & 0xFF) / 255.0f));   // G
                inputBuffer.putFloat(((val & 0xFF) / 255.0f));          // B
            }
        }

        return inputBuffer;
    }
}
