package com.aliyun.auikits.voicechat.model.entity.network;


import java.util.ArrayList;
import java.util.List;

public class ChatRoomMeetingInfo {
    public List<ChatRoomMicItem> members = new ArrayList<>();

    public List<ChatRoomMicItem> getMicMemberList() {
        List<ChatRoomMicItem> joinMicMember = new ArrayList<>();
        if(members != null && joinMicMember.size() > 0) {
            for(ChatRoomMicItem item : members) {
                if(item.joined) {
                    joinMicMember.add(item);
                }
            }
        }
        return joinMicMember;
    }

    public List<String> getMicAvatarList() {
        List<String> joinMicMember = new ArrayList<>();
        if(members != null && members.size() > 0) {
            for(ChatRoomMicItem item : members) {
                if(item.joined) {
                    joinMicMember.add(item.getAvatar());
                }
            }
        }
        return joinMicMember;
    }
}
