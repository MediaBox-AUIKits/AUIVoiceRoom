package com.aliyun.auikits.voiceroom.bean;

public enum NetworkState {
    /**
     * 网络质量较好
     * */
    EXCELLENT(0, "excellent"),

    /**
     * 网络质量一般
     * */
    NORMAL(1, "normal"),

    /**
     * 网络质量较差
     * */
    WEAK(2, "weak"),

    /**
     * 网络断连
     * */
    DISCONNECT(2, "disconnect"),

    /**
     * 网络情况未知
     * */
    UNKNOWN(3, "unknown");
    private final int mVal;
    private final String mName;
    NetworkState(int val, String name){
        mVal = val;
        mName = name;
    }

    public int getValue(){
        return mVal;
    }

    public String getName(){
        return mName;
    }
}
