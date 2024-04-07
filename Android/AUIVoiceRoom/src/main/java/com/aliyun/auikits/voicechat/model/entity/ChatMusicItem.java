package com.aliyun.auikits.voicechat.model.entity;


import java.io.Serializable;

public class ChatMusicItem implements Serializable {
    private String id;
    private String title;
    private String author;
    private String url;

    private boolean isPlaying = false;

    public ChatMusicItem() {

    }


    public ChatMusicItem(String id, String title, String author, String url) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.url = url;
    }

    public String getId() {
        return id;
    }


    public String getTitle() {
        return title;
    }


    public String getAuthor() {
        return author;
    }

    public String getUrl() {
        return url;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }
}
