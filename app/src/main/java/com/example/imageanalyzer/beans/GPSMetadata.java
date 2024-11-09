package com.example.imageanalyzer.beans;

import java.io.Serializable;

public class GPSMetadata implements Serializable {

    private String latitudeRef;
    private double latitude;
    private String longitudeRef;
    private double longitude;

    public GPSMetadata(String latitudeRef, double latitude, String longitudeRef, double longitude) {
        this.latitudeRef = latitudeRef;
        this.latitude = latitude;
        this.longitudeRef = longitudeRef;
        this.longitude = longitude;
    }

    public String getLatitudeRef() {
        return latitudeRef;
    }

    public void setLatitudeRef(String latitudeRef) {
        this.latitudeRef = latitudeRef;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getLongitudeRef() {
        return longitudeRef;
    }

    public void setLongitudeRef(String longitudeRef) {
        this.longitudeRef = longitudeRef;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "GPSMetadata{" +
                "latitudeRef='" + latitudeRef + '\'' +
                ", latitude=" + latitude +
                ", longitudeRef='" + longitudeRef + '\'' +
                ", longitude=" + longitude +
                '}';
    }
}
