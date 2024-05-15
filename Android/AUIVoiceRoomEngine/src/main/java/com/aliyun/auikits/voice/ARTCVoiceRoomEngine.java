package com.aliyun.auikits.voice;

import android.content.Context;

import com.alivc.auimessage.model.token.IMNewToken;
import com.alivc.rtc.AliRtcEngine;
import com.aliyun.auikits.rtc.ClientMode;
import com.aliyun.auikits.voiceroom.bean.AudioEffect;
import com.aliyun.auikits.voiceroom.bean.MicInfo;
import com.aliyun.auikits.voiceroom.bean.MixSound;
import com.aliyun.auikits.voiceroom.bean.Music;
import com.aliyun.auikits.voiceroom.bean.RoomInfo;
import com.aliyun.auikits.voiceroom.bean.UserInfo;
import com.aliyun.auikits.voiceroom.bean.VoiceChange;
import com.aliyun.auikits.voiceroom.callback.ActionCallback;
import com.aliyun.auikits.voiceroom.external.RtcInfo;

public interface ARTCVoiceRoomEngine {

    //初始化
    void init(Context context, ClientMode mode, String appId, UserInfo userInfo, IMNewToken token, ActionCallback callback);

    //释放实例
    void release();

    //添加回调
    void addObserver(ARTCVoiceRoomEngineDelegate callback);
    //移除回调
    void removeObserver(ARTCVoiceRoomEngineDelegate callback);

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
    void switchMicrophone(boolean open);

    //设置音频输出
    void setAudioOutputType(AudioOutputType type);

    AudioOutputType getAudioOutputType();

    UserInfo getCurrentUser();

    //发送文本消息
    void sendTextMessage(String message, ActionCallback callback);

    //发送信令
    void sendCommand(UserInfo user, int type, String protocol, ActionCallback callback);

    //当前用户是否主持人
    boolean isAnchor();

    //指定用户是否主持人
    boolean isAnchor(UserInfo userInfo);

    //是否已加入语聊房
    boolean isJoinRoom();

    //是否已上麦
    boolean isJoinMic();

    //获取RTC engine
    AliRtcEngine getRTCEngine();

    //静音目标用户
    void mute(UserInfo user, boolean mute, ActionCallback callback);

    //全员静音
    void muteAll(boolean mute, ActionCallback callback);

    //踢出目标用户
    void kickOut(UserInfo user, ActionCallback callback);

    //查询上麦用户
    void listMicUserList(ActionCallback callback);

    RoomInfo getRoomInfo();

    //设置混响
    void setAudioMixSound(MixSound mix);

    //设置变声
    void setVoiceChange(VoiceChange change);

    //设置背景音乐
    void setBackgroundMusic(Music music);

    //设置音效
    int playAudioEffect(AudioEffect effect);

    //设置人声音量 [0,100]
    void setRecordingVolume(int volume);

    //设置伴奏音乐音量 [0,100]
    void setAccompanyVolume(int volume);

    //设置音效音量 [0,100]
    void setAudioEffectVolume(int soundId, int volume);

    //获取成员数量
    int getMemberCount();

    //打开或关闭耳返
    void enableEarBack(boolean enable);
}
