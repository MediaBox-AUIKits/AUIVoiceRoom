package com.aliyun.auikits.voiceroom.bean;

import java.io.Serializable;
import java.util.Objects;

public class UserInfo implements Serializable {
    public final String userId;
    public final String deviceId;

    //TODO 新增了以下字段
    public String userName;
    public String avatarUrl;
    public int micPosition = 0;
    public boolean speaking = false;
    public NetworkState networkState = NetworkState.NORMAL;
    //

    public boolean isPublish = false;
    public boolean isMute = false;

    public UserInfo(String uid, String devid){
        userId = uid;
        deviceId = devid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserInfo userInfo = (UserInfo) o;
        return userId.equals(userInfo.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
