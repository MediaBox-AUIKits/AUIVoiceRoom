package com.aliyun.auikits.voicechat.model.entity.network;

import android.text.TextUtils;

import com.aliyun.auikits.voicechat.model.entity.ChatMember;
import com.aliyun.auikits.voicechat.model.entity.ChatRoomItem;
import com.aliyun.auikits.voicechat.util.AvatarUtil;
import com.aliyun.auikits.voicechat.util.GsonHolder;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class ChatRoomRspItem {
    public static final String KEY_ANCHOR_AVATAR = "anchor_avatar";

    public String id;
    public String created_at;
    public String updated_at;
    //房间标题
    public String title;
    public String notice;
    public String cover_url;
    //主持人id
    public String anchor_id;
    //主持人昵称
    public String anchor_nick;
    public ChatRoomMetrics metrics;
    //房间扩展信息
    @SerializedName("extends")
    public String exd;
    public int status;
    public String chat_id;
    //房间号
    public int show_code;
    @SerializedName("meetingInfo")
    public String meeting_info;

    public String getAnchorAvatar() {
        if(TextUtils.isEmpty(exd)) {
            return "";
        } else {
            Map extMap = GsonHolder.gson.fromJson(exd, Map.class);
            return (String) extMap.get("anchor_avatar");
        }
    }

    public ChatRoomItem toChatRoomItem() {
        ChatRoomItem chatRoomItem = new ChatRoomItem();
        chatRoomItem.setId(this.id);
        chatRoomItem.setRoomId(String.valueOf(this.show_code));
        chatRoomItem.setTitle(this.title);

        if(this.metrics != null) {
            chatRoomItem.setMemberNum(this.metrics.online_count);
        }

        if(this.meeting_info != null) {
            ChatRoomMeetingInfo chatRoomMeetingInfo = GsonHolder.gson.fromJson(this.meeting_info, ChatRoomMeetingInfo.class);
            if(chatRoomMeetingInfo != null) {
                List<String> joinMicAvatarList = chatRoomMeetingInfo.getMicAvatarList();
                chatRoomItem.setAvatarList(joinMicAvatarList);
            }
        }

        ChatMember adminMem = new ChatMember(this.anchor_id);
        adminMem.setName(this.anchor_nick);
        String anchorAvatar = getAnchorAvatar();
        adminMem.setAvatar(anchorAvatar);
        adminMem.setIdentifyFlag(ChatMember.IDENTIFY_FLAG_COMPERE);
        chatRoomItem.setCompere(adminMem);
        return chatRoomItem;
    }
}
