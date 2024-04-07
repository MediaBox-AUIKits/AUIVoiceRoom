package com.aliyun.auikits.voicechat.model.entity.network;

import com.google.gson.annotations.SerializedName;

public class CreateRoomRequest {
    public String title;
    public String notice;
    public String anchor;
    public String anchor_nick;
    @SerializedName("extends")
    public String exd;
}
