package com.aliyun.auikits.voiceroom;

import android.content.Context;

import com.alivc.auimessage.model.token.IMNewToken;
import com.aliyun.auikits.voiceroom.bean.MicInfo;
import com.aliyun.auikits.voiceroom.bean.RoomInfo;
import com.aliyun.auikits.voiceroom.bean.RoomState;
import com.aliyun.auikits.voiceroom.bean.UserInfo;
import com.aliyun.auikits.voiceroom.callback.ActionCallback;
import com.aliyun.auikits.voiceroom.external.RtcInfo;
import com.aliyun.auikits.voiceroom.external.TokenAccessor;

import java.util.Map;

public interface AUIVoiceRoom {

    //初始化
    void init(Context context, String appId, String authorization, UserInfo userInfo, IMNewToken token, ActionCallback callback);

    //释放实例
    void release();

    //添加回调
    void addRoomCallback(AUIVoiceRoomCallback callback);
    //移除回调
    void removeRoomCallback(AUIVoiceRoomCallback callback);

    //创建房间
    void createRoom(RoomInfo roomInfo, ActionCallback callback);

    //加入房间
    void joinRoom(RoomInfo roomInfo, RtcInfo rtcInfo, ActionCallback callback);

    //离开房间
    void leaveRoom(ActionCallback callback);

    //解散房间
    void dismissRoom(ActionCallback callback);

    //请求连麦
    void requestMic(ActionCallback callback);

    //直接上麦
    void joinMic(MicInfo micInfo, ActionCallback callback);

    //下麦
    void leaveMic(ActionCallback callback);

    //开启或关闭麦克风
    void openMic(boolean open);

    //开启或关闭扬声器
    void openLoudSpeaker(boolean open);

    //静音目标用户
    void mute(UserInfo user, boolean mute, ActionCallback callback);

    //全员静音
    void muteAll(boolean mute, ActionCallback callback);

    //踢出目标用户
    void kickOut(UserInfo user, ActionCallback callback);

    //发送文本消息
    void sendTextMessage(String message, ActionCallback callback);

    //查询最近文本消息
    void listRecentTextMessage(ActionCallback callback);

    //查询上麦用户
    void listMicUserList(ActionCallback callback);

    UserInfo getCurrentUser();

    RoomInfo getRoomInfo();

    //当前用户是否主持人
    boolean isHost();

    //指定用户是否主持人
    boolean isHost(UserInfo userInfo);

    RoomState getRoomState();
}
