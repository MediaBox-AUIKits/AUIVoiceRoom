package com.aliyun.auikits.voicechat.model.entity.network;

import com.google.gson.annotations.SerializedName;

public class CreateRoomResponse extends BaseResponse {
    public String id;
    public String created_at;
    public String updated_at;
    public String title;
    public String notice;
    public String cover_url;
    public String anchor_id;
    public String anchor_nick;
    @SerializedName("extends")
    public String exd;
    public int status;
    public String chat_id;
    //房间号
    public int show_code;
}
