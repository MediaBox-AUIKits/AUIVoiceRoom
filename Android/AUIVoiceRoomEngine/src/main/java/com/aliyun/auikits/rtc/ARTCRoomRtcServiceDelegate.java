package com.aliyun.auikits.rtc;

import com.alivc.rtc.AliRtcEngine;

//rtc场景回调
public interface ARTCRoomRtcServiceDelegate {

    //入会的通知
    void onJoined(String channelId, String userId);

    //离会通知
    void onLeaved(String userId);

    //开始推流通知
    void onStartedPublish(String userId);

    //停止推流通知
    void onStoppedPublish(String userId);

    //麦克风开关通知（包括自己）
    void onMicrophoneStateChanged(String userId, boolean open);

    //摄像头开关通知（包括自己）
    void onCameraStateChanged(String userId, boolean open);

    //数据通道消息
    void onDataChannelMessage(String userId, AliRtcEngine.AliRtcDataChannelMsg msg);

    //网络状态发生变化（包括自己）
    void onNetworkStateChanged(String userId, AliRtcEngine.AliRtcNetworkQuality quality);

    //音量发生变化（包括自己）
    void onAudioVolumeChanged(String userId);

    //当前说话焦点发生变化（包括自己）
    void onSpeakerActivated(String userId, boolean speaking);

    //当前token即将过期
    void onJoinTokenWillExpire();

    //本地伴奏播放状态改变回调
    void onAccompanyStateChanged(AliRtcEngine.AliRtcAudioAccompanyStateCode state);

    //出现错误
    void onError(int code, String msg);
}
