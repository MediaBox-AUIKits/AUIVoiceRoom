package com.aliyun.auikits.voiceroom.external;

public class RtcInfo {
    public String gslb;
    public String token;
    public long timestamp;
    public RtcInfo(String t, long tstamp, String g){
        token = t;
        timestamp = tstamp;
        gslb = g;
    }
}
