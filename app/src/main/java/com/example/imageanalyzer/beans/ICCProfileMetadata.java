package com.example.imageanalyzer.beans;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class ICCProfileMetadata {

    private byte[] iccProfileData;

    public ICCProfileMetadata(byte[] iccProfileData) {
        this.iccProfileData = iccProfileData;
    }

    public byte[] getIccProfileData() {
        return iccProfileData;
    }

    public void setIccProfileData(byte[] iccProfileData) {
        this.iccProfileData = iccProfileData;
    }

    @NonNull
    @Override
    public String toString() {
        return "IccProfileMetadata{" +
                "iccProfileData=" + Arrays.toString(iccProfileData) +
                '}';
    }
}
