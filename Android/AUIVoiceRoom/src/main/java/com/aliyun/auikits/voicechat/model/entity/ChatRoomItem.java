package com.aliyun.auikits.voicechat.model.entity;


import com.aliyun.auikits.voiceroom.bean.UserInfo;

import java.io.Serializable;
import java.util.List;

public class ChatRoomItem implements Serializable {
    private String id;
    private String roomId;
    private String title;
    private int memberNum = 0;

    private ChatMember compere;

    private List<String> avatarList;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getMemberNum() {
        return memberNum;
    }

    public void setMemberNum(int memberNum) {
        this.memberNum = memberNum;
    }

    public List<String> getAvatarList() {
        return avatarList;
    }

    public void setAvatarList(List<String> avatarList) {
        this.avatarList = avatarList;
    }

    public ChatMember getCompere() {
        return compere;
    }

    public void setCompere(ChatMember compere) {
        this.compere = compere;
    }

    public UserInfo getCompereUserInfo() {
        UserInfo userInfo = new UserInfo(compere.getId(), "");
        userInfo.userName = compere.getName();
        userInfo.avatarUrl = compere.getAvatar();
        return userInfo;
    }
}
