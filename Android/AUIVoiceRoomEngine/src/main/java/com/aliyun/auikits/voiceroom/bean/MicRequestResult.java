package com.aliyun.auikits.voiceroom.bean;

public class MicRequestResult {
    public final int micPosition;
    // 0 : 成功 , 1 :上麦人数已满 2: 已经在麦上
    public final int reason;
    public MicRequestResult(int r, int pos){
        this.reason = r;
        this.micPosition = pos;
    }
}
