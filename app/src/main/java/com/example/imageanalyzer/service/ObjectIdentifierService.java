package com.example.imageanalyzer.service;

import com.example.imageanalyzer.beans.ImageData;
import com.example.imageanalyzer.database.DBHelper;

import java.util.List;

public class ObjectIdentifierService {

    private DBHelper dbHelper;

    public ObjectIdentifierService(DBHelper dbHelper){
        this.dbHelper = dbHelper;
    }
    public List<ImageData> fetchImages(String keyword){
        return dbHelper.fetchImageForObjectKeywords(keyword);
    }
}
