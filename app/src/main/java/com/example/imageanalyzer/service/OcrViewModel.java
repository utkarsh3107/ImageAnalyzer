package com.example.imageanalyzer.service;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.imageanalyzer.ml.models.OcrModelExecutor;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.File;
import java.util.ArrayList;

public class OcrViewModel extends AndroidViewModel {
    private static final String TAG = "OcrViewModel";

    private OcrModelExecutor modelExecutor; // Custom TensorFlow Lite executor
    private MutableLiveData<ArrayList<String>> currentList = new MutableLiveData<>();
    private ArrayList<String> _currentList = new ArrayList<>();

    private long startTime;
    private long inferenceTime;

    public OcrViewModel(@NonNull Application application) {
        super(application);
        modelExecutor = new OcrModelExecutor(application.getApplicationContext(), false);
        currentList.setValue(_currentList);
    }

    public LiveData<ArrayList<String>> getCurrentList() {
        return currentList;
    }

    public void processImage(String imagePath) {
        try {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                //processWithMlKit(bitmap);
                processWithModelExecutor(bitmap);
            } else {
                Log.e(TAG, "Image file does not exist at path: " + imagePath);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing image", e);
        }
    }

    private void processWithMlKit(Bitmap bitmap) {
        TextRecognizer recognizer = TextRecognition.getClient(new com.google.mlkit.vision.text.latin.TextRecognizerOptions.Builder().build());
        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);

        recognizer.process(inputImage)
                .addOnSuccessListener(visionText -> {
                    _currentList.add(visionText.getText());
                    currentList.postValue(_currentList);
                    Log.d(TAG, "ML Kit Text Recognition Result: " + visionText.getText());
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error during ML Kit text recognition", e));
    }

    private void processWithModelExecutor(Bitmap bitmap) {
        long start = SystemClock.uptimeMillis();
        String resultString = modelExecutor.predictAndDecode(bitmap); // Updated to handle long[][] output
        long end = SystemClock.uptimeMillis();

        // Convert result to a string representation (example)
        //StringBuilder resultString = new StringBuilder();
        //for (long value : result[0]) {
            //resultString.append(value).append(" ");
        //}

        Log.d(TAG, "OcrViewModel: decoded string: " + resultString );
        _currentList.add(resultString.trim());
        currentList.postValue(_currentList);

        inferenceTime = end - start;
        Log.d(TAG, "Inference Time: " + inferenceTime + "ms");
    }
}
