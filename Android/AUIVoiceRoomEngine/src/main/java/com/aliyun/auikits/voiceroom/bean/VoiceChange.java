package com.aliyun.auikits.voiceroom.bean;

import com.aliyun.auikits.rtc.VoiceChangeType;

//变声
public class VoiceChange {
    public VoiceChangeType voiceType = VoiceChangeType.OFF;
    public VoiceChange(VoiceChangeType type){
        this.voiceType = type;
    }
}
