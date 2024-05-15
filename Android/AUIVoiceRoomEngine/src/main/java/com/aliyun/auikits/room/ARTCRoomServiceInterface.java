package com.aliyun.auikits.room;

import com.aliyun.auikits.voiceroom.bean.MicInfo;
import com.aliyun.auikits.voiceroom.bean.RoomInfo;
import com.aliyun.auikits.voiceroom.bean.UserInfo;
import com.aliyun.auikits.voiceroom.callback.ActionCallback;

import java.util.List;

public interface ARTCRoomServiceInterface {

    //获取IM token
    String fetchIMLoginToken();

    //获取RTC入会token
    String fetchRTCAuthToken();

    //获取房间列表
    List<RoomInfo> getRoomList();

    //获取房间详情
    RoomInfo getRoomDetail();

    //创建房间
    int createRoom(RoomInfo info);

    //解散房间
    int dismissRoom(String roomId);

    //获取连麦列表
    void getMicList(String roomId, ActionCallback callback);

    //申请or邀请连麦
    void requestMic(RequestMicInfo requestMic, ActionCallback callback);

    //同意连麦
    int agreeRequestMic(MicInfo mic);

    //拒绝连麦
    int rejectRequestMic(MicInfo mic);

    //下麦
    void leaveMic(LeaveMicInfo info, ActionCallback callback);

    //锁麦
    int lockMic(MicInfo mic);

    //踢人下麦
    int kickOutMic(KickOutMicInfo kickInfo);

    //禁言目标用户
    int mute(String groupId, UserInfo user, boolean mute);

    //获取禁言用户列表
    List<UserInfo> getMuteList();

    //全员禁言
    int muteAll(String groupId, boolean mute);
}
