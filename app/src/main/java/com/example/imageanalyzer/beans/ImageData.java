package com.example.imageanalyzer.beans;

public class ImageData {
    private long imageId;
    private String imageName;
    private String imagePath;
    private long imageSize;
    private int imageWidth;
    private int imageHeight;
    private long imageDateTaken;

    public ImageData(long imageId, String imageName, String imagePath, long imageSize, int imageWidth, int imageHeight, long imageDateTaken) {
        this.imageId = imageId;
        this.imageName = imageName;
        this.imagePath = imagePath;
        this.imageSize = imageSize;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.imageDateTaken = imageDateTaken;
    }

    public long getImageId() {
        return imageId;
    }

    public String getImageName() {
        return imageName;
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

    @Override
    public String toString() {
        return "ImageData{" +
                "imageId=" + imageId +
                ", imageName='" + imageName + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", imageSize=" + imageSize +
                ", imageWidth=" + imageWidth +
                ", imageHeight=" + imageHeight +
                ", imageDateTaken=" + imageDateTaken +
                '}';
    }
}
