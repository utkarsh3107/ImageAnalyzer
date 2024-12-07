package com.example.imageanalyzer.beans;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class ObjectsRecognition implements Serializable {

    private Set<String> objectsDetected;

    public ObjectsRecognition(){
        objectsDetected = new HashSet<>();
    }

    public Set<String> getObjectsDetected() {
        return objectsDetected;
    }

    public void setObjectsDetected(Set<String> objectsDetected) {
        this.objectsDetected = objectsDetected;
    }

    public void addObject(String object){
        this.objectsDetected.add(object);
    }

    @Override
    public String toString() {
        return "ObjectsRecognition{" +
                "objectsDetected=" + objectsDetected +
                '}';
    }
}
