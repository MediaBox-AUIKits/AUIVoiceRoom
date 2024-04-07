package com.aliyun.auikits.voicechat.model.entity;

public class ChatMessage {
    public static final int TYPE_NOTICE = 1;
    public static final int TYPE_CHAT_MSG = 2;

    private int type;
    private ChatMember member;
    private String content;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ChatMember getMember() {
        return member;
    }

    public void setMember(ChatMember member) {
        this.member = member;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
