package com.example.imageanalyzer.search;

import com.example.imageanalyzer.beans.ImageData;
import com.example.imageanalyzer.database.DBHelper;

import java.util.List;
import java.util.Set;

public class ImageDataCache {

    private static boolean isCacheDirty = false;
    private static TrieStructure objectTrie;

    public static void initializeCache(DBHelper dbHelper) {
        objectTrie = new TrieStructure();
        List<ImageData> allImages = dbHelper.fetchImages();
        for (ImageData image : allImages) {
            if (image.getObjectsRecognition() != null) {
                for (String object : image.getObjectsRecognition().getObjectsDetected()) {
                    objectTrie.insert(object, image);
                }
            }
        }
    }

    public static void markCacheDirty() {
        isCacheDirty = true;
    }

    public static void refreshCacheIfNeeded(DBHelper dbHelper) {
        if (objectTrie == null || isCacheDirty) {
            objectTrie = new TrieStructure(); // Initialize the Trie
            List<ImageData> allImages = dbHelper.fetchImages();
            for (ImageData image : allImages) {
                if (image.getObjectsRecognition() != null) {
                    Set<String> detectedObjects = image.getObjectsRecognition().getObjectsDetected();
                    if (detectedObjects != null) { // Handle null sets
                        for (String object : detectedObjects) {
                            objectTrie.insert(object, image);
                        }
                    }
                }
            }
            isCacheDirty = false;
        }
    }

    public static List<ImageData> searchImages(String query) {
        if (objectTrie == null) {
            throw new IllegalStateException("ObjectTrie is not initialized. Call refreshCacheIfNeeded() first.");
        }
        return objectTrie.search(query);
    }

}
