package com.aliyun.auikits.voicechat.model.entity;

public class ChatRoom {
    private String id;
    private String title;
    private int memberNum;

    private ChatMember compere;
    private ChatMember self;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public ChatMember getCompere() {
        return compere;
    }

    public void setCompere(ChatMember compere) {
        this.compere = compere;
    }

    public ChatMember getSelf() {
        return self;
    }

    public void setSelf(ChatMember self) {
        this.self = self;
    }

    /**
     * 房间是否主持人用户
     * @return
     */
    public boolean isCompere() {
        if(compere != null) {
            return compere.equals(self);
        } else {
            return false;
        }
    }
}
