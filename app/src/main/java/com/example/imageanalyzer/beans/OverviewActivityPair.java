package com.example.imageanalyzer.beans;

import java.util.List;

public class OverviewActivityPair {

    private String objectName;
    private List<ImageData> imageList;

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public List<ImageData> getImageList() {
        return imageList;
    }

    public void setImageList(List<ImageData> imageList) {
        this.imageList = imageList;
    }

    @Override
    public String toString() {
        return "OverviewActivityPair{" +
                "objectName='" + objectName + '\'' +
                ", imageList=" + imageList +
                '}';
    }
}
