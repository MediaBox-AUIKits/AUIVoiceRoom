package com.aliyun.auikits.voicechat.model.entity;

import com.aliyun.auikits.voice.ARTCVoiceRoomEngineDelegate;
import com.aliyun.auikits.voiceroom.bean.AccompanyPlayState;
import com.aliyun.auikits.voiceroom.bean.MicRequestResult;
import com.aliyun.auikits.voiceroom.bean.UserInfo;

import java.util.List;

public class ChatRoomCallback implements ARTCVoiceRoomEngineDelegate {
    @Override
    public void onJoin(String roomId, String uid) {

    }

    @Override
    public void onLeave() {

    }

    @Override
    public void onJoinedRoom(UserInfo user) {

    }

    @Override
    public void onLeavedRoom(UserInfo user) {

    }

    @Override
    public void onJoinedMic(UserInfo user) {

    }

    @Override
    public void onLeavedMic(UserInfo user) {

    }

    @Override
    public void onResponseMic(MicRequestResult rs) {

    }

    @Override
    public void onReceivedTextMessage(UserInfo user, String text) {

    }

    @Override
    public void onMicUserMicrophoneChanged(UserInfo user, boolean open) {

    }

    @Override
    public void onMicUserSpeakStateChanged(UserInfo user) {

    }

    @Override
    public void onNetworkStateChanged(UserInfo user) {

    }

    @Override
    public void onError(int code, String msg) {

    }

    @Override
    public void onMute(boolean mute) {

    }

    @Override
    public void onKickOutRoom() {

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
    public void onAccompanyStateChanged(AccompanyPlayState state) {

    }

    @Override
    public void onVoiceRoomDebugInfo(String msg) {

    }

    @Override
    public void onMemberCountChanged(int count) {

    }
}
