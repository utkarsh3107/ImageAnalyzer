package com.example.imageanalyzer.ml.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.mlkit.common.model.RemoteModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class MLKitOCRExecutor {

    public MLKitOCRExecutor(){
        TextRecognition.getClient(new TextRecognizerOptions.Builder().build())
                .process(InputImage.fromBitmap(Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888), 0))
                .addOnSuccessListener(visionText -> Log.d("MLKit", "Model preloaded successfully."))
                .addOnFailureListener(e -> Log.e("MLKit", "Error preloading model", e));
    }

    public void recognizeTextFromImagePath(String imagePath) {
        try {
            // Load image from the provided path
            InputImage image = loadImageFromPath(imagePath);

            // Initialize ML Kit Text Recognizer
            TextRecognition.getClient(new TextRecognizerOptions.Builder().build())
                    .process(image)
                    .addOnSuccessListener(visionText -> {
                        // Log the entire recognized text
                        Log.d("MLKit", "Recognized Text: " + visionText.getText());

                        // Log individual text blocks and lines
                        for (Text.TextBlock block : visionText.getTextBlocks()) {
                            Log.d("MLKit", "Text Block: " + block.getText());
                            for (Text.Line line : block.getLines()) {
                                Log.d("MLKit", "Line: " + line.getText());
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Log any errors during text recognition
                        Log.e("MLKit", "Text recognition failed", e);
                    });
        } catch (Exception e) {
            // Handle errors while loading the image
            Log.e("MLKit", "Error processing the image from path: " + imagePath, e);
        }
    }
    private InputImage loadImageFromPath(String filePath) throws Exception {
        // Decode the image file into a Bitmap
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);

        if (bitmap == null) {
            throw new Exception("Unable to decode image from the given path: " + filePath);
        }

        // Convert the Bitmap into an ML Kit InputImage
        return InputImage.fromBitmap(bitmap, 0); // Rotation is 0 degrees
    }

}
