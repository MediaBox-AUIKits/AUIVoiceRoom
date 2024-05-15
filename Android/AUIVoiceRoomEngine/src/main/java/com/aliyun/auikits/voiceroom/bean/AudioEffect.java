package com.aliyun.auikits.voiceroom.bean;

//音效
public class AudioEffect {
    public final String pathOrUrl;
    public boolean justForTest = false;
    public int volume = 50;
    public AudioEffect(String s){
        this.pathOrUrl = s;
    }
}
