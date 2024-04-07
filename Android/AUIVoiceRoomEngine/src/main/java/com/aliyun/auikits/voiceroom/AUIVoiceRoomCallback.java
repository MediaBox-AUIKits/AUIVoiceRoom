package com.aliyun.auikits.voiceroom;

import com.alivc.rtc.AliRtcEngine;
import com.aliyun.auikits.voiceroom.bean.MicInfo;
import com.aliyun.auikits.voiceroom.bean.MicRequestResult;
import com.aliyun.auikits.voiceroom.bean.UserInfo;

import java.util.List;

public interface AUIVoiceRoomCallback {

    //加入房间的回调
    void onJoin(String roomId, String uid);

    //离开房间的回调
    void onLeave();

    //用户进入房间的回调
    void onUserOnline(UserInfo user);

    //用户离开房间的回调
    void onUserOffline(UserInfo user);

    //用户上麦的回调
    void onUserJoinMic(UserInfo user);

    //用户下麦的回调
    void onUserLeaveMic(UserInfo user);

    //用户请求连麦的响应
    void onResponseMic(MicRequestResult rs);

    //接收到消息的回调
    void onTextMessageReceived(UserInfo user,String text);

    //用户开麦回调
    void onUserMicOn(UserInfo user);

    //用户闭麦回调
    void onUserMicOff(UserInfo user);

    //用户声音输出状态回调
    void onUserSpeakState(UserInfo user);

    //用户网络状态回调
    void onUserNetworkState(UserInfo user);

    //被禁言的回调
    void onMute(boolean mute);

    //被踢出房间的回调
    void onKickOut();

    //房间已解散
    void onDismissRoom(String commander);

    //被踢出群组
    void onExitGroup(String msg);

    void onRoomMicListChanged(List<UserInfo> micUsers);

    //rtc channel 信令数据回调
    void onDataChannelMessage(String uid, AliRtcEngine.AliRtcDataChannelMsg msg);

    void onVoiceRoomDebugInfo(String msg);
}
