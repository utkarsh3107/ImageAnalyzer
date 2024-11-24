package com.example.imageanalyzer.utils;

import com.example.imageanalyzer.beans.ImageData;

import java.util.ArrayList;
import java.util.List;

public class ImageDataManager {

    private static ImageDataManager instance;
    private List<ImageData> imageDataList;

    private ImageDataManager() {
        imageDataList = new ArrayList<>();
    }

    public static ImageDataManager getInstance() {
        if (instance == null) {
            instance = new ImageDataManager();
        }
        return instance;
    }

    public List<ImageData> getImageDataList() {
        return imageDataList;
    }

    public void setImageDataList(List<ImageData> imageDataList) {
        this.imageDataList = imageDataList;
    }

    public void updateImage(ImageData updatedImage) {
        for (int i = 0; i < imageDataList.size(); i++) {
            if (imageDataList.get(i).getImageId() == updatedImage.getImageId()) {
                imageDataList.set(i, updatedImage);
                break;
            }
        }
    }
}
