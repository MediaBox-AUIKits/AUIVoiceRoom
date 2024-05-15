package com.aliyun.auikits.rtc.impl;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.alivc.rtc.AliRtcAuthInfo;
import com.alivc.rtc.AliRtcEngine;
import com.alivc.rtc.AliRtcEngineEventListener;
import com.alivc.rtc.AliRtcEngineNotify;
import com.aliyun.auikits.common.AliyunLog;
import com.aliyun.auikits.room.RoomUserState;
import com.aliyun.auikits.common.util.CommonUtil;
import com.aliyun.auikits.rtc.ARTCRoomRtcService;
import com.aliyun.auikits.rtc.ARTCRoomRtcServiceDelegate;
import com.aliyun.auikits.rtc.AudioOutputType;
import com.aliyun.auikits.rtc.CameraType;
import com.aliyun.auikits.rtc.ClientMode;
import com.aliyun.auikits.rtc.MixSoundType;
import com.aliyun.auikits.rtc.PreviewInfo;
import com.aliyun.auikits.rtc.RtcChannel;
import com.aliyun.auikits.rtc.VoiceChangeType;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ARTCRoomRtcServiceImpl extends AliRtcEngineEventListener implements ARTCRoomRtcService {
    private static final String TAG = "ARTCRoomRtcService";
    private ClientMode mMode = ClientMode.CALL;
    private AliRtcEngine mRTCEngine;
    private RTCAudioVolumeObserver mAudioVolumeObserver = null;
    private AliRtcEngineNotify mEngineRemoteNotify = new RTCEngineNotify(this);
    private Context mContext;
    private Handler mUIHandler = new Handler(Looper.getMainLooper());
    private final Map<String, RoomUserState> mUserStateMap = new LinkedHashMap();
    private ARTCRoomRtcServiceDelegate mRtcObserver;
    private static final int SPEAKING_STATE = 1;
    private static final int SPEAKING_VOLUME = 5;
    private static final String CURRENT_UESR_IN_CALLBACK = "0";
    private String mUserId;
    private boolean mInit = false;
    private String mCurrentChannelId = null;
    private boolean mIsInChannel = false; //是否在频道内
    private boolean mIsPublishing = false; //是否在推流
    private Map<String, PreviewInfo> mCachePreview = new LinkedHashMap();
    private Map<String, Integer> mAudioEffectMap;
    private int mNextAudioEffectId = 1;

    public ARTCRoomRtcServiceImpl(Context ctx){
        mContext = ctx;
        mAudioEffectMap = new Hashtable<>();
    }

    public static final class RTCEngineNotify extends AliRtcEngineNotify {
        private final WeakReference<ARTCRoomRtcServiceImpl> roomRef;

        public RTCEngineNotify(ARTCRoomRtcServiceImpl roomEngine) {
            this.roomRef = new WeakReference<>(roomEngine);
        }

        @Override
        public void onRemoteUserOnLineNotify(final String p0, int p1) {
            AliyunLog.d(TAG, "onRemoteUserOnLineNotify " + p0);
        }

        @Override
        public void onRemoteUserOffLineNotify(final String p0, AliRtcEngine.AliRtcUserOfflineReason p1) {
            AliyunLog.d(TAG, "onRemoteUserOffLineNotify " + p0);
        }

        @Override
        public void onRemoteTrackAvailableNotify(final String p0, final AliRtcEngine.AliRtcAudioTrack p1, final AliRtcEngine.AliRtcVideoTrack p2) {
            AliyunLog.d(TAG, "onRemoteTrackAvailableNotify " + p0 + ", audio: " + p1 + ", video: " + p2);
            if (TextUtils.isEmpty(p0)) {
                return;
            }
            final ARTCRoomRtcServiceImpl obj = roomRef.get();
            if(obj == null)
                return;
            obj.mUIHandler.post(new Runnable() {
                @Override
                public final void run() {
                    if (p1 == AliRtcEngine.AliRtcAudioTrack.AliRtcAudioTrackMic || p2 == AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackCamera) {
                        if (!obj.mUserStateMap.containsKey(p0) || obj.mUserStateMap.get(p0) != RoomUserState.Publishing) {
                            obj.mUserStateMap.put(p0, RoomUserState.Publishing);
                            if (obj.mRtcObserver == null) {
                                return;
                            }
                            obj.mRtcObserver.onStartedPublish(p0);
                            return;
                        }
                        return;
                    }
                    if (!obj.mUserStateMap.containsKey(p0) || obj.mUserStateMap.get(p0) == RoomUserState.Publishing) {
                        obj.mUserStateMap.put(p0, RoomUserState.Online);
                        if (obj.mRtcObserver == null) {
                            return;
                        }
                        obj.mRtcObserver.onStoppedPublish(p0);
                    }
                }
            });
        }

        @Override
        public void onUserVideoMuted(final String p0, final boolean p1) {
            AliyunLog.d(TAG, "onUserVideoMuted userId[" + p0 + "] muted[" + p1 + ']');
            final ARTCRoomRtcServiceImpl obj = roomRef.get();
            if(obj == null)
                return;
            obj.mUIHandler.post(new Runnable() {
                @Override
                public final void run() {
                    if (obj.mRtcObserver == null) {
                        return;
                    }

                    obj.mRtcObserver.onCameraStateChanged(p0, !p1);
                }
            });

        }

        @Override
        public void onUserAudioMuted(final String p0, final boolean p1) {
            AliyunLog.d(TAG, "onUserAudioMuted userId[" + p0 + "] muted[" + p1 + ']');
            final ARTCRoomRtcServiceImpl obj = roomRef.get();
            if(obj == null)
                return;
            obj.mUIHandler.post(new Runnable() {
                @Override
                public final void run() {
                    if (obj.mRtcObserver == null) {
                        return;
                    }

                    obj.mRtcObserver.onMicrophoneStateChanged(p0, !p1);
                }
            });
        }

        @Override
        public void onDataChannelMessage(String s, AliRtcEngine.AliRtcDataChannelMsg aliRtcDataChannelMsg) {
            final ARTCRoomRtcServiceImpl obj = roomRef.get();
            if(obj == null)
                return;
            final String uid = s;
            final AliRtcEngine.AliRtcDataChannelMsg msg = aliRtcDataChannelMsg;
            obj.mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(obj.mRtcObserver == null){
                        return;
                    }
                    obj.mRtcObserver.onDataChannelMessage(uid, msg);
                }
            });
        }

        @Override
        public void onRemoteAudioAccompanyStarted(String s) {
            AliyunLog.d(TAG, CommonUtil.getCallMethodName());
        }

        @Override
        public void onRemoteAudioAccompanyFinished(String s) {
            AliyunLog.d(TAG, CommonUtil.getCallMethodName());
        }

        @Override
        public void onAudioAccompanyStateChanged(AliRtcEngine.AliRtcAudioAccompanyStateCode playState, AliRtcEngine.AliRtcAudioAccompanyErrorCode errorCode) {
            final ARTCRoomRtcServiceImpl obj = roomRef.get();
            if(obj == null)
                return;
            obj.mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(obj.mRtcObserver != null){
                        obj.mRtcObserver.onAccompanyStateChanged(playState);
                    }
                }
            });
        }
    }

    public static final class RTCAudioVolumeObserver extends AliRtcEngine.AliRtcAudioVolumeObserver{
        private final WeakReference<ARTCRoomRtcServiceImpl> ref;
        private String mCurrentActiveSpeaker;
        public RTCAudioVolumeObserver(ARTCRoomRtcServiceImpl engine){
            ref = new WeakReference<>(engine);
        }

        @Override
        public void onActiveSpeaker(String s) { //监听发言人的变化
            final ARTCRoomRtcServiceImpl obj = ref.get();
            if(obj == null)
                return;
            mCurrentActiveSpeaker = s;
        }

        @Override
        public void onAudioVolume(List<AliRtcEngine.AliRtcAudioVolume> list, int i) {
            final ARTCRoomRtcServiceImpl obj = ref.get();
            if(obj == null || mCurrentActiveSpeaker == null)
                return;
            final boolean[] speaking = new boolean[]{false};
            for(AliRtcEngine.AliRtcAudioVolume volume : list){
                if(TextUtils.equals(mCurrentActiveSpeaker, volume.mUserId)){
                    if(volume.mSpeechstate == SPEAKING_STATE && volume.mVolume >= SPEAKING_VOLUME){ //音量大于等于10，且有在说话
                        speaking[0] = true;
                    }
                    break;
                }
            }
            final String targetUser = CURRENT_UESR_IN_CALLBACK.equals(mCurrentActiveSpeaker) ? obj.mUserId : mCurrentActiveSpeaker;
            obj.mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(obj.mRtcObserver != null)
                        obj.mRtcObserver.onSpeakerActivated(targetUser, speaking[0]);
                }
            });
        }
    }

    @Override
    public void setMode(ClientMode mode) {
        mMode = mode;
    }

    @Override
    public void setCallback(ARTCRoomRtcServiceDelegate callback) {
        this.mRtcObserver = callback;
    }

    @Override
    public void release() {
        if (mRTCEngine != null) {
            mRTCEngine.destroy();
        }
        if(mAudioEffectMap != null)
            mAudioEffectMap.clear();
        mNextAudioEffectId = 1;
    }

    public void initEngine() {
        if(mInit)
            return;
        this.mRTCEngine = AliRtcEngine.getInstance(mContext);;
        if (mRTCEngine != null) {
            mAudioVolumeObserver = new RTCAudioVolumeObserver(this);
            if(mMode == ClientMode.VOICE_ROOM || mMode == ClientMode.KTV){
                mRTCEngine.setChannelProfile(AliRtcEngine.AliRTCSdkChannelProfile.AliRTCSdkInteractiveLive);
                mRTCEngine.setAudioOnlyMode(true);
            }else if(mMode == ClientMode.CALL){
                mRTCEngine.setChannelProfile(AliRtcEngine.AliRTCSdkChannelProfile.AliRTCSdkCommunication);
            }
            for(AliRtcEngine.AliRtcAudioProfile audioPf : AliRtcEngine.AliRtcAudioProfile.values()){
                if(mMode == ClientMode.KTV){
                    mRTCEngine.setAudioProfile(audioPf, AliRtcEngine.AliRtcAudioScenario.AliRtcSceneKtvMode);
                }else{
                    mRTCEngine.setAudioProfile(audioPf, AliRtcEngine.AliRtcAudioScenario.AliRtcSceneMusicMode);
                }
            }
            mRTCEngine.setRtcEngineEventListener(this);
            mRTCEngine.setRtcEngineNotify(this.mEngineRemoteNotify);
            mRTCEngine.registerAudioVolumeObserver(mAudioVolumeObserver);
            mRTCEngine.enableAudioVolumeIndication(500, 3, 1);
            mRTCEngine.setClientRole(AliRtcEngine.AliRTCSdkClientRole.AliRTCSdkLive);
            mRTCEngine.publishLocalAudioStream(false);
            mRTCEngine.publishLocalVideoStream(false);
            mInit = true;
        }
    }

    @Override
    public ClientMode getMode() {
        return mMode;
    }

    @Override
    public int join(RtcChannel rtcChannel) {
        initEngine();
        AliyunLog.d(TAG, "join " + rtcChannel.channelId);
        AliRtcAuthInfo authInfo = new AliRtcAuthInfo();
        authInfo.gslb = new String[]{rtcChannel.gslb};
        authInfo.channelId = rtcChannel.channelId;
        authInfo.appId = rtcChannel.appId;
        authInfo.userId = rtcChannel.userId;
        authInfo.token = rtcChannel.token;
        authInfo.timestamp = rtcChannel.timestamp;
        if (mRTCEngine != null) {
            mRTCEngine.enableSpeakerphone(true);
        }
        int ret = mRTCEngine == null ? -1 : mRTCEngine.joinChannel(authInfo, authInfo.userId);
        AliyunLog.d(TAG, String.format("join channel ret: [%d]", ret));
        if (ret == 0) {
            mCurrentChannelId = rtcChannel.channelId;
        }
        return ret;
    }

    @Override
    public int leave() {
        if(!isJoin()){
            AliyunLog.w(TAG, "user not in channel, leave action invalid");
            return -1;
        }
        if(isPublishing()) //如果正在推流，先停止推流
            stopPublish();
        int ret = mRTCEngine != null ? mRTCEngine.leaveChannel() : -1;
        AliyunLog.d(TAG, String.format("leave channel[%s] ret[%d]", mCurrentChannelId, ret));
        clearRemoteViews();
        return 0;
    }

    private final void clearRemoteViews() {
        List<String> keys = new ArrayList<>(this.mCachePreview.keySet());
        for (String uid : keys) {
            if (!TextUtils.equals(uid, this.mUserId)) {
                setRenderViewLayout(uid, null, false, false);
            }
        }
    }

    @Override
    public void setRenderViewLayout(String uid, View container, boolean isTop, boolean mirror) {
        AliyunLog.d(TAG, "setRenderViewLayout " + uid);
        if (container != null) {
            if (!(container instanceof ViewGroup)) {
                throw new RuntimeException("container should be ViewGroup subclass");
            }
            mCachePreview.put(uid, new PreviewInfo(uid, container, isTop, mirror));
            startPreview(uid);
        } else if (this.mCachePreview.containsKey(uid)) {

            stopPreview(uid);
            this.mCachePreview.remove(uid);
        }
    }

    @Override
    public boolean isJoin() {
        return mIsInChannel;
    }


    @Override
    public int startPublish(boolean pushVideo, boolean videoMute, boolean pushAudio, boolean audioMute) {
        if (!isJoin()) {
            AliyunLog.e(TAG, "startPublish user no in channel");
            return -1;
        }
        if(mRTCEngine == null)
            return -2;
        mRTCEngine.setClientRole(AliRtcEngine.AliRTCSdkClientRole.AliRTCSdkInteractive);
        if (!pushVideo) {
            startPreview(mUserId);
        }
        mRTCEngine.publishLocalVideoStream(pushVideo);
        mRTCEngine.publishLocalAudioStream(pushAudio);
        mRTCEngine.muteLocalCamera(videoMute, AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackCamera);
        mRTCEngine.muteLocalMic(audioMute, AliRtcEngine.AliRtcMuteLocalAudioMode.AliRtcMuteOnlyMicAudioMode);
        mRTCEngine.startAudioCapture();

        this.mIsPublishing = true;
        return 0;
    }

    private final void startPreview(String uid) {
        SurfaceView surfaceView;
        AliyunLog.i(TAG, "startPreview " + uid);
        if (this.mCachePreview.containsKey(uid)) {
            PreviewInfo previewInfo = this.mCachePreview.get(uid);

            View view = previewInfo.getView().get();
            if (view == null)
                return;
            AliRtcEngine.AliRtcVideoCanvas canvas = new AliRtcEngine.AliRtcVideoCanvas();
            if (((ViewGroup) view).getChildCount() == 0) {
                surfaceView = mRTCEngine == null ? null : mRTCEngine.createRenderSurfaceView(mContext);
                ((ViewGroup) view).addView(surfaceView, -1, -1);
                canvas.view = surfaceView;
            } else {
                ((ViewGroup) view).getChildAt(0).setVisibility(View.VISIBLE);
                canvas.view = ((ViewGroup) view).getChildAt(0);
                surfaceView = (SurfaceView) canvas.view;
            }

            if (previewInfo.isTop()) {
                if (surfaceView != null) {
                    surfaceView.setZOrderOnTop(true);
                }
                if (surfaceView != null) {
                    surfaceView.setZOrderMediaOverlay(true);
                }
            } else {
                if (surfaceView != null) {
                    surfaceView.setZOrderOnTop(false);
                }
                if (surfaceView != null) {
                    surfaceView.setZOrderMediaOverlay(false);
                }
            }
            if (TextUtils.equals(uid, this.mUserId)) {
                if (previewInfo.getMirror()) {
                    canvas.mirrorMode = AliRtcEngine.AliRtcRenderMirrorMode.AliRtcRenderMirrorModeOnlyFront;
                } else {
                    canvas.mirrorMode = AliRtcEngine.AliRtcRenderMirrorMode.AliRtcRenderMirrorModeAllDisable;
                }
                mRTCEngine.setLocalViewConfig(null, AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackCamera);
                mRTCEngine.setLocalViewConfig(canvas, AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackCamera);
                mRTCEngine.startPreview();
                return;
            }else{
                mRTCEngine.setRemoteViewConfig(null, uid, AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackCamera);
                mRTCEngine.setRemoteViewConfig(canvas, uid, AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackCamera);
            }
        }
    }

    private final void stopPreview(String uid) {
        AliyunLog.i(TAG, "stopPreview " + uid);
        if(mRTCEngine == null)
            return;
        if (TextUtils.equals(uid, this.mUserId)) {
            mRTCEngine.setLocalViewConfig(null, AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackCamera);
            mRTCEngine.stopPreview();
        } else {
            mRTCEngine.setRemoteViewConfig(null, uid, AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackCamera);
        }
        if (this.mCachePreview.containsKey(uid)) {
            PreviewInfo previewInfo = this.mCachePreview.get(uid);
            View view = previewInfo.getView().get();
            if (view == null)
                return;
            ((ViewGroup) view).getChildAt(0).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int stopPublish() {
        AliyunLog.d(TAG, "stopPublish");
        if (!isPublishing()) {
            AliyunLog.w(TAG, "stopPublish user no in publishing state");
            return -1;
        }
        if(mRTCEngine == null)
            return -2;
        mRTCEngine.stopAudioCapture();
        mRTCEngine.muteLocalCamera(true, AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackCamera);
        mRTCEngine.muteLocalMic(true, AliRtcEngine.AliRtcMuteLocalAudioMode.AliRtcMuteOnlyMicAudioMode);
        mRTCEngine.setClientRole(AliRtcEngine.AliRTCSdkClientRole.AliRTCSdkLive);
        mRTCEngine.publishLocalAudioStream(false);
        mRTCEngine.publishLocalVideoStream(false);
        stopPreview(this.mUserId);
        this.mIsPublishing = false;
        return 0;
    }

    @Override
    public boolean isPublishing() {
        return mIsPublishing;
    }

    @Override
    public int startPreview() {
        if(TextUtils.isEmpty(mUserId))
            return -1;
        startPreview(mUserId);
        return 0;
    }

    @Override
    public int stopPreview() {
        if(TextUtils.isEmpty(mUserId))
            return -1;
        stopPreview(mUserId);
        return 0;
    }

    @Override
    public int switchMicrophone(boolean open) {
        AliyunLog.d(TAG, "switchMicrophone userId: " + mUserId + ", open: " + open);
        if(mRTCEngine == null)
            return -1;
        int rs = mRTCEngine.muteLocalMic(!open, AliRtcEngine.AliRtcMuteLocalAudioMode.AliRtcMuteOnlyMicAudioMode);
        if (open) {
            mRTCEngine.startAudioCapture();
        } else {
            mRTCEngine.stopAudioCapture();
        }
        if (rs == 0) {
            this.mUIHandler.post(new Runnable() {
                @Override
                public final void run() {
                    if (mRtcObserver == null)
                        return;
                    mRtcObserver.onMicrophoneStateChanged(mUserId, open);
                }
            });
        }
        return 0;
    }

    @Override
    public int switchCamera(boolean open) {
        if(mRTCEngine == null)
            return -1;
        int rs = mRTCEngine.muteLocalCamera(!open, AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackCamera);
        if (open) {
            startPreview(this.mUserId);
        } else {
            stopPreview(this.mUserId);
        }
        if (rs == 0) {
            this.mUIHandler.post(new Runnable() {
                @Override
                public final void run() {
                    if (mRtcObserver == null)
                        return;
                    mRtcObserver.onCameraStateChanged(mUserId, open);
                }
            });
        }
        return 0;
    }

    @Override
    public int setCameraType(CameraType type) {
        if(mRTCEngine == null)
            return -1;
        if((type == CameraType.FRONT && mRTCEngine.getCurrentCameraDirection() == AliRtcEngine.AliRtcCameraDirection.CAMERA_FRONT)
                ||(type == CameraType.BACK && mRTCEngine.getCurrentCameraDirection() == AliRtcEngine.AliRtcCameraDirection.CAMERA_REAR)){
        }else{
            mRTCEngine.switchCamera();
        }
        return 0;
    }

    @Override
    public CameraType getCameraType() {
        if(mRTCEngine.getCurrentCameraDirection() == AliRtcEngine.AliRtcCameraDirection.CAMERA_FRONT)
            return CameraType.FRONT;
        return CameraType.BACK;
    }

    @Override
    public int setAudioOutputType(AudioOutputType type) {
        if(mRTCEngine == null)
            return -1;
        if(type == AudioOutputType.SPEAKER){
            mRTCEngine.enableSpeakerphone(true);
        }else{
            mRTCEngine.enableSpeakerphone(false);
        }
        return 0;
    }

    @Override
    public AudioOutputType getAudioOutputType() {
        if(mRTCEngine == null) return AudioOutputType.HEADSET;
        return mRTCEngine.isSpeakerOn() ? AudioOutputType.SPEAKER : AudioOutputType.HEADSET;
    }

    @Override
    public AliRtcEngine getRTCEngine() {
        return mRTCEngine;
    }

    @Override
    public void onJoinChannelResult(final int result, final String channel, final String userId, int elapsed) {
        this.mUIHandler.post(new Runnable() {
            @Override
            public final void run() {
                if(result == 0){
                    mUserId = userId;
                }
                AliyunLog.d(TAG, "join channel result[" + result + "] userId[" + userId + "] channel[" +  channel + ']');
                if (mRtcObserver == null) {
                    return;
                }
                if (result != 0) {
                    mRtcObserver.onError(-1, "user join channel failed!!!");
                } else if (!TextUtils.equals(mCurrentChannelId, channel)) {
                    mRtcObserver.onError(-1, String.format("onJoinChannelResult not equals channel [%s] [%s]", mCurrentChannelId, channel));
                } else {
                    if (!TextUtils.equals(mUserId, userId)) {
                        mRtcObserver.onError(-1, String.format("onJoinChannelResult not equals user [%s] [%s]", mUserId, userId));
                        return;
                    }
                    mIsInChannel = true;
                    mRtcObserver.onJoined(channel, userId);
                }
            }
        });
    }

    @Override
    public void onNetworkQualityChanged(String s, AliRtcEngine.AliRtcNetworkQuality up, AliRtcEngine.AliRtcNetworkQuality down) {
        AliyunLog.d(TAG, "onNetworkQualityChanged uid: " + s + ", up: " + up + ", down: " + down);
        if(mRtcObserver != null){
            String uid = TextUtils.isEmpty(s) ? mUserId : s;
            mRtcObserver.onNetworkStateChanged(uid, down);
        }
    }

    @Override
    public void onLeaveChannelResult(final int p0, final AliRtcEngine.AliRtcStats p1) {
        this.mUIHandler.post(new Runnable() {
            @Override
            public final void run() {
                if(p0 == 0){
                    mUserId = null;
                }
                AliyunLog.d(TAG, "leave channel result[" + p0 + "] userId[" + mUserId + "] channel[" + mCurrentChannelId + "] state[" + p1 + ']');
                if (mRtcObserver == null)
                    return;
                if (p0 != 0) {
                    mRtcObserver.onError(-1, "user leave channel failed!!!");
                    return;
                }
                mUserStateMap.clear();
                mIsInChannel = false;
                mRtcObserver.onLeaved(mUserId);
            }
        });
    }

    @Override
    public void setAudioMixSound(MixSoundType type) {
        AliyunLog.d(TAG, "setAudioMixSound " + type);
        if(mRTCEngine != null){
            AliRtcEngine.AliRtcAudioEffectReverbMode reverbMode = AliRtcEngine.AliRtcAudioEffectReverbMode.AliRtcSdk_AudioEffect_Reverb_Off;
            switch(type){
                case Vocal_I:
                    reverbMode = AliRtcEngine.AliRtcAudioEffectReverbMode.AliRtcSdk_AudioEffect_Reverb_Vocal_I;
                    break;
                case Vocal_II:
                    reverbMode = AliRtcEngine.AliRtcAudioEffectReverbMode.AliRtcSdk_AudioEffect_Reverb_Vocal_II;
                    break;
                case Bathroom:
                    reverbMode = AliRtcEngine.AliRtcAudioEffectReverbMode.AliRtcSdk_AudioEffect_Reverb_Bathroom;
                    break;
                case Small_Room_Bright:
                    reverbMode = AliRtcEngine.AliRtcAudioEffectReverbMode.AliRtcSdk_AudioEffect_Reverb_Small_Room_Bright;
                    break;
                case Small_Room_Dark:
                    reverbMode = AliRtcEngine.AliRtcAudioEffectReverbMode.AliRtcSdk_AudioEffect_Reverb_Small_Room_Dark;
                    break;
                case Medium_Room:
                    reverbMode = AliRtcEngine.AliRtcAudioEffectReverbMode.AliRtcSdk_AudioEffect_Reverb_Medium_Room;
                    break;
                case Large_Room:
                    reverbMode = AliRtcEngine.AliRtcAudioEffectReverbMode.AliRtcSdk_AudioEffect_Reverb_Large_Room;
                    break;
                case Church_Hall:
                    reverbMode = AliRtcEngine.AliRtcAudioEffectReverbMode.AliRtcSdk_AudioEffect_Reverb_Church_Hall;
                    break;
            }
            mRTCEngine.setAudioEffectReverbMode(reverbMode);
        }
    }

    @Override
    public void setVoiceType(VoiceChangeType type) {
        AliyunLog.d(TAG, "setVoiceType " + type);
        if(mRTCEngine != null){
            AliRtcEngine.AliRtcAudioEffectVoiceChangerMode mode = AliRtcEngine.AliRtcAudioEffectVoiceChangerMode.AliRtcSdk_AudioEffect_Voice_Changer_OFF;
            switch (type){
                case Oldman:
                    mode = AliRtcEngine.AliRtcAudioEffectVoiceChangerMode.AliRtcSdk_AudioEffect_Voice_Changer_Oldman;
                    break;
                case Babyboy:
                    mode = AliRtcEngine.AliRtcAudioEffectVoiceChangerMode.AliRtcSdk_AudioEffect_Voice_Changer_Babyboy;
                    break;
                case Babygirl:
                    mode = AliRtcEngine.AliRtcAudioEffectVoiceChangerMode.AliRtcSdk_AudioEffect_Voice_Changer_Babygirl;
                    break;
                case Robot:
                    mode = AliRtcEngine.AliRtcAudioEffectVoiceChangerMode.AliRtcSdk_AudioEffect_Voice_Changer_Robot;
                    break;
                case Daimo:
                    mode = AliRtcEngine.AliRtcAudioEffectVoiceChangerMode.AliRtcSdk_AudioEffect_Voice_Changer_Daimo;
                    break;
                case Ktv:
                    mode = AliRtcEngine.AliRtcAudioEffectVoiceChangerMode.AliRtcSdk_AudioEffect_Voice_Changer_Ktv;
                    break;
                case Echo:
                    mode = AliRtcEngine.AliRtcAudioEffectVoiceChangerMode.AliRtcSdk_AudioEffect_Voice_Changer_Echo;
                    break;
                case Dialect:
                    mode = AliRtcEngine.AliRtcAudioEffectVoiceChangerMode.AliRtcSdk_AudioEffect_Voice_Changer_Dialect;
                    break;
                case Howl:
                    mode = AliRtcEngine.AliRtcAudioEffectVoiceChangerMode.AliRtcSdk_AudioEffect_Voice_Changer_Howl;
                    break;
                case Electronic:
                    mode = AliRtcEngine.AliRtcAudioEffectVoiceChangerMode.AliRtcSdk_AudioEffect_Voice_Changer_Electronic;
                    break;
                case Phonograph:
                    mode = AliRtcEngine.AliRtcAudioEffectVoiceChangerMode.AliRtcSdk_AudioEffect_Voice_Changer_Phonograph;
                    break;
            }
            mRTCEngine.setAudioEffectVoiceChangerMode(mode);
        }
    }

    @Override
    public void setBackgroundMusic(String path, boolean justForTest, int volume) {
        AliyunLog.d(TAG, "setBackgroundMusic " + path);
        if(mRTCEngine == null)
            return;
        if(TextUtils.isEmpty(path)){
            mRTCEngine.stopAudioAccompany();
        }else{
            AliRtcEngine.AliRtcAudioAccompanyConfig playConfig = new AliRtcEngine.AliRtcAudioAccompanyConfig();
            playConfig.loopCycles = 1;
            if(justForTest){
                playConfig.onlyLocalPlay = true;
                playConfig.playoutVolume = volume;
            }else{
                playConfig.publishVolume = volume;
            }
            mRTCEngine.startAudioAccompany(path, playConfig);
        }
    }

    @Override
    public int playAudioEffect(String pathOrUrl, boolean justForTest, int volume) {
        AliyunLog.d(TAG, "playAudioEffect " + pathOrUrl);
        if(TextUtils.isEmpty(pathOrUrl)){
            if(mRTCEngine != null){
                mRTCEngine.stopAllAudioEffects();
            }
            return -1;
        }
        if(!mAudioEffectMap.containsKey(pathOrUrl))
            mAudioEffectMap.put(pathOrUrl, mNextAudioEffectId++);
        if(mRTCEngine != null){
            AliRtcEngine.AliRtcAudioEffectConfig playConfig = new AliRtcEngine.AliRtcAudioEffectConfig();
            playConfig.needPublish = !justForTest;
            playConfig.loopCycles = 1; //只播放一次
            playConfig.publishVolume = volume;
            playConfig.playoutVolume = volume;
            mRTCEngine.playAudioEffect(mAudioEffectMap.get(pathOrUrl), pathOrUrl, playConfig);
            return mAudioEffectMap.get(pathOrUrl);
        }
        return -1;
    }

    @Override
    public void enableEarBack(boolean enable) {
        if(mRTCEngine == null)
            return;
        mRTCEngine.enableEarBack(enable);
    }

    @Override
    public void setRecordingVolume(int volume) {
        if(mRTCEngine == null)
            return;
        if(volume > 50){ //mapping [100,400]
            int offset = volume - 50;
            int rs = (int)(((float)offset / 50.0) * 300);
            mRTCEngine.setRecordingVolume(100 + rs);
        }else{ //mapping [0,100]
             int rs = (int)(((float)volume / 50.0) * 100);
             mRTCEngine.setRecordingVolume(rs);
        }
    }

    @Override
    public void setAudioEffectVolume(int soundId, int volume) {
        if(mRTCEngine == null)
            return;
        mRTCEngine.setAudioEffectPublishVolume(soundId, volume);
        mRTCEngine.setAudioEffectPlayoutVolume(soundId, volume);
    }

    @Override
    public void setAccompanyVolume(int volume) {
        if(mRTCEngine == null)
            return;
        mRTCEngine.setAudioAccompanyVolume(volume);
    }

    @Override
    public void onOccurWarning(int p0, String p1) {
        AliyunLog.w(TAG, "onOccurWarning " + p0 + " : " + ((Object) p1));
    }

    @Override
    public void onOccurError(final int p0, final String p1) {
        this.mUIHandler.post(new Runnable() {
            @Override
            public final void run() {
                AliyunLog.e(TAG, "onOccurError " + p0 + " : " + p1);
                if (mRtcObserver == null)
                    return;
                mRtcObserver.onError(p0, p1);
            }
        });
    }

    public String getCurrentUser(){
        return this.mUserId;
    }
}
