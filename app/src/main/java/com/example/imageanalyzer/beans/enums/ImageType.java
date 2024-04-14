package com.example.imageanalyzer.beans.enums;

public enum ImageType {
    JPG,PNG,UNKNOWN;

    public static ImageType getEnum(String value){
        if(value == null || value.equalsIgnoreCase("")){
            return ImageType.UNKNOWN;
        }

        if(value.toLowerCase().contains(ImageType.JPG.toString().toLowerCase())){
            return ImageType.JPG;
        }else if(value.toLowerCase().contains(ImageType.PNG.toString().toLowerCase())){
            return ImageType.PNG;
        }

        return ImageType.UNKNOWN;
    }
}
