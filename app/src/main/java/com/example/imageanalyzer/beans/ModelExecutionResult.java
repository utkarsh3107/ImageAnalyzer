package com.example.imageanalyzer.beans;

import android.graphics.Bitmap;

import java.util.Map;

public class ModelExecutionResult {

    private String executionLog;
    private Bitmap bitmapResult;
    private Map<String, Integer> itemsFound;

    public ModelExecutionResult(Bitmap bitmapResult, String executionLog,  Map<String, Integer> itemsFound){
        this.bitmapResult = bitmapResult;
        this.executionLog = executionLog;
        this.itemsFound = itemsFound;
    }
    public String getExecutionLog() {
        return executionLog;
    }

    public void setExecutionLog(String executionLog) {
        this.executionLog = executionLog;
    }

    public Bitmap getBitmapResult() {
        return bitmapResult;
    }

    public void setBitmapResult(Bitmap bitmapResult) {
        this.bitmapResult = bitmapResult;
    }

    public Map<String, Integer> getItemsFound() {
        return itemsFound;
    }

    public void setItemsFound(Map<String, Integer> itemsFound) {
        this.itemsFound = itemsFound;
    }

    @Override
    public String toString() {
        return "ModelExecutionResult{" +
                "executionLog='" + executionLog + '\'' +
                ", bitmapResult=" + bitmapResult +
                ", itemsFound=" + itemsFound +
                '}';
    }
}
