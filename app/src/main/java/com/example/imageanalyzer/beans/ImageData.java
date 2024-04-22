package com.example.imageanalyzer.beans;

import com.example.imageanalyzer.beans.enums.ImageType;

import java.util.List;

public class ImageData {
    private long imageId;
    private String imageName;
    private ImageType imageType;
    private String imagePath;
    private long imageSize;
    private int imageWidth;
    private int imageHeight;
    private long imageDateTaken;

    private ObjectsRecognition objectsRecognition;
    private ExifMetadata exifMetadata;

    private GPSMetadata gpsMetadata;

    public ImageData(long imageId, String imageName, String imagePath, long imageSize, int imageWidth, int imageHeight, long imageDateTaken, ImageType imageType) {
        this.imageId = imageId;
        this.imageName = imageName;
        this.imagePath = imagePath;
        this.imageSize = imageSize;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.imageDateTaken = imageDateTaken;
        this.imageType = imageType;
    }

    public long getImageId() {
        return imageId;
    }

    public String getImageName() {
        return imageName;
    }

    public ImageType getImageType() {
        return imageType;
    }

    public void setImageType(ImageType imageType) {
        this.imageType = imageType;
    }

    public String getImagePath() {
        return imagePath;
    }

    public long getImageSize() {
        return imageSize;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public long getImageDateTaken() {
        return imageDateTaken;
    }

    public ExifMetadata getExifMetadata() {
        return exifMetadata;
    }

    public void setExifMetadata(ExifMetadata exifMetadata) {
        this.exifMetadata = exifMetadata;
    }

    public GPSMetadata getGpsMetadata() {
        return gpsMetadata;
    }

    public void setGpsMetadata(GPSMetadata gpsMetadata) {
        this.gpsMetadata = gpsMetadata;
    }

    public ObjectsRecognition getObjectsRecognition() {
        return objectsRecognition;
    }

    public void setObjectsRecognition(ObjectsRecognition objectsRecognition) {
        this.objectsRecognition = objectsRecognition;
    }
    @Override
    public String toString() {
        return "ImageData{" +
                "imageId=" + imageId +
                ", imageName='" + imageName + '\'' +
                ", imageType=" + imageType +
                ", imagePath='" + imagePath + '\'' +
                ", imageSize=" + imageSize +
                ", imageWidth=" + imageWidth +
                ", imageHeight=" + imageHeight +
                ", imageDateTaken=" + imageDateTaken +
                ", objectsRecognition=" + objectsRecognition +
                ", exifMetadata=" + exifMetadata +
                ", gpsMetadata=" + gpsMetadata +
                '}';
    }
}
