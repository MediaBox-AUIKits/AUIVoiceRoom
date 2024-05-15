package com.aliyun.auikits.voiceroom.module.seat.factory;

import com.aliyun.auikits.rtc.ClientMode;
import com.aliyun.auikits.single.Singleton;
import com.aliyun.auikits.voiceroom.module.seat.SeatManager;
import com.aliyun.auikits.voiceroom.module.seat.impl.ServerSeatManager;

public class SeatManagerFactory {
    public static SeatManager createServerSeatManager(ClientMode mode){
        ServerSeatManager manager = Singleton.getInstance(ServerSeatManager.class);
        manager.setClientMode(mode);
        return manager;
    }
}
