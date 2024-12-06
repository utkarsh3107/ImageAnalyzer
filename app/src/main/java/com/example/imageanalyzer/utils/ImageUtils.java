package com.example.imageanalyzer.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.icc.IccDirectory;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;

import com.example.imageanalyzer.beans.GPSMetadata;
import com.example.imageanalyzer.beans.ICCProfileMetadata;
import com.example.imageanalyzer.beans.ImageData;
import com.example.imageanalyzer.beans.ExifMetadata;
import com.example.imageanalyzer.beans.ObjectsRecognition;
import com.example.imageanalyzer.beans.OverviewActivityPair;
import com.example.imageanalyzer.beans.Recognition;
import com.example.imageanalyzer.beans.enums.ImageType;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ImageUtils {

    public static List<ImageData> getAllImageNames(Context context) {
        List<ImageData> imageDataList = new ArrayList<>();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {  MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.DATE_TAKEN };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                long imageId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                String imageName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                long imageSize = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
                int imageWidth = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH));
                int imageHeight = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT));
                long imageDateTaken = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN));
                File imageFile = new File(imagePath);
                ImageType imageType = getImageType(imageFile);
                ImageData imageData = new ImageData(imageId, imageName, imagePath, imageSize, imageWidth, imageHeight, imageDateTaken, imageType);
                imageData.setGpsMetadata(fetchGPSData(imageFile));
                imageData.setExifMetadata(fetchExifMetadata(imageFile));
                imageDataList.add(imageData);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return imageDataList;
    }

    public static ExifMetadata fetchExifMetadata(File imageFile){
        ExifMetadata result = null;

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
            ExifSubIFDDirectory exifDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (exifDirectory != null) {
                result =  new ExifMetadata(exifDirectory.getString(ExifSubIFDDirectory.TAG_MODEL),exifDirectory.getString(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL),exifDirectory.getString(ExifSubIFDDirectory.TAG_EXPOSURE_TIME),exifDirectory.getString(ExifSubIFDDirectory.TAG_FNUMBER),exifDirectory.getString(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT));
                Log.d(Constants.IMAGE_UTILS_CLASS, "Camera Model: " + exifDirectory.getString(ExifSubIFDDirectory.TAG_MODEL));
                Log.d(Constants.IMAGE_UTILS_CLASS, "Date/Time Original: " + exifDirectory.getString(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL));
                Log.d(Constants.IMAGE_UTILS_CLASS, "Exposure Time: " + exifDirectory.getString(ExifSubIFDDirectory.TAG_EXPOSURE_TIME));
                Log.d(Constants.IMAGE_UTILS_CLASS, "Aperture: " + exifDirectory.getString(ExifSubIFDDirectory.TAG_FNUMBER));
                Log.d(Constants.IMAGE_UTILS_CLASS, "ISO Speed Rating: " + exifDirectory.getString(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT));
            }
        }catch(Exception ex){
            Log.e(Constants.IMAGE_UTILS_CLASS, "Error determining exif metadata: " + ex.getMessage());
        }
        return result;
    }

    public static ICCProfileMetadata fetchICCProfileMetadata(File imageFile){
        ICCProfileMetadata result = null;
        try {
             /*
            Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
            IccDirectory iccDirectory = metadata.getFirstDirectoryOfType(Iccp);
            if (iccDirectory != null) {
                byte[] iccProfileData = iccDirectory.getByteArray(IccDirectorY.TAG_;
                if (iccProfileData != null) {
                    Log.d("Image Metadata", "ICC Profile Length: " + iccProfileData.length + " bytes");
                }
            }*/
        }catch(Exception ex){
            Log.e(Constants.IMAGE_UTILS_CLASS, "Error determining iccprofile metadata: " + ex.getMessage());
        }
        return result;
    }

    public static GPSMetadata fetchGPSData(File imageFile){
        GPSMetadata result = null;

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(imageFile);

            GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            if (gpsDirectory != null) {
                String latitudeRef = gpsDirectory.getString(GpsDirectory.TAG_LATITUDE_REF);
                double latitude = gpsDirectory.getDouble(GpsDirectory.TAG_LATITUDE);
                String longitudeRef = gpsDirectory.getString(GpsDirectory.TAG_LONGITUDE);
                double longitude = gpsDirectory.getDouble(GpsDirectory.TAG_DEST_LONGITUDE_REF);
                Log.i(Constants.IMAGE_UTILS_CLASS, "GPS Latitude: " + latitudeRef + " " + latitude);
                Log.i(Constants.IMAGE_UTILS_CLASS, "GPS Longitude: " + longitudeRef + " " + longitude);
                result = new GPSMetadata(latitudeRef, latitude, longitudeRef, longitude);
            }

        }catch(Exception ex){
            Log.e(Constants.IMAGE_UTILS_CLASS, "Error determining gps metadata: " + ex.getMessage());
        }
        return result;
    }

    public static long getImageSize(Context context, String imageName) {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Images.Media.SIZE };
        Cursor cursor = context.getContentResolver().query(uri, projection,
                MediaStore.Images.Media.DISPLAY_NAME + "=?", new String[]{imageName}, null);
        long size = -1;
        if (cursor != null && cursor.moveToFirst()) {
            size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
            cursor.close();
        }
        return size;
    }

    private static ImageType getImageType(File imageFile) {
        ImageType result = ImageType.UNKNOWN;
        try {
            String mimeType = java.nio.file.Files.probeContentType(imageFile.toPath());
            Log.i(Constants.IMAGE_UTILS_CLASS, "Actual image type: " + mimeType);
            result = ImageType.getEnum(mimeType);
        } catch (java.io.IOException e) {
            Log.e(Constants.IMAGE_UTILS_CLASS, "Error determining image type: " + e.getMessage());
        }
        return result;
    }

    public static Uri convertToUri(String imagePath){
        File imageFile = new File(imagePath);
        return Uri.fromFile(imageFile);
    }

    private static Bitmap convertToBitmap(Context context, Uri imageUri){
        Bitmap result = null;
        try{
            result = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
        }catch(Exception ex){
            Log.i(Constants.IMAGE_UTILS_CLASS,"Error occured while converting to bitmap", ex);
        }
        return result;
    }

    public static Bitmap convertToBitmap(Context context, String imagePath){
        Uri uri = convertToUri(imagePath);
        return convertToBitmap(context, uri);
    }

    public static void initObjects(ImageData imageData, List<Recognition> objectsFound){
        if(imageData.getObjectsRecognition() == null){
            imageData.setObjectsRecognition(new ObjectsRecognition());
        }

        for(Recognition eachRecognition : objectsFound){
            imageData.getObjectsRecognition().addObject(eachRecognition.getLabelName().toLowerCase());
        }

        Log.i(Constants.IMAGE_UTILS_CLASS, "Found object: " + objectsFound + " for image: " + imageData.getImageName());
    }

    private static Map<String, List<ImageData>> findObjectFrequency(List<ImageData> allImages) {
        Map<String, List<ImageData>> objectAllocatationMap = new HashMap<>();

        for (ImageData eachImg : allImages) {
            if (eachImg.getObjectsRecognition() != null && eachImg.getObjectsRecognition().getObjectsDetected() != null) {
                for (String eachObj : eachImg.getObjectsRecognition().getObjectsDetected()) {
                    objectAllocatationMap.putIfAbsent(eachObj, new ArrayList<>());
                    Objects.requireNonNull(objectAllocatationMap.get(eachObj)).add(eachImg);
                }
            }
        }

        return objectAllocatationMap;
    }

    public static List<OverviewActivityPair> getObjectsForScreens(List<ImageData> allImages, int objectLimit, int imageLimit){
        Map<String, List<ImageData>> objectFrequencyMap = findObjectFrequency(allImages);

        List<Map.Entry<String, List<ImageData>>> sortedEntries = new ArrayList<>(objectFrequencyMap.entrySet());
        sortedEntries.sort((key, value) -> Integer.compare(value.getValue().size(), key.getValue().size()));

        List<OverviewActivityPair> result = new ArrayList<>();
        int objectCount = 0;

        for (Map.Entry<String, List<ImageData>> entry : sortedEntries) {
            if (objectLimit > 0 && objectCount >= objectLimit) break;

            String objectName = entry.getKey();
            List<ImageData> allImagesForObject = entry.getValue();

            List<ImageData> extractedImageList;
            if (imageLimit > 0 && imageLimit < allImagesForObject.size()) {
                extractedImageList = allImagesForObject.subList(0, imageLimit);
            } else {
                extractedImageList = new ArrayList<>(allImagesForObject);
            }

            result.add(new OverviewActivityPair(objectName, extractedImageList));

            objectCount++;
        }

        return result;
    }

    public static String getFormattedImageName(String objectName){
        return objectName.substring(0, 1).toUpperCase() + objectName.substring(1).toLowerCase();
    }
}

