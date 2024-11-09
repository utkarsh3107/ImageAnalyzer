package com.example.imageanalyzer.beans;

import java.io.Serializable;

public class ExifMetadata implements Serializable {

    private String cameraModel;
    private String dateTimeOriginal;
    private String exposureTime;
    private String aperture;
    private String isoSpeedRating;

    public ExifMetadata(String cameraModel, String dateTimeOriginal, String exposureTime, String aperture, String isoSpeedRating) {
        this.cameraModel = cameraModel;
        this.dateTimeOriginal = dateTimeOriginal;
        this.exposureTime = exposureTime;
        this.aperture = aperture;
        this.isoSpeedRating = isoSpeedRating;
    }

    public String getCameraModel() {
        return cameraModel;
    }

    public void setCameraModel(String cameraModel) {
        this.cameraModel = cameraModel;
    }

    public String getDateTimeOriginal() {
        return dateTimeOriginal;
    }

    public void setDateTimeOriginal(String dateTimeOriginal) {
        this.dateTimeOriginal = dateTimeOriginal;
    }

    public String getExposureTime() {
        return exposureTime;
    }

    public void setExposureTime(String exposureTime) {
        this.exposureTime = exposureTime;
    }

    public String getAperture() {
        return aperture;
    }

    public void setAperture(String aperture) {
        this.aperture = aperture;
    }

    public String getIsoSpeedRating() {
        return isoSpeedRating;
    }

    public void setIsoSpeedRating(String isoSpeedRating) {
        this.isoSpeedRating = isoSpeedRating;
    }

    @Override
    public String toString() {
        return "ImageMetadata{" +
                "cameraModel='" + cameraModel + '\'' +
                ", dateTimeOriginal='" + dateTimeOriginal + '\'' +
                ", exposureTime='" + exposureTime + '\'' +
                ", aperture='" + aperture + '\'' +
                ", isoSpeedRating='" + isoSpeedRating + '\'' +
                '}';
    }
}
