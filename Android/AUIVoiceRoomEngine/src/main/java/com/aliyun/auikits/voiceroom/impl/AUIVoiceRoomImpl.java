package com.aliyun.auikits.voiceroom.impl;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.alivc.auimessage.model.base.AUIMessageUserInfo;
import com.alivc.auimessage.model.token.IMNewToken;
import com.alivc.rtc.AliRtcEngine;
import com.aliyun.auikits.room.AUIActionCallback;
import com.aliyun.auikits.room.AUICreateRoomCallback;
import com.aliyun.auikits.room.AUIRoomEngine;
import com.aliyun.auikits.room.AppConfig;
import com.aliyun.auikits.room.bean.AUIAudioOutputType;
import com.aliyun.auikits.room.bean.AUIRoomConfig;
import com.aliyun.auikits.room.bean.AUIRoomUserInfo;
import com.aliyun.auikits.room.bean.PublishStreamConfig;
import com.aliyun.auikits.room.callback.AUIRoomEngineObserver;
import com.aliyun.auikits.room.config.AUIRoomBusinessType;
import com.aliyun.auikits.room.config.ErrorCode;
import com.aliyun.auikits.room.factory.AUIRoomEngineFactory;
import com.aliyun.auikits.room.network.RoomNetworkState;
import com.aliyun.auikits.voiceroom.bean.MicInfo;
import com.aliyun.auikits.voiceroom.bean.MicRequestResult;
import com.aliyun.auikits.voiceroom.bean.NetworkState;
import com.aliyun.auikits.room.util.AliyunLog;
import com.aliyun.auikits.voiceroom.AUIVoiceRoom;
import com.aliyun.auikits.voiceroom.AUIVoiceRoomCallback;
import com.aliyun.auikits.voiceroom.bean.RoomInfo;
import com.aliyun.auikits.voiceroom.bean.RoomState;
import com.aliyun.auikits.voiceroom.module.seat.SeatInfo;
import com.aliyun.auikits.voiceroom.bean.UserInfo;
import com.aliyun.auikits.voiceroom.callback.ActionCallback;
import com.aliyun.auikits.voiceroom.external.RtcInfo;
import com.aliyun.auikits.voiceroom.module.seat.SeatManager;
import com.aliyun.auikits.voiceroom.module.seat.callback.SeatManagerCallback;
import com.aliyun.auikits.voiceroom.module.seat.impl.SeatManagerFactory;
import com.aliyun.auikits.voiceroom.module.seat.protocol.Params;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class AUIVoiceRoomImpl implements AUIVoiceRoom, AUIRoomEngineObserver, SeatManagerCallback {
    public static final int MSG_TYPE_DISMISS_ROOM = 21001; //解散房间
    public static final int MSG_TYPE_MUTE = 21002; //禁言消息类型
    public static final int MSG_TYPE_MUTE_ALL = 21003; //禁言所有消息类型
    public static final int MSG_TYPE_KICK_OUT = 21004; //踢人消息类型
    public static final int MSG_TYPE_SEND_TEXT = 21301; //房间内文本消息
    public static final int MSG_TYPE_SYNC_JOIN_MIC = 21101; //房间内同步上麦消息
    public static final int MSG_TYPE_SYNC_LEAVE_MIC = 21102; //房间内同步离麦消息
    public static final int CODE_COMMON_SUCCESS = 0;
    private static final String TAG = "voice_room";
    private static final String KEY_TYPE = "type";
    private static final String KEY_AGREE = "agree";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_MUTE = "mute";
    private static final String KEY_CONTENT = "content";
    private static final String KEY_USER_NICK_NAME = "userNick";
    private static final String KEY_USER_AVATAR = "userAvatar";
    private static final String KEY_SEAT_INDEX = "seatIndex";
    private static final String KEY_INDEX = "index";
    protected CopyOnWriteArrayList<AUIVoiceRoomCallback> mRoomCallbacks = new CopyOnWriteArrayList<>();
    protected AUIRoomEngine mRoomEngine;
    protected RoomInfo mRoomInfo;
    protected UserInfo mCurrentUser;
    protected Context mContext;
    private Looper mTargetLooper;
    protected Handler mTargetHandler;
    protected RoomState mRoomState = RoomState.UN_INIT;
    protected Map<String, UserInfo> mMicUsers;
    protected boolean mOnMuted = false; //默认不被禁言
    protected SeatManager mSeatManager; //麦位管理
    protected boolean mDebug = true;
    protected Map<String, UserInfo> mCacheUserMap;

    public AUIVoiceRoomImpl(Looper looper){
        mTargetLooper = looper;
        mTargetHandler = new Handler(mTargetLooper);
        mRoomEngine = AUIRoomEngineFactory.createRoomEngine(AUIRoomBusinessType.AUIVoiceChat);
        mMicUsers = new Hashtable<>();
        mCacheUserMap = new Hashtable<>();
    }

    @Override
    public void init(Context context, String appId, String authorization, UserInfo userInfo, IMNewToken token, ActionCallback callback) {
        actionRunOnTargetThread(new Runnable() {
            @Override
            public void run() {
                debugInfo(String.format("[init] userid: %s, token: %s", userInfo.userId, token.app_token));
                AppConfig.setAppInfo(appId);
                mContext = context;
                mCurrentUser = userInfo;
                mRoomEngine.addObserver(AUIVoiceRoomImpl.this);
                AUIRoomUserInfo roomUserInfo = new AUIRoomUserInfo(mCurrentUser.userId, mCurrentUser.deviceId, token);
                roomUserInfo.setNickName(userInfo.userName);
                roomUserInfo.setAvatar(userInfo.avatarUrl);
                mRoomEngine.login(mContext, roomUserInfo, new AUIActionCallback() {
                    @Override
                    public void onResult(int code, String msg) {
                        if(code == CODE_COMMON_SUCCESS)
                            mRoomState = RoomState.INIT;
                        debugInfo(String.format("[login] result code: %d, msg: %s", code, msg));
                        actionCallback(callback, code, msg, null);
                    }
                });
                mSeatManager = SeatManagerFactory.createServerSeatManager(authorization, AUIVoiceRoomImpl.this);
            }
        });
    }

    @Override
    public void release() {
        actionRunOnTargetThread(new Runnable() {
            @Override
            public void run() {
                debugInfo("[release]");
                if(isInRoom()){
                    if(isHost()){ //主持人离开前需要释放房间
                        dismissRoom(null);
                    }
                    mRoomEngine.leave(null);
                }
                mRoomEngine.logout();
                mRoomEngine.removerObserver(AUIVoiceRoomImpl.this);
                mContext = null;
                mCurrentUser = null;
                mRoomState = RoomState.UN_INIT;
            }
        });
    }

    @Override
    public void addRoomCallback(AUIVoiceRoomCallback callback) {
        if(callback != null) {
            this.mRoomCallbacks.add(callback);
        }

    }

    @Override
    public void removeRoomCallback(AUIVoiceRoomCallback callback) {
        if(callback != null) {
            this.mRoomCallbacks.remove(callback);
        }
    }

    @Override
    public void createRoom(RoomInfo roomInfo, ActionCallback callback) {
        if(!isRoomInfoValid(roomInfo)){
            actionCallback(callback, -1, "room info invalid", null);
            return;
        }
        debugInfo(String.format("createRoom roomId[%s]", roomInfo.roomId));
        roomInfo.creator = getCurrentUser().userId;
        mRoomEngine.createRoom(roomInfo.roomId, new AUICreateRoomCallback() {
            @Override
            public void onError(int code, String msg) {
                actionCallback(callback, code, msg, null);
            }

            @Override
            public void onSuccess(String roomId) {
                Map<String, Object> params = new HashMap<>();
                params.put("roomId", roomId);
                actionCallback(callback, 0, null, params);
            }
        });
    }

    @Override
    public void joinRoom(RoomInfo roomInfo, RtcInfo rtcInfo, ActionCallback callback) {
        actionRunOnTargetThread(new Runnable() {
            @Override
            public void run() {
                if(!checkCurrentUser(callback))
                    return;
                debugInfo(String.format("[%s]joinRoom room[%s]", mCurrentUser.userId, roomInfo.roomId));
                if(!isRoomInfoValid(roomInfo)){
                    actionCallback(callback, -1, "room info invalid", null);
                    return;
                }
                AUIRoomConfig roomConfig = new AUIRoomConfig(roomInfo.roomId, rtcInfo.gslb, rtcInfo.token, rtcInfo.timestamp);
                mRoomInfo = roomInfo;
                mRoomEngine.join(roomConfig, new AUIActionCallback() {
                    @Override
                    public void onResult(int code, String msg) {
                        actionCallback(callback, code, msg, null);
                    }
                });
            }
        });
    }

    @Override
    public void leaveRoom(ActionCallback callback) {
        actionRunOnTargetThread(new Runnable() {
            @Override
            public void run() {
                if(!checkRoomState(callback) || !checkCurrentUser(callback))
                    return;
                debugInfo(String.format("[%s]leaveRoom", mCurrentUser.userId));
                if(mRoomState == RoomState.IN_MIC){ //需要先下麦
                    leaveMic(new ActionCallback() {
                        @Override
                        public void onResult(int code, String msg, Map<String, Object> params) {
                            if(code != CODE_COMMON_SUCCESS){
                                actionCallback(callback, code, msg, null);
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
        mRoomEngine.leave(new AUIActionCallback() {
            @Override
            public void onResult(int code, String msg) {
                actionCallback(callback, code, msg, null);
            }
        });
    }

    @Override
    public void dismissRoom(ActionCallback callback) {
        actionRunOnTargetThread(new Runnable() {
            @Override
            public void run() {
                if(!isHost()){ //非主持人没有权限
                    actionCallback(callback, -1, "dismissRoom have not permission", null);
                    return;
                }
                if(!checkRoomState(callback) || !checkCurrentUser(callback))
                    return;
                debugInfo(String.format("[%s]dismissRoom room[%s] state[%s]", mCurrentUser.userId, mRoomInfo.roomId, mRoomState.getName()));
                JSONObject dismissJson = new JSONObject();
                try {
                    dismissJson.put(KEY_TYPE, MSG_TYPE_DISMISS_ROOM);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mRoomEngine.sendGroupCustomMessage(MSG_TYPE_DISMISS_ROOM, dismissJson.toString(), new AUIActionCallback() {
                    @Override
                    public void onResult(int code, String msg) {
                        actionCallback(callback, code, msg, null);
                    }
                });
            }
        });
    }

    @Override
    public void requestMic(ActionCallback callback) {
        actionRunOnTargetThread(new Runnable() {
            @Override
            public void run() {
                if(mRoomState == RoomState.IN_MIC){
                    actionCallback(callback, 0, "user already in mic", null);
                    return;
                }
                if(!checkCurrentUser(callback))
                    return;
                debugInfo(String.format("[%s]requestMic room[%s] state[%s]", mCurrentUser.userId, mRoomInfo.roomId, mRoomState.getName()));
                //检查房间状态是否符合预期
                if(!checkInRoom(callback))
                    return;
                if(!checkRoomOwnerExists(callback))
                    return;
                innerRequestMic(callback);
            }
        });
    }

    private void innerRequestMic(ActionCallback callback){
        SeatInfo seat = new SeatInfo();
        seat.roomId = mRoomInfo.roomId;
        seat.userId = mCurrentUser.userId;
        seat.userName = mCurrentUser.userName;
        seat.userAvatar = mCurrentUser.avatarUrl;
        mSeatManager.joinSeat(seat, callback);
    }

    @Override
    public void joinMic(MicInfo micInfo, ActionCallback callback) {
        actionRunOnTargetThread(new Runnable() {
            @Override
            public void run() {
                if(!checkCurrentUser(callback))
                    return;
                debugInfo(String.format("[%s]joinMic mute[%b] pos[%d]", mCurrentUser.userId, micInfo.audioMute, micInfo.position));
                mCurrentUser.isMute = micInfo.audioMute;
                mCurrentUser.micPosition = micInfo.position;
                innerJoinMic(micInfo.audioMute);
                actionCallback(callback, 0, "", null);
            }
        });
    }

    private void actionRunOnTargetThread(Runnable runnable){
        if(mTargetHandler.getLooper() == Looper.myLooper()){
            runnable.run();
        }else{
            mTargetHandler.post(runnable);
        }
    }

    private void callbackRunOnTargetThread(Runnable runnable){
        mTargetHandler.post(runnable);
    }

    private boolean checkInRoom(ActionCallback callback){
        if(mRoomState != RoomState.IN_ROOM){
            actionCallback(callback, -1, "action in wrong state " + mRoomState.getName(), null);
            return false;
        }
        return true;
    }

    private boolean checkCurrentUser(ActionCallback callback){
        if(mCurrentUser == null){
            actionCallback(callback, -1, "current user null", null);
            return false;
        }
        return true;
    }

    private boolean checkRoomOwnerExists(ActionCallback callback){
        if(TextUtils.isEmpty(mRoomInfo.creator)){
            actionCallback(callback, -1, "room creator null", null);
            return false;
        }
        return true;
    }

    private boolean checkUserInMic(ActionCallback callback){
        if(mRoomState != RoomState.IN_MIC){
            actionCallback(callback, -1, "user in wrong state " + mRoomState.getName(), null);
            return false;
        }
        return true;
    }

    @Override
    public void leaveMic(ActionCallback callback) {
        actionRunOnTargetThread(new Runnable() {
            @Override
            public void run() {
                //检查用户状态
                if(!checkUserInMic(callback) || !checkCurrentUser(callback))
                    return;
                debugInfo(String.format("[%s]leaveMic state[%s]", mCurrentUser.userId, mRoomState.getName()));
                mRoomState = RoomState.IN_ROOM;
                final UserInfo leaveUser = mCurrentUser;
                if(isHost()){
                    innerLeaveMic(leaveUser, callback);
                }else{
                    innerLeaveMic(leaveUser, new ActionCallback() {
                        @Override
                        public void onResult(int code, String msg, Map<String, Object> params) {
                            if(code != CODE_COMMON_SUCCESS){
                                actionCallback(callback, code, msg, null);
                                return;
                            }
                            SeatInfo seat = new SeatInfo();
                            seat.roomId = mRoomInfo.roomId;
                            seat.userId = leaveUser.userId;
                            seat.userName = leaveUser.userName;
                            seat.userAvatar = leaveUser.avatarUrl;
                            seat.seatIndex = leaveUser.micPosition;
                            mSeatManager.leaveSeat(seat, callback);
                        }
                    });
                }
            }
        });
    }

    private void innerLeaveMic(UserInfo user, ActionCallback callback){
        callbackRunOnTargetThread(new Runnable() {
            @Override
            public void run() {
                for(AUIVoiceRoomCallback c : mRoomCallbacks) {
                    c.onUserLeaveMic(user);
                }
                mRoomEngine.switchMicrophone(user.userId, true, null, null); //下麦恢复麦克风开关状态为默认开
                mRoomEngine.stopPublish(null);
                JSONObject jsonObj = new JSONObject();
                try {
                    jsonObj.put(KEY_USER_ID, user.userId);
                    jsonObj.put(KEY_SEAT_INDEX, user.micPosition);
                } catch (JSONException e) {
                    e.printStackTrace();
                    actionCallback(callback, -1, "" + e.getMessage(), null);
                    return;
                }
                //发送麦位信息同步通知
                mRoomEngine.sendGroupCustomMessage(MSG_TYPE_SYNC_LEAVE_MIC, jsonObj.toString(), new AUIActionCallback() {
                    @Override
                    public void onResult(int code, String msg) {
                        actionCallback(callback, code, msg, null);
                    }
                });
            }
        });
    }

    @Override
    public void sendTextMessage(String textMessage, ActionCallback callback) {
        JSONObject jsonObj = new JSONObject();
        try{
            jsonObj.put(KEY_TYPE, MSG_TYPE_SEND_TEXT);
            jsonObj.put(KEY_CONTENT, textMessage);
        }catch (JSONException e){
            e.printStackTrace();
            actionCallback(callback, -1, "" + e.getMessage(), null);
            return;
        }
        mRoomEngine.sendGroupCustomMessage(MSG_TYPE_SEND_TEXT, jsonObj.toString(), new AUIActionCallback() {
            @Override
            public void onResult(int code, String msg) {
                actionCallback(callback, code, msg, null);
            }
        });
    }

    @Override
    public void listRecentTextMessage(ActionCallback callback) {

    }

    @Override
    public void listMicUserList(ActionCallback callback) {
        mSeatManager.getSeatList(mRoomInfo, callback);
    }

    @Override
    public void openMic(boolean open) {
        if(mCurrentUser == null){
            debugInfo("room unInit");
            return;
        }
        mRoomEngine.switchMicrophone(mCurrentUser.userId, open, null, null);
    }

    @Override
    public void openLoudSpeaker(boolean open) {
        if(open){
            mRoomEngine.switchAudioOutput(AUIAudioOutputType.SPEAKER);
        }else{
            mRoomEngine.switchAudioOutput(AUIAudioOutputType.HEADSET);
        }
    }

    @Override
    public void mute(UserInfo user, boolean mute, ActionCallback callback) {
        if(!isHost()){ //非主持人没有权限
            actionCallback(callback, -1, "mute have not permission", null);
            return;
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(KEY_TYPE, MSG_TYPE_MUTE);
            jsonObject.put(KEY_USER_ID, user.userId);
            jsonObject.put(KEY_MUTE, mute);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //发送禁言消息
        mRoomEngine.sendCustomMessage(user.userId, MSG_TYPE_MUTE, jsonObject.toString(), new AUIActionCallback() {
            @Override
            public void onResult(int code, String msg) {
                actionCallback(callback, code, msg, null);
            }
        });
    }

    @Override
    public void muteAll(boolean mute, ActionCallback callback) {
        if(!isHost()){ //非主持人没有权限
            actionCallback(callback, -1, "muteAll have not permission", null);
            return;
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(KEY_TYPE, MSG_TYPE_MUTE_ALL);
            jsonObject.put(KEY_MUTE, mute);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //发送禁言消息
        mRoomEngine.sendGroupCustomMessage(MSG_TYPE_MUTE_ALL, jsonObject.toString(), new AUIActionCallback() {
            @Override
            public void onResult(int code, String msg) {
                actionCallback(callback, code, msg, null);
            }
        });
    }

    @Override
    public void kickOut(UserInfo user, ActionCallback callback) {
        if(!isHost()){ //非主持人没有权限
            actionCallback(callback, -1, "kickOut have not permission", null);
            return;
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(KEY_TYPE, MSG_TYPE_KICK_OUT);
            jsonObject.put(KEY_USER_ID, user.userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //发送踢人消息
        mRoomEngine.sendCustomMessage(user.userId, MSG_TYPE_KICK_OUT, jsonObject.toString(), new AUIActionCallback() {
            @Override
            public void onResult(int code, String msg) {
                actionCallback(callback, code, msg, null);
            }
        });
    }

    @Override
    public UserInfo getCurrentUser() {
        return mCurrentUser;
    }

    @Override
    public RoomInfo getRoomInfo() {
        return mRoomInfo;
    }

    @Override
    public boolean isHost() {
        return isHost(mCurrentUser);
    }

    @Override
    public boolean isHost(UserInfo userInfo) {
        if(userInfo == null || TextUtils.isEmpty(userInfo.userId) || mRoomInfo == null)
            return false;
        if(userInfo.userId.equals(mRoomInfo.creator))
            return true;
        return false;
    }

    @Override
    public RoomState getRoomState() {
        return mRoomState;
    }

    private boolean isRoomInfoValid(RoomInfo info){
        if(info == null)
            return false;
        return true;
    }

    private void actionCallback(ActionCallback callback, int code, String msg, Map<String, Object> params){
        if(callback == null) return;
        mTargetHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onResult(code, msg, params);
            }
        });
    }

    private void debugInfo(String msg){
        if(!mDebug)
            return;
        String roomMsg = String.format("[%s][%s][%s]%s", TAG, mCurrentUser != null ? mCurrentUser.userId : "uninit", mRoomState.getName(), msg);
        callbackRunOnTargetThread(new Runnable() {
            @Override
            public void run() {
                for(AUIVoiceRoomCallback c : mRoomCallbacks) {
                    c.onVoiceRoomDebugInfo(roomMsg);
                }
            }
        });
    }

    private boolean checkRoomState(ActionCallback callback){
        if(mRoomEngine == null){
            actionCallback(callback, -1, "room state invalid", null);
            return false;
        }
        if(!mRoomEngine.isInRoom()){
            actionCallback(callback, -1, "user not in room", null);
            return false;
        }
        return true;
    }

    @Override
    public void onCancelRequestJoin(String roomId, String sender, JSONObject extra) {
    }

    @Override
    public void onCancelRequestPublish(String sender, JSONObject extra) {
    }

    @Override
    public void onCustomMessageReceived(AUIMessageUserInfo sender, int msgType, String data) {
        if(!TextUtils.isEmpty(data)){
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(data);
            } catch (JSONException e) {
                e.printStackTrace();
                debugInfo("" + e.getMessage());
                return;
            }
            switch (msgType){
                case MSG_TYPE_DISMISS_ROOM:
                    onDismissRoom(sender.userId, jsonObject);
                    break;
                case MSG_TYPE_MUTE:
                    onMute(sender.userId, jsonObject);
                    break;
                case MSG_TYPE_MUTE_ALL:
                    onMuteAll(sender.userId, jsonObject);
                    break;
                case MSG_TYPE_KICK_OUT:
                    onKickOut(sender.userId, jsonObject);
                    break;
                case MSG_TYPE_SEND_TEXT:
                    onReceiveTextMessage(sender, jsonObject);
                    break;
                case MSG_TYPE_SYNC_JOIN_MIC:
                    onJoinSeatUserNotify(sender, jsonObject);
                    break;
                case MSG_TYPE_SYNC_LEAVE_MIC:
                    onLeaveSeatUserNotify(sender, jsonObject);
                    break;
            }
        }
    }

    private void onLeaveSeatUserNotify(AUIMessageUserInfo sender, JSONObject data){
        if(TextUtils.equals(sender.userId, mCurrentUser.userId)){
            debugInfo("onLeaveSeatUserNotify from self");
            return;
        }
        String userId = data.optString(KEY_USER_ID);
        if(!TextUtils.isEmpty(userId) && mMicUsers.containsKey(userId)){
            UserInfo rs = mMicUsers.remove(userId);
            rs.isPublish = false;
            callbackRunOnTargetThread(new Runnable() {
                @Override
                public void run() {
                    for(AUIVoiceRoomCallback c : mRoomCallbacks){
                        c.onUserLeaveMic(rs);
                    }
                }
            });
        }
    }

    private void syncCacheUserInfo(UserInfo userInfo){
        if(!mCacheUserMap.containsKey(userInfo.userId))
            return;
        //同步状态
        UserInfo cache = mCacheUserMap.get(userInfo.userId);
        userInfo.isMute = cache.isMute;
        mCacheUserMap.remove(userInfo.userId);
    }

    private void onJoinSeatUserNotify(AUIMessageUserInfo sender, JSONObject data){
        if(TextUtils.equals(sender.userId, mCurrentUser.userId)){
            debugInfo("onJoinSeatUserNotify from self");
            return;
        }
        String userId = data.optString(KEY_USER_ID);
        String userName = data.optString(KEY_USER_NICK_NAME);
        String userAvatar = data.optString(KEY_USER_AVATAR);
        int seatIndex = data.optInt(KEY_SEAT_INDEX, -1);
        debugInfo(String.format("onSyncSeatUserInfo userId[%s] name[%s] seat[%d]", userId, userName, seatIndex));
        if(!TextUtils.isEmpty(userId)){
            UserInfo userInfo = new UserInfo(userId, "unknown");
            userInfo.userName = userName;
            userInfo.avatarUrl = userAvatar;
            userInfo.micPosition = seatIndex;
            userInfo.isPublish = true;

            syncCacheUserInfo(userInfo);

            mMicUsers.put(sender.userId, userInfo);
            callbackRunOnTargetThread(new Runnable() {
                @Override
                public void run() {
                    for(AUIVoiceRoomCallback c : mRoomCallbacks){
                        c.onUserJoinMic(userInfo);
                    }
                }
            });
        }
    }

    private void innerNotifyOnJoinMic(UserInfo userInfo){

    }

    private void onDismissRoom(String sender, JSONObject data){
        if(isHost(sender)){ //只有主持人才有权限解散房间
            leaveRoom(null);
            callbackRunOnTargetThread(new Runnable() {
                @Override
                public void run() {
                    for(AUIVoiceRoomCallback c : mRoomCallbacks){
                        c.onDismissRoom(sender);
                    }
                }
            });
        }
    }

    private void onResponseRequestMic(JSONObject data){
        callbackRunOnTargetThread(new Runnable() {
            @Override
            public void run() {
                mRoomState = RoomState.IN_ROOM; //重置状态
                int reason = 0;
                int micPos = -1;
                if(data != null){
                    if(data.has(Params.KEY_REASON)){
                        reason = data.optInt(Params.KEY_REASON);
                    }
                    micPos = data.optInt(KEY_INDEX, -1);
                }
                MicRequestResult rs = new MicRequestResult(reason, micPos);
                for(AUIVoiceRoomCallback c : mRoomCallbacks) {
                    c.onResponseMic(rs);
                }
            }
        });
    }

    private void onMute(String requester, JSONObject data){
        if(!isHost(requester)) //非主持人没有权限禁言
            return;
        boolean mute = data.optBoolean(KEY_MUTE, false);
        innerMute(mute);
        callbackRunOnTargetThread(new Runnable() {
            @Override
            public void run() {
                for(AUIVoiceRoomCallback c : mRoomCallbacks) {
                    c.onMute(mute);
                }
            }
        });
    }

    private void onMuteAll(String requester, JSONObject data){
        if(!isHost(requester)) //非主持人没有权限禁言
            return;
        if(isHost()) //主持人自己不禁言
            return;
        boolean mute = data.optBoolean(KEY_MUTE, false);
        innerMute(mute);
        callbackRunOnTargetThread(new Runnable() {
            @Override
            public void run() {
                for(AUIVoiceRoomCallback c : mRoomCallbacks) {
                    c.onMute(mute);
                }
            }
        });
    }

    private void onKickOut(String requester, JSONObject data){
        if(!isHost(requester)) //非主持人没有踢人权限
            return;
        mRoomEngine.leave(null);
        callbackRunOnTargetThread(new Runnable() {
            @Override
            public void run() {
                for(AUIVoiceRoomCallback c : mRoomCallbacks) {
                    c.onKickOut();
                }
            }
        });
    }

    private void onReceiveTextMessage(AUIMessageUserInfo sender, JSONObject data){
        UserInfo userInfo = new UserInfo(sender.userId, "unknown");
        userInfo.userName = sender.userNick;
        userInfo.avatarUrl = sender.userAvatar;
        String msg = data.optString(KEY_CONTENT);
        callbackRunOnTargetThread(new Runnable() {
            @Override
            public void run() {
                for(AUIVoiceRoomCallback c : mRoomCallbacks) {
                    c.onTextMessageReceived(userInfo, msg);
                }
            }
        });
    }

    private boolean isHost(String userId){
        return TextUtils.equals(userId, mRoomInfo.creator);
    }

    @Override
    public void onDebugInfo(String info) {
        debugInfo(info);
    }

    @Override
    public void onError(int code, String msg) {
        debugInfo(String.format("onError code[%d] msg[%s]", code, msg));
        if(code == ErrorCode.ERROR_EXITED_GROUP){
            callbackRunOnTargetThread(new Runnable() {
                @Override
                public void run() {
                    if(isInRoom()){ //被踢出群，需要主动离开
                        leaveRoom(null);
                    }
                    for(AUIVoiceRoomCallback c : mRoomCallbacks){
                        c.onExitGroup(msg);
                    }
                }
            });
        }
    }

    private void innerJoinMic(boolean audioMute){
        PublishStreamConfig publishConfig = new PublishStreamConfig(false, true, true, audioMute);
        mRoomEngine.startPublish(publishConfig, new AUIActionCallback() { //同意上麦后直接上麦
            @Override
            public void onResult(int code, String msg) {
                if(code == CODE_COMMON_SUCCESS){
                    mRoomState = RoomState.IN_MIC;
                }
                debugInfo(String.format("startPublish code[%d] msg[%s]", code, msg));
            }
        });

        final boolean mute = audioMute;
        final UserInfo userInfo = mCurrentUser;
        callbackRunOnTargetThread(new Runnable() {
            @Override
            public void run() {
                for(AUIVoiceRoomCallback c : mRoomCallbacks) {
                    c.onUserJoinMic(userInfo);
                }
                for(AUIVoiceRoomCallback c : mRoomCallbacks) {
                    if(mute){ //通知麦控开关变化
                        c.onUserMicOff(userInfo);
                    }else{
                        c.onUserMicOn(userInfo);
                    }
                }
            }
        });

        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put(KEY_USER_ID, mCurrentUser.userId);
            jsonObj.put(KEY_USER_NICK_NAME, mCurrentUser.userName);
            jsonObj.put(KEY_USER_AVATAR, mCurrentUser.avatarUrl);
            jsonObj.put(KEY_SEAT_INDEX, mCurrentUser.micPosition);
        } catch (JSONException e) {
            e.printStackTrace();
            debugInfo("" + e.getMessage());
            return;
        }
        //发送麦位信息同步通知
        mRoomEngine.sendGroupCustomMessage(MSG_TYPE_SYNC_JOIN_MIC, jsonObj.toString(), new AUIActionCallback() {
            @Override
            public void onResult(int code, String msg) {
                debugInfo(String.format("send MSG_TYPE_SYNC_JOIN_MIC code[%d] msg[%s]", code, msg));
            }
        });
    }

    private void innerMute(boolean mute){
        mOnMuted = mute;
        mRoomEngine.switchMicrophone(mCurrentUser.userId, !mute, null, null);
    }

    private boolean isInRoom(){
        if(mRoomState.val() >= RoomState.IN_ROOM.val())
            return true;
        return false;
    }

    @Override
    public void onJoin(String roomId, String uid) {
        debugInfo(String.format("[%s]onJoin room[%s] state[%s] isHost[%b]",
                uid, roomId, mRoomState.getName(), isHost()));
        mRoomState = RoomState.IN_ROOM;
        callbackRunOnTargetThread(new Runnable() {
            @Override
            public void run() {
                for(AUIVoiceRoomCallback c : mRoomCallbacks) {
                    c.onJoin(roomId, uid);
                }
            }
        });

        if(isHost()){ //主持人加入房间需要直接上麦
           innerJoinMic(mCurrentUser.isMute);
        }
    }

    @Override
    public void onLeave() {
        debugInfo(String.format("[%s]onLeave room[%s] state[%s]",
                mCurrentUser.userId, mRoomInfo.roomId, mRoomState.getName()));
        mRoomState = RoomState.INIT;
        debugInfo("onLeave");
        resetRoom();
        callbackRunOnTargetThread(new Runnable() {
            @Override
            public void run() {
                for(AUIVoiceRoomCallback c : mRoomCallbacks) {
                    c.onLeave();
                }
            }
        });
    }

    private void resetRoom(){
        mOnMuted = false;
        mRoomState = RoomState.INIT;
        mMicUsers.clear();
        mCacheUserMap.clear();
    }

    @Override
    public void onRequestJoin(String roomId, String inviter, JSONObject extra) {

    }

    @Override
    public void onRequestPublish(String roomId, String inviter, JSONObject extra) {

    }

    @Override
    public void onRequestSwitchCamera(String roomId, String requester, boolean off, JSONObject extra) {

    }

    @Override
    public void onRequestSwitchMic(String roomId, String requester, boolean off, JSONObject extra) {

    }

    @Override
    public void onResponseJoin(String roomId, String invitee, boolean agree, JSONObject extra) {

    }

    @Override
    public void onResponsePublish(String roomId, String invitee, boolean agree, JSONObject extra) {

    }

    @Override
    public void onUserAudioMuted(String uid, boolean muted) {
        callbackRunOnTargetThread(new Runnable() {
            @Override
            public void run() {
                UserInfo user = mMicUsers.containsKey(uid) ? mMicUsers.get(uid) : (TextUtils.equals(mCurrentUser.userId, uid) ? mCurrentUser : null);
                if(user == null){
                    user = new UserInfo(uid, "unknown");
                    mCacheUserMap.put(user.userId, user);
                }
                user.isMute = muted;
                for(AUIVoiceRoomCallback c : mRoomCallbacks) {
                    if(muted) {
                        c.onUserMicOff(user);
                    } else {
                        c.onUserMicOn(user);
                    }
                }
            }
        });
    }

    @Override
    public void onUserOffline(String roomId, AUIRoomUserInfo user) {
        debugInfo(String.format("userOffline uid[%s] room[%s]", user.getUserId(), roomId));
        if(!TextUtils.equals(roomId, mRoomInfo.roomId)){ //非当前房间的事件
            debugInfo(String.format("onUserOffline other room[%s] uid[%s]", roomId, user.getUserId()));
            return;
        }
        UserInfo userInfo = new UserInfo(user.getUserId(), "unknown");
        userInfo.userName = user.getNickName();
        userInfo.avatarUrl = user.getAvatar();
        callbackRunOnTargetThread(new Runnable() {
            @Override
            public void run() {
                for(AUIVoiceRoomCallback c : mRoomCallbacks) {
                    c.onUserOffline(userInfo);
                }
            }
        });
    }

    @Override
    public void onUserOnline(String roomId, AUIRoomUserInfo user) {
        debugInfo(String.format("userOnline uid[%s] room[%s]", user.getUserId(), roomId));
        if(!TextUtils.equals(roomId, mRoomInfo.roomId)){ //非当前房间的事件
            debugInfo(String.format("onUserOnline other room[%s] uid[%s]", roomId, user.getUserId()));
            return;
        }
        callbackRunOnTargetThread(new Runnable() {
            @Override
            public void run() {
                UserInfo userInfo = new UserInfo(user.getUserId(), "unknown");
                userInfo.userName = user.getNickName();
                userInfo.avatarUrl = user.getAvatar();
                for(AUIVoiceRoomCallback c : mRoomCallbacks) {
                    c.onUserOnline(userInfo);
                }
            }
        });
    }

    @Override
    public void onUserStartPublish(String uid) {
        debugInfo(String.format("onUserStartPublish userId[%s]", uid));
    }

    @Override
    public void onUserStopPublish(String uid) {
        debugInfo(String.format("onUserStopPublish userId[%s]", uid));
    }

    @Override
    public void onUserVideoMuted(String uid, boolean muted) {
    }

    @Override
    public void onActiveSpeaker(String uid, boolean speaking) {
        AliyunLog.d(TAG, "onActiveSpeaker " + uid);
        callbackRunOnTargetThread(new Runnable() {
            @Override
            public void run() {
                UserInfo user = mMicUsers.containsKey(uid) ? mMicUsers.get(uid) : (mCurrentUser != null && mCurrentUser.userId.equals(uid) ? mCurrentUser : null);
                if(user == null){
                    user = new UserInfo(uid, "unknown");
                }
                user.speaking = speaking; //标记当前正在发言
                for(AUIVoiceRoomCallback c : mRoomCallbacks) {
                    c.onUserSpeakState(user);
                }
            }
        });
    }

    @Override
    public void onNetworkStateChanged(String uid, RoomNetworkState state) {
        AliyunLog.d(TAG, "onNetworkStateChanged " + uid + ", state " + state.getName());
        callbackRunOnTargetThread(new Runnable() {
            @Override
            public void run() {
                UserInfo user = mMicUsers.containsKey(uid) ? mMicUsers.get(uid) : (mCurrentUser.userId.equals(uid) ? mCurrentUser : null);
                if(user == null){
                    user = new UserInfo(uid, "unknown");
                }
                user.networkState = convert2NetworkState(state); //刷新网络状态
                for(AUIVoiceRoomCallback c : mRoomCallbacks) {
                    c.onUserNetworkState(user);
                }
            }
        });
    }

    @Override
    public void onDataChannelMessage(String uid, AliRtcEngine.AliRtcDataChannelMsg msg) {
        debugInfo(String.format("data channel receive msg from[%s]", uid));
        callbackRunOnTargetThread(new Runnable() {
            @Override
            public void run() {
                for(AUIVoiceRoomCallback c : mRoomCallbacks){
                    c.onDataChannelMessage(uid, msg);
                }
            }
        });
    }

    private NetworkState convert2NetworkState(RoomNetworkState state){
        if(state == RoomNetworkState.EXCELLENT){
            return NetworkState.EXCELLENT;
        }else if(state == RoomNetworkState.NORMAL){
            return NetworkState.NORMAL;
        }else if(state == RoomNetworkState.WEAK){
            return NetworkState.WEAK;
        }else if(state == RoomNetworkState.DISCONNECT){
            return NetworkState.DISCONNECT;
        }else {
            return NetworkState.UNKNOWN;
        }
    }

    @Override
    public void onResponseJoinSeat(JSONObject rs) {
        onResponseRequestMic(rs);
    }

    @Override
    public void onResponseLeaveSeat(JSONObject rs) {
        if(rs != null){
            int code = rs.optInt(Params.KEY_CODE);
            if(code == 200){
                debugInfo("leave seat response success");
            }else{
                debugInfo("leave seat response fail");
            }
        }
    }

    @Override
    public void onResponseQuerySeatList(JSONObject rs) {
        if(rs != null){
            try{
                List<SeatInfo> seats = new ArrayList<>();
                JSONArray members = rs.optJSONArray(Params.KEY_MEMBERS);
                for(int i = 0; i < members.length(); ++i){
                    JSONObject mem = members.getJSONObject(i);
                    SeatInfo s = new SeatInfo();
                    s.userId = mem.optString(Params.KEY_USER_ID);
                    String extend = mem.optString(Params.KEY_EXTENDS);
                    if(!TextUtils.isEmpty(extend)){
                        JSONObject extendObj = new JSONObject(extend);
                        s.userName = extendObj.optString(Params.KEY_USER_NICK);
                        s.userAvatar = extendObj.optString(Params.KEY_USER_AVATAR);
                    }
                    s.seatIndex = mem.optInt(KEY_INDEX);
                    seats.add(s);
                }
                callbackRunOnTargetThread(new Runnable() {
                    @Override
                    public void run() {
                        combineSeatUsers(seats);
                        List<UserInfo> result = new ArrayList<>(mMicUsers.values());
                        for(AUIVoiceRoomCallback c : mRoomCallbacks){
                            c.onRoomMicListChanged(result);
                        }
                    }
                });
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
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
            syncCacheUserInfo(newU);
            mMicUsers.put(s.userId, newU);
        }
        debugInfo(String.format("user in mic count[%d]", mMicUsers.size()));
    }
}
