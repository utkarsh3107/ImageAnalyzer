package com.example.imageanalyzer.beans;

public class ImageOverviewPair {

    private String objectName;
    private ImageData imageData;

    public ImageOverviewPair(String objectName, ImageData imageData) {
        this.objectName = objectName;
        this.imageData = imageData;
    }

    public String getObjectName() {
        return objectName;
    }

    public ImageData getImageData() {
        return imageData;
    }

    @Override
    public String toString() {
        return "ImageOverviewPair{" +
                "objectName='" + objectName + '\'' +
                ", imageData=" + imageData +
                '}';
    }
}
