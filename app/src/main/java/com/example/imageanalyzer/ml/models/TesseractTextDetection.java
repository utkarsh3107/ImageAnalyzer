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
    }

    public String performOCR(ImageData image) {
        String result = null;
        try{
            Bitmap bitmap = BitmapFactory.decodeFile(image.getImagePath());
            //Bitmap bitmap = preProcessImage1(image);
            String text = performOCR(bitmap);
            image.setImgText(new TextDetected(text));
            Log.i("TesseractTextDetection", "For image: "+ image.getImageName() + " text detected: "+ text);
            tessBaseAPI.setImage(bitmap);
            result =  tessBaseAPI.getUTF8Text();
        }catch (Exception ex){
            Log.e("TesseractTextDetection", "Error loading model", ex);
        }
       return result;
    }

    public Bitmap preProcessImage(ImageData image){
        Bitmap decodedBitmap = BitmapFactory.decodeFile(image.getImagePath());
        Bitmap grayscaleBitmap = toGrayscale(decodedBitmap);
        Bitmap denoisedBitmap = denoiseImage(grayscaleBitmap);
        Bitmap thresholdedBitmap = adaptiveThreshold(denoisedBitmap);
        Bitmap binarize = binarize(thresholdedBitmap);
        Bitmap detectEdges = detectEdges(binarize);
        Bitmap resizeBitmap = resizeBitmap(detectEdges, 800, 800);
        return resizeBitmap;
    }

    public Bitmap preProcessImage1(ImageData image){
        Bitmap decodedBitmap = BitmapFactory.decodeFile(image.getImagePath());
        Bitmap grayscaleBitmap = toGrayscale(decodedBitmap);
        return grayscaleBitmap;
    }

    private Bitmap toGrayscale(Bitmap bitmap) {
        Bitmap grayscaleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(grayscaleBitmap);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(filter);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return grayscaleBitmap;
    }

    private Bitmap binarize(Bitmap bitmap) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.threshold(mat, mat, 128, 255, Imgproc.THRESH_BINARY);
        Bitmap binarizedBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, binarizedBitmap);
        return binarizedBitmap;
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float ratio = Math.min((float) maxWidth / width, (float) maxHeight / height);
        int finalWidth = Math.round(width * ratio);
        int finalHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true);
    }

    private Bitmap denoiseImage(Bitmap bitmap) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY); // Convert to grayscale
        Imgproc.GaussianBlur(mat, mat, new Size(5, 5), 0); // Apply Gaussian Blur
        Bitmap processedBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, processedBitmap);
        return processedBitmap;
    }

    private Bitmap detectEdges(Bitmap bitmap) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY); // Convert to grayscale
        Imgproc.Canny(mat, mat, 50, 150); // Apply Canny edge detection
        Bitmap edgeBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, edgeBitmap);
        return edgeBitmap;
    }

    private Bitmap adaptiveThreshold(Bitmap bitmap) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY); // Convert to grayscale
        Imgproc.adaptiveThreshold(mat, mat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
                Imgproc.THRESH_BINARY, 11, 2);
        Bitmap thresholdedBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, thresholdedBitmap);
        return thresholdedBitmap;
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
