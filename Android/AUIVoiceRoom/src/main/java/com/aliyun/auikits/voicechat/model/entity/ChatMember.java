package com.aliyun.auikits.voicechat.model.entity;


import com.aliyun.auikits.voiceroom.bean.NetworkState;
import com.aliyun.auikits.voiceroom.bean.UserInfo;

import java.io.Serializable;
import java.util.Objects;

public class ChatMember implements Serializable {
    /** 麦克风打开 **/
    public static final int MICROPHONE_STATUS_ON = 1;
    /** 麦克风关闭 **/
    public static final int MICROPHONE_STATUS_OFF = 2;
    /** 麦克风禁止使用 **/
    public static final int MICROPHONE_STATUS_DISABLE = 3;

    /** 身份-观众 **/
    public static final int IDENTIFY_FLAG_AUDIENCE = 0;
    /** 身份-主持人 **/
    public static final int IDENTIFY_FLAG_COMPERE = 1;
    /** 身份-自己 **/
    public static final int IDENTIFY_FLAG_SELF = 2;
    /** 身份-连麦观众 **/
    public static final int IDENTIFY_FLAG_CHAT = 4;

    /** 身份标识 **/
    private int identifyFlag = IDENTIFY_FLAG_AUDIENCE;
    /** 麦克风状态 **/
    private int microphoneStatus = MICROPHONE_STATUS_ON;

    private UserInfo userInfo;
    private boolean speaking = false;

    public ChatMember() {
        this("");
    }

    public ChatMember(String userId) {
        this(new UserInfo(userId,""));
    }

    public ChatMember(UserInfo userInfo) {
        this.userInfo = userInfo;
        if(userInfo.isMute) {
            this.microphoneStatus = MICROPHONE_STATUS_OFF;
        } else {
            this.microphoneStatus = MICROPHONE_STATUS_ON;
        }
    }

    public String getId() {
        return this.userInfo.userId;
    }

    public String getName() {
        return this.userInfo.userName;
    }

    public void setName(String name) {
        this.userInfo.userName = name;
    }

    public String getAvatar() {
        return this.userInfo.avatarUrl;
    }

    public void setAvatar(String avatar) {
        this.userInfo.avatarUrl = avatar;
    }

    public NetworkState getNetworkStatus() {
        return this.userInfo.networkState;
    }

    public void setNetworkStatus(NetworkState networkStatus) {
        this.userInfo.networkState = networkStatus;
    }

    public int getMicrophoneStatus() {
        return microphoneStatus;
    }

    public void setMicrophoneStatus(int microphoneStatus) {
        this.microphoneStatus = microphoneStatus;
    }

    public int getIndex() {
        return this.userInfo.micPosition;
    }

    public void setIndex(int index) {
        this.userInfo.micPosition = index;
    }

    public void setIdentifyFlag(int identifyFlag) {
        this.identifyFlag = identifyFlag;
    }

    public int getIdentifyFlag() {
        return identifyFlag;
    }

    public boolean isSpeaking() {
        return this.speaking;
    }

    public void setSpeaking(boolean speaking) {
        this.speaking = speaking;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatMember that = (ChatMember) o;
        return this.userInfo.userId.equals(that.userInfo.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userInfo.userId);
    }


}
