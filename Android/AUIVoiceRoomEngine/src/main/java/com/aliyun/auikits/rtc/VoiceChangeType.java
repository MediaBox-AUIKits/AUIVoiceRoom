package com.aliyun.auikits.rtc;

public enum VoiceChangeType {
    //关闭
    OFF(0),

    //老人
    Oldman(1),

    //男孩
    Babyboy(2),

    //女孩
    Babygirl(3),

    //机器人
    Robot(4),

    //大魔王
    Daimo(5),

    //KTV
    Ktv(6),

    //回声
    Echo(7),

    //方言
    Dialect(8),

    //怒吼
    Howl(9),

    //电音
    Electronic(10),

    //留声机
    Phonograph(11);

    private final int mVal;
    VoiceChangeType(int val){
        this.mVal = val;
    }

    public int getVal(){
        return this.mVal;
    }

    public static VoiceChangeType fromInt(int val){
        for(VoiceChangeType type : VoiceChangeType.values()){
            if(type.getVal() == val)
                return type;
        }
        return VoiceChangeType.OFF;
    }
}