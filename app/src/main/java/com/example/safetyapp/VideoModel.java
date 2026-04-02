package com.example.safetyapp;

public class VideoModel {

    private String title;
    private String videoUrl;
    private int thumbnail;

    public VideoModel(String title, String videoUrl, int thumbnail) {
        this.title = title;
        this.videoUrl = videoUrl;
        this.thumbnail = thumbnail;
    }

    public String getTitle() {
        return title;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public int getThumbnail() {
        return thumbnail;
    }

    public String getUrl() {
        return null;
    }
}