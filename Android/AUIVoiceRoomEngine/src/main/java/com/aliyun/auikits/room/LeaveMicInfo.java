package com.aliyun.auikits.room;

import com.aliyun.auikits.voiceroom.bean.MicInfo;
import com.aliyun.auikits.voiceroom.bean.RoomInfo;
import com.aliyun.auikits.voiceroom.bean.UserInfo;

public class LeaveMicInfo {
    public UserInfo userInfo;
    public RoomInfo roomInfo;
    public int micPos; //麦位序号
    public boolean isHost = false; //是否是主持人
}
