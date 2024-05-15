package com.aliyun.auikits.voiceroom.bean;

import com.aliyun.auikits.rtc.MixSoundType;

//混响bean
public class MixSound {
    public MixSoundType mixSoundType = MixSoundType.OFF;
    public MixSound(MixSoundType type){
        this.mixSoundType = type;
    }
}
