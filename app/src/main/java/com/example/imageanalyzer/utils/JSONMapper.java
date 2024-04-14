package com.example.imageanalyzer.utils;

import com.example.imageanalyzer.beans.ImageData;
import com.google.gson.Gson;

public class JSONMapper {
    private static final Gson gson = new Gson();

    // Convert Object to JSON string
    public static String toJSON(Object object){
        return gson.toJson(object);
    }

    public static <T> T toObject(String json, Class<T> tClass) {
        return gson.fromJson(json, tClass);
    }
}
