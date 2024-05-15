package com.aliyun.auikits.voicechat.model.entity;


import java.io.Serializable;

public class ChatMusicItem implements Serializable {
    private String id;
    private String title;
    private String author;
    private String url;

    private boolean isPlaying = false;
    private boolean applying = false;

    private int soundId = -1;
    private int volume = 50;

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

    public boolean isApplying() {
        return applying;
    }

    public void setApplying(boolean applying) {
        this.applying = applying;
    }

    public int getSoundId() {
        return soundId;
    }

    public void setSoundId(int soundId) {
        this.soundId = soundId;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }
}
