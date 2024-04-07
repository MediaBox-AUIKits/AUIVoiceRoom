package com.aliyun.auikits.voiceroom.bean;

public enum RoomState {
    UN_INIT(0x0001, "UN_INIT"),
    INIT(0x0010, "INIT"),
    IN_ROOM(0x0100, "IN_ROOM"),
    IN_MIC(0x1000, "IN_MIC");
    private final int mValue;
    private final String mName;
    RoomState(int v, String name){
        mValue = v;
        mName = name;
    }
    public int val(){
        return mValue;
    }
    public String getName(){
        return mName;
    }
}
