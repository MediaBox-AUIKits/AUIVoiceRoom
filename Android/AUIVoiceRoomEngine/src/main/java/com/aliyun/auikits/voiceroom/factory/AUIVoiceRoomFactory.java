package com.aliyun.auikits.voiceroom.factory;

import android.os.Looper;

import com.aliyun.auikits.voice.ARTCVoiceRoomEngine;
import com.aliyun.auikits.voiceroom.impl.AUIVoiceRoomImplV2;

public class AUIVoiceRoomFactory {
    public static ARTCVoiceRoomEngine createVoiceRoom(){
        return createVoiceRoom(Looper.getMainLooper());
    }

    public static ARTCVoiceRoomEngine createVoiceRoom(Looper l){
//        return new AUIVoiceRoomImpl(l);
        return new AUIVoiceRoomImplV2();
    }
}
