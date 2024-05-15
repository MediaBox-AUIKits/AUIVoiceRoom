package com.aliyun.auikits.voiceroom.bean;

public class RoomInfo {
    public final String roomId;
    public UserInfo creator;
    public RoomInfo(String id){
        roomId = id;
    }
}
