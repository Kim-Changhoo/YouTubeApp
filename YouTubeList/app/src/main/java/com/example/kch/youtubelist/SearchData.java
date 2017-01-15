package com.example.kch.youtubelist;

/**
 * Created by KCH on 2017-01-12.
 */

public class SearchData {
    String videoId;
    String title;
    String url;
    String publishedAt;
    String kind;

    public SearchData(String videoId, String title, String url, String publishedAt) {
        super();
        this.videoId = videoId;
        this.title = title;
        this.url = url;
        this.publishedAt = publishedAt;
    }

    public String getVideoId() {
        return videoId;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public String getChannel() { return kind; }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public void setChannel(String channel) {
        this.kind = channel;
    }
}
