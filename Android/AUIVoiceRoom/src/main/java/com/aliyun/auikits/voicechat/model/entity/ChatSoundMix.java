package com.aliyun.auikits.voicechat.model.entity;


import java.io.Serializable;

public class ChatSoundMix implements Serializable {
    private String id;
    private int nameRes;
    private int imageRes;

    private boolean selected = false;

    public ChatSoundMix() {

    }

    public ChatSoundMix(String id, int nameRes, int imageRes) {
        this.id = id;
        this.nameRes = nameRes;
        this.imageRes = imageRes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNameRes() {
        return nameRes;
    }

    public void setNameRes(int nameRes) {
        this.nameRes = nameRes;
    }

    public int getImageRes() {
        return imageRes;
    }

    public void setImageRes(int imageRes) {
        this.imageRes = imageRes;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
