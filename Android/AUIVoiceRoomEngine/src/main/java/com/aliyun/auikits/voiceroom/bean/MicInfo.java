package com.aliyun.auikits.voiceroom.bean;

public class MicInfo {
    public final int position;
    public final boolean audioMute;
    public MicInfo(int pos, boolean audioMute){
        position = pos;
        this.audioMute = audioMute;
    }
}
