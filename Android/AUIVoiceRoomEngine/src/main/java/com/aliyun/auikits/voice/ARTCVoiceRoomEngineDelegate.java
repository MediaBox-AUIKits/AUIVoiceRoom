package com.aliyun.auikits.voice;

import com.alivc.rtc.AliRtcEngine;
import com.aliyun.auikits.voiceroom.bean.AccompanyPlayState;
import com.aliyun.auikits.voiceroom.bean.MicRequestResult;
import com.aliyun.auikits.voiceroom.bean.UserInfo;

import java.util.List;

public interface ARTCVoiceRoomEngineDelegate {

    //加入房间的回调
    void onJoin(String roomId, String uid);

    //离开房间的回调
    void onLeave();

    //用户进入房间的回调
    void onJoinedRoom(UserInfo user);

    //用户离开房间的回调
    void onLeavedRoom(UserInfo user);

    //被踢出房间的回调
    void onKickOutRoom();

    //房间已解散
    void onDismissRoom(String commander);

    //用户上麦的回调
    void onJoinedMic(UserInfo user);

    //用户下麦的回调
    void onLeavedMic(UserInfo user);

    //用户请求连麦的响应
    void onResponseMic(MicRequestResult rs);

    //接收到消息的回调
    void onReceivedTextMessage(UserInfo user, String text);

    //用户开关麦回调
    void onMicUserMicrophoneChanged(UserInfo user, boolean open);

    //用户声音输出状态回调
    void onMicUserSpeakStateChanged(UserInfo user);

    //用户网络状态回调
    void onNetworkStateChanged(UserInfo user);

    //错误回调
    void onError(int code, String msg);

    //被禁言的回调
    void onMute(boolean mute);

    //被踢出群组
    void onExitGroup(String msg);

    void onRoomMicListChanged(List<UserInfo> micUsers);

    //rtc channel 信令数据回调
    void onDataChannelMessage(String uid, AliRtcEngine.AliRtcDataChannelMsg msg);

    //伴奏播放状态变化
    void onAccompanyStateChanged(AccompanyPlayState state);

    //群组内用户人数发生变化时回调
    void onMemberCountChanged(int count);

    void onVoiceRoomDebugInfo(String msg);
}
