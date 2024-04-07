package com.aliyun.auikits.voicechat.model.entity;

import com.aliyun.auikits.voiceroom.AUIVoiceRoomCallback;
import com.aliyun.auikits.voiceroom.bean.MicRequestResult;
import com.aliyun.auikits.voiceroom.bean.UserInfo;

import java.util.List;

public class ChatRoomCallback implements AUIVoiceRoomCallback {
    @Override
    public void onJoin(String roomId, String uid) {

    }

    @Override
    public void onLeave() {

    }

    @Override
    public void onUserOnline(UserInfo user) {

    }

    @Override
    public void onUserOffline(UserInfo user) {

    }

    @Override
    public void onUserJoinMic(UserInfo user) {

    }

    @Override
    public void onUserLeaveMic(UserInfo user) {

    }

    @Override
    public void onResponseMic(MicRequestResult rs) {

    }

    @Override
    public void onTextMessageReceived(UserInfo user, String text) {

    }

    @Override
    public void onUserMicOn(UserInfo user) {

    }

    @Override
    public void onUserMicOff(UserInfo user) {

    }

    @Override
    public void onUserSpeakState(UserInfo user) {

    }

    @Override
    public void onUserNetworkState(UserInfo user) {

    }

    @Override
    public void onMute(boolean mute) {

    }

    @Override
    public void onKickOut() {

    }

    @Override
    public void onDismissRoom(String commander) {

    }

    @Override
    public void onExitGroup(String msg) {

    }

    @Override
    public void onRoomMicListChanged(List<UserInfo> micUsers) {

    }

    @Override
    public void onDataChannelMessage(String uid, com.alivc.rtc.AliRtcEngine.AliRtcDataChannelMsg msg) {

    }

    @Override
    public void onVoiceRoomDebugInfo(String msg) {

    }
}
