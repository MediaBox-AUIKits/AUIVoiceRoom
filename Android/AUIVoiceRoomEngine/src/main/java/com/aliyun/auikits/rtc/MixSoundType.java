package com.aliyun.auikits.rtc;

public enum MixSoundType {
    //关闭
    OFF(0),

    //人声I(1),
    Vocal_I(1),

    //人声II
    Vocal_II(2),

    //澡堂
    Bathroom(3),

    //明亮小房间
    Small_Room_Bright(4),

    //黑暗小房间
    Small_Room_Dark(5),

    //中等房间
    Medium_Room(6),

    //大房间
    Large_Room(7),

    //教堂走廊
    Church_Hall(8);
    private final int mVal;
    MixSoundType(int val){
        this.mVal = val;
    }

    public int getVal(){
        return this.mVal;
    }

    public static MixSoundType fromInt(int val){
        for(MixSoundType type : MixSoundType.values()){
            if(type.getVal() == val)
                return type;
        }
        return MixSoundType.OFF;
    }
}
