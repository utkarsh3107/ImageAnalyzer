package com.example.imageanalyzer.search;

import com.example.imageanalyzer.beans.ImageData;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

public class TrieStructure {

    private final TrieNode root = new TrieNode();

    public void insert(String word, ImageData imageData) {
        TrieNode curr = root;
        for (char eachCh : word.toCharArray()) {
            curr = curr.getChildren().computeIfAbsent(eachCh, c -> new TrieNode());
        }
        curr.setEndOfWord(true);
        curr.getImageDataList().add(imageData);
    }

    public List<ImageData> search(String prefix) {
        TrieNode current = root;
        for (char ch : prefix.toCharArray()) {
            if (!current.getChildren().containsKey(ch)) {
                return new ArrayList<>(); // No match
            }
            current = current.getChildren().get(ch);
        }
        return createDictionary(current);
    }

    private List<ImageData> createDictionary(TrieNode node) {
        List<ImageData> results = new ArrayList<>(node.getImageDataList());
        for (Map.Entry<Character, TrieNode> entry : node.getChildren().entrySet()) {
            results.addAll(createDictionary(entry.getValue()));
        }
        return results;
    }
}