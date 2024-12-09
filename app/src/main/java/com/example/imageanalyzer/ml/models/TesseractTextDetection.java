package com.example.imageanalyzer.ml.models;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;

import com.example.imageanalyzer.beans.ImageData;
import com.example.imageanalyzer.beans.TextDetected;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TesseractTextDetection {

    private Context context;

    private TessBaseAPI tessBaseAPI;
    public TesseractTextDetection(Context context){
        this.context = context;
        initTesseract();
    }

    public void initTesseract() {
        String tessPath = context.getExternalFilesDir(null) + "/tesseract/";
        tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.init(tessPath, "eng");
        tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
        tessBaseAPI.setVariable("user_words_suffix", "user-words");
    }

    public String performOCR(ImageData image) {
        String result = null;
        try{
            Bitmap bitmap = BitmapFactory.decodeFile(image.getImagePath());
            //Bitmap bitmap = preProcessImage1(image);
            String text = performOCR(bitmap);
            if(text != null){
                text = text.replace("\n", "").replace("\r", "");
            }
            image.setImgText(new TextDetected(text));
            Log.i("TesseractTextDetection", "For image: "+ image.getImageName() + " text detected: "+ text);
            tessBaseAPI.setImage(bitmap);
            result =  tessBaseAPI.getUTF8Text();
        }catch (Exception ex){
            Log.e("TesseractTextDetection", "Error loading model", ex);
        }
        return result;
    }

    private String performOCR(Bitmap bitmap) {
        tessBaseAPI.setImage(bitmap);
        return tessBaseAPI.getUTF8Text();
    }

    public void release() {
        if (tessBaseAPI != null) {
            tessBaseAPI.recycle();
        }
    }

    public void copyTessDataFiles() {
        try {
            String tessPath = context.getExternalFilesDir(null) + "/tesseract/tessdata/";
            File dir = new File(tessPath);
            if (!dir.exists()) dir.mkdirs();

            AssetManager assetManager = context.getAssets();
            for (String file : assetManager.list("tessdata")) {
                InputStream in = assetManager.open("tessdata/" + file);
                OutputStream out = new FileOutputStream(tessPath + file);

                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}