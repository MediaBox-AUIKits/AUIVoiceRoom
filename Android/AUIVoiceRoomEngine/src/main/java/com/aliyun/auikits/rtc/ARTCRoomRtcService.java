package com.aliyun.auikits.rtc;

import android.view.View;

import com.alivc.rtc.AliRtcEngine;

public interface ARTCRoomRtcService {

    //设置业务场景模式
    void setMode(ClientMode mode);

    //设置回调
    void setCallback(ARTCRoomRtcServiceDelegate callback);

    //设置预览容器
    void setRenderViewLayout(String uid, View container, boolean isTop, boolean mirror);

    //释放实例
    void release();

    //获取当前场景模式
    ClientMode getMode();

    //加入频道
    int join(RtcChannel rtcChannel);

    //离开频道
    int leave();

    //检查是否已在频道
    boolean isJoin();

    //开始推流
    int startPublish(boolean pushVideo, boolean videoMute, boolean pushAudio, boolean audioMute);

    //停止推流
    int stopPublish();

    //是否推流
    boolean isPublishing();

    //开始视频预览
    int startPreview();

    //停止预览
    int stopPreview();

    //开关麦克风
    int switchMicrophone(boolean open);

    //开关摄像头
    int switchCamera(boolean open);

    //设置摄像头类型（前后置）
    int setCameraType(CameraType type);

    //获取摄像头类型
    CameraType getCameraType();

    //切换音频输出类型
    int setAudioOutputType(AudioOutputType type);

    //获取音频输出类型
    AudioOutputType getAudioOutputType();

    //设置混响
    void setAudioMixSound(MixSoundType type);

    //设置变声
    void setVoiceType(VoiceChangeType type);

    //设置背景音乐
    void setBackgroundMusic(String path, boolean justForTest, int volume);

    //设置音效
    int playAudioEffect(String pathOrUrl, boolean justForTest, int volume);

    //打开或关闭耳返
    void enableEarBack(boolean enable);

    //设置伴奏音量 [0,100]
    void setAccompanyVolume(int volume);

    //设置人声音量 [0,100]
    void setRecordingVolume(int volume);

    //设置音效音量 [0,100]
    void setAudioEffectVolume(int soundId, int volume);

    //获取RTC引擎实例
    AliRtcEngine getRTCEngine();
}
