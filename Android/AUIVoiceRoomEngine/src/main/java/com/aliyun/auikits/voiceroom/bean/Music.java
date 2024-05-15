package com.aliyun.auikits.voiceroom.bean;

//背景音乐
public class Music {
    public final String path;
    public boolean justForTest = false;
    public int volume = 50;
    public Music(String p){
        this.path = p;
    }
}
