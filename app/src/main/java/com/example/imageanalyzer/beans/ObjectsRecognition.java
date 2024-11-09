package com.example.imageanalyzer.beans;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class ObjectsRecognition implements Serializable {

    private Set<String> objectsDetected;

    public Set<String> getObjectsDetected() {
        return objectsDetected;
    }

    public void setObjectsDetected(Set<String> objectsDetected) {
        this.objectsDetected = objectsDetected;
    }

    public void addObject(String object){
        if(objectsDetected == null){
            this.objectsDetected = new HashSet<>();
        }
        this.objectsDetected.add(object);
    }

    @Override
    public String toString() {
        return "ObjectsRecognition{" +
                "objectsDetected=" + objectsDetected +
                '}';
    }
}
