package com.example.imageanalyzer.beans;

import java.io.Serializable;

public class TextDetected implements Serializable {

    private String imageText;

    public TextDetected(){

    }

    public TextDetected(String textDetected){
        imageText = textDetected;
    }
    public String getImageText() {
        return imageText;
    }

    public void setImageText(String imageText) {
        this.imageText = imageText;
    }

    @Override
    public String toString() {
        return "TextDetected{" +
                "imageText='" + imageText + '\'' +
                '}';
    }
}