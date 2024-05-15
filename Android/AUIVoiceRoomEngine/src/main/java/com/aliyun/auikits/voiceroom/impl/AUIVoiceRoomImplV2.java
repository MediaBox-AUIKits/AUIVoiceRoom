package com.aliyun.auikits.voiceroom.impl;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.alivc.auimessage.listener.MessageListener;
import com.alivc.auimessage.model.base.AUIMessageModel;
import com.alivc.auimessage.model.base.AUIMessageUserInfo;
import com.alivc.auimessage.model.message.ExitGroupMessage;
import com.alivc.auimessage.model.message.JoinGroupMessage;
import com.alivc.auimessage.model.message.LeaveGroupMessage;
import com.alivc.auimessage.model.message.MuteGroupMessage;
import com.alivc.auimessage.model.message.UnMuteGroupMessage;
import com.alivc.auimessage.model.token.IMNewToken;
import com.alivc.rtc.AliRtcEngine;
import com.aliyun.auikits.common.AliyunLog;
import com.aliyun.auikits.im.IMLoginInfo;
import com.aliyun.auikits.im.IMParams;
import com.aliyun.auikits.im.IMService;
import com.aliyun.auikits.room.ARTCRoomServiceInterface;
import com.aliyun.auikits.room.RoomUserState;
import com.aliyun.auikits.room.Constant;
import com.aliyun.auikits.room.KickOutMicInfo;
import com.aliyun.auikits.room.LeaveMicInfo;
import com.aliyun.auikits.room.RequestMicInfo;
import com.aliyun.auikits.room.impl.ARTCRoomServiceImpl;
import com.aliyun.auikits.common.util.CommonUtil;
import com.aliyun.auikits.rtc.ARTCRoomRtcService;
import com.aliyun.auikits.rtc.ARTCRoomRtcServiceDelegate;
import com.aliyun.auikits.rtc.ClientMode;
import com.aliyun.auikits.rtc.RtcChannel;
import com.aliyun.auikits.rtc.impl.ARTCRoomRtcServiceImpl;
import com.aliyun.auikits.voice.ARTCVoiceRoomEngine;
import com.aliyun.auikits.voice.ARTCVoiceRoomEngineDelegate;
import com.aliyun.auikits.voice.AudioOutputType;
import com.aliyun.auikits.voiceroom.bean.AccompanyPlayState;
import com.aliyun.auikits.voiceroom.bean.AudioEffect;
import com.aliyun.auikits.voiceroom.bean.MicInfo;
import com.aliyun.auikits.voiceroom.bean.MicRequestResult;
import com.aliyun.auikits.voiceroom.bean.MixSound;
import com.aliyun.auikits.voiceroom.bean.Music;
import com.aliyun.auikits.voiceroom.bean.NetworkState;
import com.aliyun.auikits.voiceroom.bean.RoomInfo;
import com.aliyun.auikits.voiceroom.bean.RoomState;
import com.aliyun.auikits.voiceroom.bean.UserInfo;
import com.aliyun.auikits.voiceroom.bean.VoiceChange;
import com.aliyun.auikits.voiceroom.callback.ActionCallback;
import com.aliyun.auikits.voiceroom.external.RtcInfo;
import com.aliyun.auikits.voiceroom.module.seat.SeatInfo;
import com.aliyun.auikits.voiceroom.module.seat.protocol.Params;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class AUIVoiceRoomImplV2 implements ARTCVoiceRoomEngine, ARTCRoomRtcServiceDelegate, MessageListener {
    protected ARTCRoomRtcService mRTCService;
    protected ARTCRoomServiceInterface mRoomService;
    protected CopyOnWriteArrayList<ARTCVoiceRoomEngineDelegate> mRoomCallbacks = new CopyOnWriteArrayList<>();
    protected UserInfo mCurrentUser;
    protected boolean mDebug = true;
    private static final String TAG = "room2";
    protected RoomState mRoomState = RoomState.UN_INIT;
    protected RoomInfo mRoomInfo;
    protected AudioOutputType mAudioOutputType = AudioOutputType.LOUDSPEAKER;
    protected Map<String, UserInfo> mMicUsers = new Hashtable<>();
    protected Map<String, UserInfo> mCacheUserMap = new Hashtable<>();
    protected boolean mOnMuted = false; //默认不被禁言
    protected final Map<String, RoomUserState> mUserStateMap = new LinkedHashMap();
    protected String mAppId;
    protected int mMemberCount = 0;
    protected boolean mAnchorBindRoom = true; //房间是否绑定主持人，如果绑定主持人，主持人离开就需要解散房间

    @Override
    public void init(Context context, ClientMode mode, String appId, UserInfo userInfo, IMNewToken token, ActionCallback callback) {
        debugInfo(String.format("appId: %s user: %s", appId, userInfo.userId));
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                mAppId = appId;
                mCurrentUser = userInfo;
                mRTCService = new ARTCRoomRtcServiceImpl(context); //rtc接口服务初始化
                mRTCService.setMode(mode);
                mRTCService.setCallback(AUIVoiceRoomImplV2.this); //设置rtc回调
                mRoomService = new ARTCRoomServiceImpl(mode);
                IMLoginInfo imLoginInfo = new IMLoginInfo.Builder()
                        .appId(token.app_id)
                        .appSign(token.app_sign)
                        .appToken(token.app_token)
                        .once(token.auth.nonce)
                        .role(token.auth.role)
                        .timestamp(token.auth.timestamp)
                        .userId(userInfo.userId)
                        .nickName(userInfo.userName)
                        .avatar(userInfo.avatarUrl)
                        .deviceId(userInfo.deviceId)
                        .build();
                IMService.getInstance().setLoginInfo(imLoginInfo); //设置IM登录相关信息
                IMService.getInstance().register(AUIVoiceRoomImplV2.this, callback); //IM注册回调
            }
        });
    }

    @Override
    public void release() {
        debugInfo("");
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                if(isJoinRoom()){
                    if(mAnchorBindRoom && isAnchor()){
                        dismissRoom(null); //主持人且房间绑定主持人,离开需要解散房间
                    }else{
                        leaveRoom(null); //非主持人只是离开房间
                    }
                }
                if(mRTCService != null){
                    mRTCService.setCallback(null);
                    mRTCService.release();
                    mRTCService = null;
                }
                mRoomService = null;
                mRoomState = RoomState.UN_INIT;
                IMService.getInstance().unregister(AUIVoiceRoomImplV2.this);
            }
        });
    }

    @Override
    public void addObserver(ARTCVoiceRoomEngineDelegate callback) {
        if(callback != null) {
            this.mRoomCallbacks.add(callback);
        }
    }

    @Override
    public void removeObserver(ARTCVoiceRoomEngineDelegate callback) {
        if(callback != null) {
            this.mRoomCallbacks.remove(callback);
        }
    }

    @Override
    public void createRoom(RoomInfo roomInfo, ActionCallback callback) {
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                if(!isRoomInfoValid(roomInfo)){
                    CommonUtil.actionCallback(callback, -1, "room info invalid", null);
                    return;
                }
                if(!checkCurrentUser(callback))
                    return;
                debugInfo(String.format("createRoom roomId: %s", roomInfo.roomId));
                roomInfo.creator = mCurrentUser;
                IMService.getInstance().createGroup(roomInfo.roomId, callback);
            }
        });
    }

    @Override
    public void joinRoom(RoomInfo roomInfo, RtcInfo rtcInfo, ActionCallback callback) {
        debugInfo(String.format("room: %s", roomInfo.roomId));
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                if(!checkCurrentUser(callback))
                    return;
                if(!isRoomInfoValid(roomInfo)){
                    CommonUtil.actionCallback(callback, -1, "room info invalid", null);
                    return;
                }
                if (!checkLogin(callback))
                    return;
                if (isJoinRoom()) {
                    CommonUtil.actionCallback(callback, 0, "user already in room");
                    return;
                }
                mRoomInfo = roomInfo;
                IMService.getInstance().joinGroup(roomInfo.roomId, AUIVoiceRoomImplV2.this, new ActionCallback() {
                    @Override
                    public void onResult(int code, String msg, Map<String, Object> params) {
                        if(code == 0){
                            RtcChannel rtcChannel = new RtcChannel.Builder()
                                    .appId(mAppId)
                                    .channel(roomInfo.roomId)
                                    .gslb(rtcInfo.gslb)
                                    .token(rtcInfo.token)
                                    .timestamp(rtcInfo.timestamp)
                                    .uid(mCurrentUser.userId)
                                    .build();
                            if(mRTCService.join(rtcChannel) == 0){
                                CommonUtil.actionCallback(callback, 0, null); //成功
                            }else{
                                CommonUtil.actionCallback(callback, -1, "rtc join channel failed"); //失败
                            }
                        }else{
                            CommonUtil.actionCallback(callback, code, msg, params);  //失败
                        }
                    }
                });
            }
        });
    }

    private final boolean checkLogin(ActionCallback callback) {
        if (!IMService.getInstance().isLogin()) {
            CommonUtil.actionCallback(callback, -1, "user in logout state");
            return false;
        }
        return true;
    }

    private boolean isRoomInfoValid(RoomInfo info){
        if(info == null)
            return false;
        return true;
    }

    private boolean checkCurrentUser(ActionCallback callback){
        if(mCurrentUser == null){
            CommonUtil.actionCallback(callback, -1, "current user null");
            return false;
        }
        return true;
    }

    private boolean checkInRoom(ActionCallback callback){
        if(mRoomState != RoomState.IN_ROOM){
            CommonUtil.actionCallback(callback, -1, "action in wrong state " + mRoomState.getName(), null);
            return false;
        }
        return true;
    }

    private boolean checkRoomOwnerExists(ActionCallback callback){
        if(mRoomInfo == null || null == mRoomInfo.creator) {
            CommonUtil.actionCallback(callback, -1, "room creator null", null);
            return false;
        }
        return true;
    }

    private boolean checkRoomState(ActionCallback callback){
        if(mRTCService == null){
            CommonUtil.actionCallback(callback, -1, "room state invalid");
            return false;
        }
        if(!isJoinRoom()){
            CommonUtil.actionCallback(callback, -1, "user not in room");
            return false;
        }
        return true;
    }

    private boolean checkUserInMic(ActionCallback callback){
        if(mRoomState != RoomState.IN_MIC){
            CommonUtil.actionCallback(callback, -1, "user in wrong state " + mRoomState.getName(), null);
            return false;
        }
        return true;
    }

    private boolean checkRoomService(ActionCallback callback){
        if(mRoomService == null){
            CommonUtil.actionCallback(callback, -1, "room service null");
            return false;
        }
        return true;
    }

    private boolean checkRtcService(ActionCallback callback){
        if(mRTCService == null){
            CommonUtil.actionCallback(callback, -1, "rtc service null");
            return false;
        }
        return true;
    }

    @Override
    public void leaveRoom(ActionCallback callback) {
        debugInfo("");
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                if(!checkRoomState(callback) || !checkCurrentUser(callback))
                    return;
                if(mRoomState == RoomState.IN_MIC){ //需要先下麦
                    leaveMic(new ActionCallback() {
                        @Override
                        public void onResult(int code, String msg, Map<String, Object> params) {
                            if(code != 0){
                                CommonUtil.actionCallback(callback, code, msg);
                                return;
                            }
                            innerLeaveRoom(callback);
                        }
                    });
                }else{
                    innerLeaveRoom(callback);
                }
            }
        });
    }

    private void innerLeaveRoom(ActionCallback callback){
        debugInfo("");
        if(!isJoinRoom()){
            debugInfo("leave action when not in room");
            return;
        }
        int ret = mRTCService.leave();
        if (ret != 0) {
            CommonUtil.actionCallback(callback, ret, "rtc leave failed");
            return;
        }
        IMService.getInstance().leaveGroup(mRoomInfo.roomId, callback);
    }

    @Override
    public void dismissRoom(ActionCallback callback) {
        debugInfo(String.format("room: %s, state: %s", mRoomInfo.roomId, mRoomState.getName()));
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                if(!isAnchor()){ //非主持人没有权限
                    CommonUtil.actionCallback(callback, -1, "dismissRoom have not permission");
                    return;
                }
                if(!checkRoomState(callback) || !checkCurrentUser(callback))
                    return;
                JSONObject dismissJson = new JSONObject();
                try {
                    dismissJson.put(Constant.KEY_TYPE, IMParams.MSG_TYPE_DISMISS_ROOM);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                IMService.getInstance().sendGroupMessage(mRoomInfo.roomId, IMParams.MSG_TYPE_DISMISS_ROOM, dismissJson.toString());
            }
        });
    }

    @Override
    public void requestMic(ActionCallback callback) {
        debugInfo("");
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                if(mRoomState == RoomState.IN_MIC){
                    CommonUtil.actionCallback(callback, 0, "user already in mic");
                    return;
                }
                if(!checkCurrentUser(callback))
                    return;
                //检查房间状态是否符合预期
                if(!checkInRoom(callback))
                    return;
                if(!checkRoomOwnerExists(callback))
                    return;
                RequestMicInfo requestMicInfo = new RequestMicInfo();
                requestMicInfo.roomInfo = mRoomInfo;
                requestMicInfo.userInfo = mCurrentUser;
                mRoomService.requestMic(requestMicInfo, new ActionCallback() {
                    @Override
                    public void onResult(int code, String msg, Map<String, Object> params) {
                        if(code == 0){
                            JSONObject rs = (JSONObject) params.get(Params.KEY_RESPONSE);
                            onResponseRequestMic(rs);
                            CommonUtil.actionCallback(callback, 0, null, null);
                        }else{
                            CommonUtil.actionCallback(callback, code, msg, null);
                        }
                    }
                });
            }
        });
    }

    private void onResponseRequestMic(JSONObject data){
        debugInfo("");
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                mRoomState = RoomState.IN_ROOM; //重置状态
                int reason = 0;
                int micPos = -1;
                if(data != null){
                    if(data.has(Params.KEY_REASON)){
                        reason = data.optInt(Params.KEY_REASON);
                    }
                    micPos = data.optInt(Constant.KEY_INDEX, -1);
                }
                MicRequestResult rs = new MicRequestResult(reason, micPos);
                for(ARTCVoiceRoomEngineDelegate c : mRoomCallbacks) {
                    c.onResponseMic(rs);
                }
            }
        });
    }

    @Override
    public void joinMic(MicInfo micInfo, ActionCallback callback) {
        debugInfo(String.format("joinMic mute: %b, pos: %d", micInfo.audioMute, micInfo.position));
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                if(!checkCurrentUser(callback))
                    return;
                mCurrentUser.isMute = micInfo.audioMute;
                mCurrentUser.micPosition = micInfo.position;
                innerJoinMic(micInfo.audioMute);
                CommonUtil.actionCallback(callback, 0, "", null);
            }
        });
    }

    private void innerJoinMic(boolean audioMute){
        debugInfo(String.format("audioMute: %b", audioMute));
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                mRTCService.startPublish(false, true, true, audioMute);
                mRoomState = RoomState.IN_MIC;
                final boolean mute = audioMute;
                final UserInfo userInfo = mCurrentUser;

                for(ARTCVoiceRoomEngineDelegate c : mRoomCallbacks) {
                    c.onJoinedMic(userInfo);
                }
                for(ARTCVoiceRoomEngineDelegate c : mRoomCallbacks) {
                    c.onMicUserMicrophoneChanged(userInfo, !mute);
                }

                JSONObject jsonObj = new JSONObject();
                try {
                    jsonObj.put(Constant.KEY_USER_ID, mCurrentUser.userId);
                    jsonObj.put(Constant.KEY_USER_NICK_NAME, mCurrentUser.userName);
                    jsonObj.put(Constant.KEY_USER_AVATAR, mCurrentUser.avatarUrl);
                    jsonObj.put(Constant.KEY_SEAT_INDEX, mCurrentUser.micPosition);
                } catch (JSONException e) {
                    e.printStackTrace();
                    debugInfo("" + e.getMessage());
                    return;
                }
                //发送麦位信息同步通知
                IMService.getInstance().sendGroupMessage(mRoomInfo.roomId, IMParams.MSG_TYPE_SYNC_JOIN_MIC, jsonObj.toString());
            }
        });
    }

    @Override
    public void leaveMic(ActionCallback callback) {
        debugInfo("");
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                //检查用户状态
                if(!checkUserInMic(callback) || !checkCurrentUser(callback))
                    return;
                mRoomState = RoomState.IN_ROOM;
                final UserInfo leaveUser = mCurrentUser;
                if(isAnchor()){
                    innerLeaveMic(leaveUser, callback);
                }else{
                    innerLeaveMic(leaveUser, new ActionCallback() {
                        @Override
                        public void onResult(int code, String msg, Map<String, Object> params) {
                            if(code != 0){
                                CommonUtil.actionCallback(callback, code, msg, null);
                                return;
                            }
                            LeaveMicInfo leaveMicInfo = new LeaveMicInfo();
                            leaveMicInfo.userInfo = mCurrentUser;
                            leaveMicInfo.roomInfo = mRoomInfo;
                            leaveMicInfo.micPos = mCurrentUser.micPosition;
                            leaveMicInfo.isHost = isAnchor(mCurrentUser);
                            mRoomService.leaveMic(leaveMicInfo, callback);
                        }
                    });
                }
            }
        });
    }

    private void innerLeaveMic(UserInfo user, ActionCallback callback){
        debugInfo("");
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                for(ARTCVoiceRoomEngineDelegate c : mRoomCallbacks) {
                    c.onLeavedMic(user);
                }
                mRTCService.switchMicrophone(true); //下麦恢复麦克风开关状态为默认开
                mRTCService.stopPublish();
                JSONObject jsonObj = new JSONObject();
                try {
                    jsonObj.put(Constant.KEY_USER_ID, user.userId);
                    jsonObj.put(Constant.KEY_SEAT_INDEX, user.micPosition);
                } catch (JSONException e) {
                    e.printStackTrace();
                    CommonUtil.actionCallback(callback, -1, "" + e.getMessage(), null);
                    return;
                }
                resetAudioSettings();
                //发送麦位信息同步通知
                IMService.getInstance().sendGroupMessage(mRoomInfo.roomId, IMParams.MSG_TYPE_SYNC_LEAVE_MIC, jsonObj.toString());
                CommonUtil.actionCallback(callback, 0, null);
            }
        });
    }

    @Override
    public void switchMicrophone(boolean open) {
        if(mCurrentUser == null){
            debugInfo("room unInit");
            return;
        }
        mRTCService.switchMicrophone(open);
    }

    @Override
    public void setAudioOutputType(AudioOutputType type) {
        mAudioOutputType = type;
        if(AudioOutputType.LOUDSPEAKER == type){
            mRTCService.setAudioOutputType(com.aliyun.auikits.rtc.AudioOutputType.SPEAKER);
        }else{
            mRTCService.setAudioOutputType(com.aliyun.auikits.rtc.AudioOutputType.HEADSET);
        }
    }

    @Override
    public AudioOutputType getAudioOutputType() {
        return mAudioOutputType;
    }

    @Override
    public UserInfo getCurrentUser() {
        return mCurrentUser;
    }

    @Override
    public void sendTextMessage(String message, ActionCallback callback) {
        JSONObject jsonObj = new JSONObject();
        try{
            jsonObj.put(Constant.KEY_TYPE, IMParams.MSG_TYPE_SEND_TEXT);
            jsonObj.put(Constant.KEY_CONTENT, message);
        }catch (JSONException e){
            e.printStackTrace();
            CommonUtil.actionCallback(callback, -1, "" + e.getMessage(), null);
            return;
        }
        IMService.getInstance().sendGroupMessage(mRoomInfo.roomId, IMParams.MSG_TYPE_SEND_TEXT, jsonObj.toString());
        CommonUtil.actionCallback(callback, 0, null);
    }

    @Override
    public void sendCommand(UserInfo user, int type, String protocol, ActionCallback callback) {
        JSONObject jsonObj = new JSONObject();
        try{
            jsonObj.put(Constant.KEY_TYPE, type);
            jsonObj.put(Constant.KEY_CONTENT, protocol);
        }catch (JSONException e){
            e.printStackTrace();
            CommonUtil.actionCallback(callback, -1, "" + e.getMessage(), null);
            return;
        }
        IMService.getInstance().sendMessage(mRoomInfo.roomId, user.userId, type, jsonObj.toString());
        CommonUtil.actionCallback(callback, 0,null);
    }

    @Override
    public boolean isAnchor() {
        return isAnchor(mCurrentUser);
    }

    @Override
    public boolean isAnchor(UserInfo userInfo) {
        if(userInfo == null || TextUtils.isEmpty(userInfo.userId) || mRoomInfo == null)
            return false;
        if(userInfo.userId.equals(mRoomInfo.creator.userId))
            return true;
        return false;
    }

    private boolean isAnchor(String userId){
        UserInfo user = new UserInfo(userId, "unknown");
        return isAnchor(user);
    }

    @Override
    public boolean isJoinRoom() {
        return mRoomState.val() >= RoomState.IN_ROOM.val();
    }

    @Override
    public boolean isJoinMic() {
        return mRoomState.val() >= RoomState.IN_MIC.val();
    }

    @Override
    public AliRtcEngine getRTCEngine() {
        if(mRTCService == null)
            return null;
        return mRTCService.getRTCEngine();
    }

    @Override
    public void mute(UserInfo user, boolean mute, ActionCallback callback) {
        if(!checkRoomService(callback))
            return;
        int ret = mRoomService.mute(mRoomInfo.roomId, user, mute);
        if(ret != 0){
            CommonUtil.actionCallback(callback, ret, "mute failed");
        }else{
            CommonUtil.actionCallback(callback, 0, null);
        }
    }

    @Override
    public void muteAll(boolean mute, ActionCallback callback) {
        if(!isAnchor()){ //非主持人没有权限
            CommonUtil.actionCallback(callback, -1, "muteAll have not permission");
            return;
        }
        if(!checkRoomService(callback))
            return;
        mRoomService.muteAll(mRoomInfo.roomId, mute);
        CommonUtil.actionCallback(callback, 0, null);
    }

    @Override
    public void kickOut(UserInfo user, ActionCallback callback) {
        if(!isAnchor()){ //非主持人没有权限
            CommonUtil.actionCallback(callback, -1, "kickOut have not permission", null);
            return;
        }
        if(!checkRoomService(callback))
            return;
        KickOutMicInfo kickOutMicInfo = new KickOutMicInfo();
        kickOutMicInfo.roomInfo = mRoomInfo;
        kickOutMicInfo.userInfo = user;
        int ret = mRoomService.kickOutMic(kickOutMicInfo);
        if(ret == 0){
            CommonUtil.actionCallback(callback, 0, null);
        }else{
            CommonUtil.actionCallback(callback, ret, "kick out failed");
        }
    }

    @Override
    public void listMicUserList(ActionCallback callback) {
        if(!checkRoomService(callback))
            return;
        mRoomService.getMicList(mRoomInfo.roomId, new ActionCallback() {
            @Override
            public void onResult(int code, String msg, Map<String, Object> params) {
                if(code == 0){
                    List<SeatInfo> seats = (List<SeatInfo>)params.get(Constant.KEY_SEAT_LIST);
                    combineSeatUsers(seats);
                    List<UserInfo> result = new ArrayList<>(mMicUsers.values());
                    for(ARTCVoiceRoomEngineDelegate c : mRoomCallbacks){
                        c.onRoomMicListChanged(result);
                    }
                    CommonUtil.actionCallback(callback, 0, null);
                }else{
                    CommonUtil.actionCallback(callback, -1, "listMicUserList failed");
                }
            }
        });
    }

    private void combineSeatUsers(List<SeatInfo> seats){
        //以服务端返回为准
        Map<String, UserInfo> copyMap = new Hashtable<>(mMicUsers);
        mMicUsers.clear();
        for(SeatInfo s : seats){
            if(TextUtils.isEmpty(s.userId))
                continue;
            UserInfo oldU = copyMap.get(s.userId);
            UserInfo newU = new UserInfo(s.userId, "unknown");
            if(oldU != null){
                newU.isMute = oldU.isMute;
            }
            newU.userName = s.userName;
            newU.avatarUrl = s.userAvatar;
            newU.micPosition = s.seatIndex;
            syncOffMicUserInfo(newU);
            mMicUsers.put(s.userId, newU);
        }
        debugInfo(String.format("user in mic count: %d", mMicUsers.size()));
    }

    //同步在没在麦上的用户的麦开关状态
    private void syncOffMicUserInfo(UserInfo userInfo){
        if(!mCacheUserMap.containsKey(userInfo.userId))
            return;
        //同步状态
        UserInfo cache = mCacheUserMap.get(userInfo.userId);
        userInfo.isMute = cache.isMute;
        mCacheUserMap.remove(userInfo.userId);
    }

    @Override
    public RoomInfo getRoomInfo() {
        return mRoomInfo;
    }

    @Override
    public void setAudioMixSound(MixSound mix) {
        if(mRTCService != null)
            mRTCService.setAudioMixSound(mix.mixSoundType);
    }

    @Override
    public void setVoiceChange(VoiceChange change) {
        if(mRTCService == null)
            return;
        mRTCService.setVoiceType(change.voiceType);
    }

    @Override
    public void setBackgroundMusic(Music music) {
        if(mRTCService == null)
            return;
        if(music == null){
            mRTCService.setBackgroundMusic(null, true, 0);
        }else{
            mRTCService.setBackgroundMusic(music.path, music.justForTest, music.volume);
        }
    }

    @Override
    public int playAudioEffect(AudioEffect effect) {
        if(mRTCService == null)
            return -1;
        if(effect == null){
            return mRTCService.playAudioEffect(null, true, 0);
        }else{
            return mRTCService.playAudioEffect(effect.pathOrUrl, effect.justForTest, effect.volume);
        }
    }

    @Override
    public void enableEarBack(boolean enable) {
        if(mRTCService == null)
            return;
        mRTCService.enableEarBack(enable);
    }

    @Override
    public void setRecordingVolume(int volume) {
        if(mRTCService == null)
            return;
        mRTCService.setRecordingVolume(volume);
    }

    @Override
    public void setAccompanyVolume(int volume) {
        if(mRTCService == null)
            return;
        mRTCService.setAccompanyVolume(volume);
    }

    @Override
    public void setAudioEffectVolume(int soundId, int volume) {
        if(mRTCService == null)
            return;
        mRTCService.setAudioEffectVolume(soundId, volume);
    }

    @Override
    public int getMemberCount() {
        return mMemberCount;
    }

    @Override
    public void onJoined(String channelId, String userId) {
        debugInfo(String.format("channelId: %s, userId: %s", channelId, userId));
        mRoomState = RoomState.IN_ROOM;
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                for(ARTCVoiceRoomEngineDelegate c : mRoomCallbacks) {
                    c.onJoin(channelId, userId);
                }
            }
        });

        if(isAnchor()){ //主持人加入房间需要直接上麦
            innerJoinMic(mCurrentUser.isMute);
        }

        IMService.getInstance().getGroupInfo(channelId, new ActionCallback() {
            @Override
            public void onResult(int code, String msg, Map<String, Object> params) {
                if(code == 0){
                    int count = (int)params.get("onlineCount");
                    CommonUtil.runOnUI(new Runnable() {
                        @Override
                        public void run() {
                            mMemberCount = count;
                            for(ARTCVoiceRoomEngineDelegate callback : mRoomCallbacks){
                                callback.onMemberCountChanged(count);
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onLeaved(String userId) {
        debugInfo(String.format("room: %s, state: %s", mRoomInfo.roomId, mRoomState.getName()));
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                resetAudioSettings();
                resetRoom();
                for(ARTCVoiceRoomEngineDelegate c : mRoomCallbacks) {
                    c.onLeave();
                }
            }
        });
    }

    //加入房间之前的状态
    private void resetRoom(){
        mMemberCount = 0;
        mOnMuted = false;
        mRoomState = RoomState.INIT;
        mMicUsers.clear();
        mCacheUserMap.clear();
        mUserStateMap.clear();
        mRoomInfo = null;
    }

    private void resetAudioSettings(){
        setBackgroundMusic(null); //停止背景音乐播放
        playAudioEffect(null); //停止音效
        //TODO:恢复混响和变声设置
    }

    @Override
    public void onStartedPublish(String userId) {
        mUserStateMap.put(userId, RoomUserState.Publishing);
    }

    @Override
    public void onStoppedPublish(String userId) {
        mUserStateMap.put(userId, RoomUserState.Online);
    }

    @Override
    public void onMicrophoneStateChanged(String userId, boolean open) {
        debugInfo(String.format(" userId: %s, open: %b", userId, open));
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                UserInfo user = mMicUsers.containsKey(userId) ? mMicUsers.get(userId) : (TextUtils.equals(mCurrentUser.userId, userId) ? mCurrentUser : null);
                if(user == null){
                    user = new UserInfo(userId, "unknown");
                    mCacheUserMap.put(user.userId, user);
                }
                user.isMute = !open;
                for(ARTCVoiceRoomEngineDelegate c : mRoomCallbacks) {
                    c.onMicUserMicrophoneChanged(user, open);
                }
            }
        });
    }

    @Override
    public void onCameraStateChanged(String userId, boolean open) {

    }

    @Override
    public void onDataChannelMessage(String userId, AliRtcEngine.AliRtcDataChannelMsg msg) {
        debugInfo(String.format("data channel receive msg from: %s", userId));
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                for(ARTCVoiceRoomEngineDelegate c : mRoomCallbacks){
                    c.onDataChannelMessage(userId, msg);
                }
            }
        });
    }

    @Override
    public void onNetworkStateChanged(String userId, AliRtcEngine.AliRtcNetworkQuality quality) {
        debugInfo(String.format("userId: %s, state: %s", userId, quality.name()));
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                UserInfo user = mMicUsers.containsKey(userId) ? mMicUsers.get(userId) : (mCurrentUser.userId.equals(userId) ? mCurrentUser : null);
                if(user == null){
                    user = new UserInfo(userId, "unknown");
                }
                user.networkState = convert2NetworkState(quality); //刷新网络状态
                for(ARTCVoiceRoomEngineDelegate c : mRoomCallbacks) {
                    c.onNetworkStateChanged(user);
                }
            }
        });
    }

    private NetworkState convert2NetworkState(AliRtcEngine.AliRtcNetworkQuality quality){
        if(quality == AliRtcEngine.AliRtcNetworkQuality.AliRtcNetworkExcellent){
            return NetworkState.EXCELLENT;
        }else if(quality == AliRtcEngine.AliRtcNetworkQuality.AliRtcNetworkGood){
            return NetworkState.NORMAL;
        }else if(quality == AliRtcEngine.AliRtcNetworkQuality.AliRtcNetworkPoor
                || quality == AliRtcEngine.AliRtcNetworkQuality.AliRtcNetworkBad
                || quality == AliRtcEngine.AliRtcNetworkQuality.AliRtcNetworkVeryBad){
            return NetworkState.WEAK;
        }else if(quality == AliRtcEngine.AliRtcNetworkQuality.AliRtcNetworkDisconnected){
            return NetworkState.DISCONNECT;
        }else {
            return NetworkState.UNKNOWN;
        }
    }

    @Override
    public void onAudioVolumeChanged(String userId) {

    }

    @Override
    public void onSpeakerActivated(String userId, boolean speaking) {
//        debugInfo(String.format("userId: %s, speaking: %b", userId, speaking));
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                UserInfo user = mMicUsers.containsKey(userId) ? mMicUsers.get(userId) : (mCurrentUser != null && mCurrentUser.userId.equals(userId) ? mCurrentUser : null);
                if(user == null){
                    user = new UserInfo(userId, "unknown");
                }
                user.speaking = speaking; //标记当前正在发言
                for(ARTCVoiceRoomEngineDelegate c : mRoomCallbacks) {
                    c.onMicUserSpeakStateChanged(user);
                }
            }
        });
    }

    @Override
    public void onJoinTokenWillExpire() {
        debugInfo("");
    }

    @Override
    public void onAccompanyStateChanged(AliRtcEngine.AliRtcAudioAccompanyStateCode state) {
        if(state == AliRtcEngine.AliRtcAudioAccompanyStateCode.AliRtcAudioAccompanyStarted){
            CommonUtil.runOnUI(new Runnable() {
                @Override
                public void run() {
                    for(ARTCVoiceRoomEngineDelegate callback : mRoomCallbacks){
                        callback.onAccompanyStateChanged(AccompanyPlayState.STARTED);
                    }
                }
            });
        }else {
            CommonUtil.runOnUI(new Runnable() {
                @Override
                public void run() {
                    for(ARTCVoiceRoomEngineDelegate callback : mRoomCallbacks){
                        callback.onAccompanyStateChanged(AccompanyPlayState.STOPPED);
                    }
                }
            });
        }
    }

    @Override
    public void onError(int code, String msg) {
        debugInfo(String.format(" code: %d, msg: %s", code, msg));
        for(ARTCVoiceRoomEngineDelegate callback : mRoomCallbacks){
            callback.onError(code, msg);
        }
    }

    @Override
    public void onJoinGroup(AUIMessageModel<JoinGroupMessage> message) {
        AliyunLog.d(TAG, "onJoinGroup " + message.data.userId);
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                innerUserOnlineNotify(message.groupId, message.senderInfo, message.data.onlineCount);
            }
        });
    }

    private void innerUserOfflineNotify(String roomId, final AUIMessageUserInfo user){
        mUserStateMap.remove(user.userId);
        debugInfo(String.format(" uid: %s, room: %s", user.userId, roomId));
        if(!TextUtils.equals(roomId, mRoomInfo.roomId)){ //非当前房间的事件
            debugInfo(String.format("out room offline notify, room: %s, uid: %s", roomId, user.userId));
            return;
        }
        if(mMemberCount > 0){
            mMemberCount -= 1;
        }
        UserInfo userInfo = new UserInfo(user.userId, "unknown");
        userInfo.userName = user.userNick;
        userInfo.avatarUrl = user.userAvatar;
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                for(ARTCVoiceRoomEngineDelegate c : mRoomCallbacks) {
                    c.onLeavedRoom(userInfo);
                }
            }
        });
    }

    private void innerUserOnlineNotify(String roomId, final AUIMessageUserInfo user, int onlineCount){
        if(mUserStateMap.containsKey(user.userId)){
            debugInfo(String.format("online notify user already in room, room: %s, uid: %s", roomId, user.userId));
            return;
        }
        mUserStateMap.put(user.userId, RoomUserState.Online);
        debugInfo(String.format("uid: %s, room: %s", user.userId, roomId));
        if(!TextUtils.equals(roomId, mRoomInfo.roomId)){ //非当前房间的事件
            debugInfo(String.format("out room offline notify, room: %s, uid: %s", roomId, user.userId));
            return;
        }
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                mMemberCount = onlineCount;
                UserInfo userInfo = new UserInfo(user.userId, "unknown");
                userInfo.userName = user.userNick;
                userInfo.avatarUrl = user.userAvatar;
                for(ARTCVoiceRoomEngineDelegate c : mRoomCallbacks) {
                    c.onJoinedRoom(userInfo);
                }
                for(ARTCVoiceRoomEngineDelegate c : mRoomCallbacks) {
                    c.onMemberCountChanged(onlineCount);
                }
            }
        });
    }

    @Override
    public void onLeaveGroup(AUIMessageModel<LeaveGroupMessage> message) {
        AliyunLog.d(TAG, "onLeaveGroup " + message.data.userId);
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                innerUserOfflineNotify(message.groupId, message.senderInfo);
            }
        });
    }

    @Override
    public void onMuteGroup(AUIMessageModel<MuteGroupMessage> message) {

    }

    @Override
    public void onUnMuteGroup(AUIMessageModel<UnMuteGroupMessage> message) {

    }

    @Override
    public void onMessageReceived(AUIMessageModel<String> message) {
        debugInfo(String.format("type[%d] content[%s]", message.type, message.data));
        if(!TextUtils.isEmpty(message.data)){
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(message.data);
            } catch (JSONException e) {
                e.printStackTrace();
                debugInfo("" + e.getMessage());
                return;
            }
            switch (message.type){
                case IMParams.MSG_TYPE_DISMISS_ROOM:
                    onDismissRoom(message.senderInfo, jsonObject);
                    break;
                case IMParams.MSG_TYPE_MUTE:
                    onMute(message.senderInfo, jsonObject);
                    break;
                case IMParams.MSG_TYPE_MUTE_ALL:
                    onMuteAll(message.senderInfo, jsonObject);
                    break;
                case IMParams.MSG_TYPE_KICK_OUT:
                    onKickOut(message.groupId, message.senderInfo, jsonObject);
                    break;
                case IMParams.MSG_TYPE_SEND_TEXT:
                    onReceiveTextMessage(message.senderInfo, jsonObject);
                    break;
                case IMParams.MSG_TYPE_SYNC_JOIN_MIC:
                    onJoinSeatUserNotify(message.senderInfo, jsonObject);
                    break;
                case IMParams.MSG_TYPE_SYNC_LEAVE_MIC:
                    onLeaveSeatUserNotify(message.senderInfo, jsonObject);
                    break;
            }
        }
    }

    private void onLeaveSeatUserNotify(AUIMessageUserInfo sender, JSONObject data){
        debugInfo(String.format("sender %s", sender.userId));
        if(TextUtils.equals(sender.userId, mCurrentUser.userId)){
            return;
        }
        String userId = data.optString(Constant.KEY_USER_ID);
        if(!TextUtils.isEmpty(userId) && mMicUsers.containsKey(userId)){
            UserInfo rs = mMicUsers.remove(userId);
            rs.isPublish = false;
            CommonUtil.runOnUI(new Runnable() {
                @Override
                public void run() {
                    for(ARTCVoiceRoomEngineDelegate c : mRoomCallbacks){
                        c.onLeavedMic(rs);
                    }
                }
            });
        }
    }

    private void onJoinSeatUserNotify(AUIMessageUserInfo sender, JSONObject data){
        debugInfo(String.format("sender %s", sender.userId));
        if(TextUtils.equals(sender.userId, mCurrentUser.userId)){
            return;
        }
        String userId = data.optString(Constant.KEY_USER_ID);
        String userName = data.optString(Constant.KEY_USER_NICK_NAME);
        String userAvatar = data.optString(Constant.KEY_USER_AVATAR);
        int seatIndex = data.optInt(Constant.KEY_SEAT_INDEX, -1);
        debugInfo(String.format("onSyncSeatUserInfo userId: %s, name: %s, seat: %d", userId, userName, seatIndex));
        if(!TextUtils.isEmpty(userId)){
            UserInfo userInfo = new UserInfo(userId, "unknown");
            userInfo.userName = userName;
            userInfo.avatarUrl = userAvatar;
            userInfo.micPosition = seatIndex;
            userInfo.isPublish = true;

            syncOffMicUserInfo(userInfo);

            mMicUsers.put(sender.userId, userInfo);
            CommonUtil.runOnUI(new Runnable() {
                @Override
                public void run() {
                    for(ARTCVoiceRoomEngineDelegate c : mRoomCallbacks){
                        c.onJoinedMic(userInfo);
                    }
                }
            });
        }
    }

    private void onReceiveTextMessage(AUIMessageUserInfo sender, JSONObject data){
        UserInfo userInfo = new UserInfo(sender.userId, "unknown");
        userInfo.userName = sender.userNick;
        userInfo.avatarUrl = sender.userAvatar;
        String msg = data.optString(Constant.KEY_CONTENT);
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                for(ARTCVoiceRoomEngineDelegate c : mRoomCallbacks) {
                    c.onReceivedTextMessage(userInfo, msg);
                }
            }
        });
    }

    private void onKickOut(String roomId, AUIMessageUserInfo requester, JSONObject data){
        if(!isAnchor(requester.userId)) //非主持人没有踢人权限
            return;
        if(mRTCService != null)
            mRTCService.leave();
        IMService.getInstance().leaveGroup(roomId, new ActionCallback() {
            @Override
            public void onResult(int code, String msg, Map<String, Object> params) {
                debugInfo(String.format("leaveGroup rs code: %d, msg: %s", code, msg));
            }
        });
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                for(ARTCVoiceRoomEngineDelegate c : mRoomCallbacks) {
                    c.onKickOutRoom();
                }
            }
        });
    }

    private void onDismissRoom(AUIMessageUserInfo sender, JSONObject data){
        debugInfo("");
        if(isAnchor(sender.userId)){ //只有主持人才有权限解散房间
            leaveRoom(null);
            CommonUtil.runOnUI(new Runnable() {
                @Override
                public void run() {
                    for(ARTCVoiceRoomEngineDelegate c : mRoomCallbacks){
                        c.onDismissRoom(sender.userId);
                    }
                }
            });
        }
    }

    private void onMute(AUIMessageUserInfo requester, JSONObject data){
        if(!isAnchor(requester.userId)) //非主持人没有权限禁言
            return;
        boolean mute = data.optBoolean(Constant.KEY_MUTE, false);
        innerMute(mute);
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                for(ARTCVoiceRoomEngineDelegate c : mRoomCallbacks) {
                    c.onMute(mute);
                }
            }
        });
    }

    private void innerMute(boolean mute){
        mOnMuted = mute;
        if(mRTCService != null)
            mRTCService.switchMicrophone(!mute);
    }

    private void onMuteAll(AUIMessageUserInfo requester, JSONObject data){
        if(!isAnchor(requester.userId)) //非主持人没有权限禁言
            return;
        if(isAnchor()) //主持人自己不禁言
            return;
        boolean mute = data.optBoolean(Constant.KEY_MUTE, false);
        innerMute(mute);
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                for(ARTCVoiceRoomEngineDelegate c : mRoomCallbacks) {
                    c.onMute(mute);
                }
            }
        });
    }

    @Override
    public void onExitedGroup(AUIMessageModel<ExitGroupMessage> message) {
        debugInfo("");
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                if(isJoinRoom()){ //被踢出群，需要主动离开
                    leaveRoom(null);
                }
                for(ARTCVoiceRoomEngineDelegate c : mRoomCallbacks){
                    c.onExitGroup("im token expire");
                }
            }
        });
    }

    protected void debugInfo(String msg){
        if(!mDebug)
            return;
        String method = CommonUtil.getCallMethodName(1);
        CommonUtil.runOnUI(new Runnable() {
            @Override
            public void run() {
                String roomMsg = String.format("[u:%s][s:%s][m:%s]%s", mCurrentUser != null ? mCurrentUser.userId : "unInit", mRoomState.getName(), method, msg);
                AliyunLog.d(TAG, roomMsg);
                for(ARTCVoiceRoomEngineDelegate c : mRoomCallbacks) {
                    c.onVoiceRoomDebugInfo(roomMsg);
                }
            }
        });
    }
}
