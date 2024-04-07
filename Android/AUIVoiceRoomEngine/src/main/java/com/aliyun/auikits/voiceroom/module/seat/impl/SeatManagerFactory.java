package com.aliyun.auikits.voiceroom.module.seat.impl;

import com.aliyun.auikits.voiceroom.module.seat.SeatManager;
import com.aliyun.auikits.voiceroom.module.seat.callback.SeatManagerCallback;

public class SeatManagerFactory {
    public static SeatManager createServerSeatManager(String token, SeatManagerCallback callback){
        return new ServerSeatManager(token, callback);
    }
}
