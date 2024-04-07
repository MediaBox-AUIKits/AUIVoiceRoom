package com.aliyun.auikits.voiceroom.factory;

import android.os.Looper;

import com.aliyun.auikits.voiceroom.AUIVoiceRoom;
import com.aliyun.auikits.voiceroom.impl.AUIVoiceRoomImpl;

public class AUIVoiceRoomFactory {
    public static AUIVoiceRoom createVoiceRoom(){
        return createVoiceRoom(Looper.getMainLooper());
    }

    public static AUIVoiceRoom createVoiceRoom(Looper l){
        return new AUIVoiceRoomImpl(l);
    }
}
