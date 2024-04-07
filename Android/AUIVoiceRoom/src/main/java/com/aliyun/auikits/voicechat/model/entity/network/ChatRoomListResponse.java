package com.aliyun.auikits.voicechat.model.entity.network;


import java.util.ArrayList;
import java.util.List;

public class ChatRoomListResponse extends BaseResponse{
    public List<ChatRoomRspItem> rooms = new ArrayList<>();
}
