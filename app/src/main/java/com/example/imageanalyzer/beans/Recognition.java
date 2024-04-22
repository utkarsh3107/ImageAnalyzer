package com.example.imageanalyzer.beans;

import androidx.annotation.NonNull;

public class Recognition {
    private Integer labelId;
    private String labelName;
    private Float labelScore;
    private Float confidence;

    public Recognition(int labelId,  String labelName, Float labelScore, Float confidence) {
        this.labelId = labelId;
        this.labelScore = labelScore;
        this.labelName = labelName;
        this.confidence = confidence;
    }

    public Integer getLabelId() {
        return labelId;
    }

    public String getLabelName() {
        return labelName;
    }

    public Float getLabelScore() {
        return labelScore;
    }

    public Float getConfidence() {
        return confidence;
    }

    public void setLabelName(String labelName) {
        this.labelName = labelName;
    }

    public void setLabelId(int labelId) {
        this.labelId = labelId;
    }

    public void setLabelScore(Float labelScore) {
        this.labelScore = labelScore;
    }

    public void setConfidence(Float confidence) {
        this.confidence = confidence;
    }

    @NonNull
    @Override
    public String toString() {
        return "Recognition{" +
                "labelId=" + labelId +
                ", labelName='" + labelName + '\'' +
                ", labelScore=" + labelScore +
                ", confidence=" + confidence +
                '}';
    }
}
