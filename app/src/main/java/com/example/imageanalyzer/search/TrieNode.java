package com.example.imageanalyzer.search;

import com.example.imageanalyzer.beans.ImageData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrieNode {
    private Map<Character, TrieNode> children;
    private List<ImageData> imageDataList;
    private boolean isEndOfWord;

    public TrieNode(){
        this.children = new HashMap<>();
        this.imageDataList = new ArrayList<>();
        this.isEndOfWord = false;
    }

    public Map<Character, TrieNode> getChildren() {
        return children;
    }

    public boolean isEndOfWord() {
        return isEndOfWord;
    }

    public void setChildren(Map<Character, TrieNode> children) {
        this.children = children;
    }

    public void setEndOfWord(boolean endOfWord) {
        isEndOfWord = endOfWord;
    }

    public List<ImageData> getImageDataList() {
        return imageDataList;
    }

    public void setImageDataList(List<ImageData> imageDataList) {
        this.imageDataList = imageDataList;
    }
}
