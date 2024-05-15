package com.aliyun.auikits.room.impl;

import android.text.TextUtils;

import com.aliyun.auikits.im.IMParams;
import com.aliyun.auikits.im.IMService;
import com.aliyun.auikits.room.ARTCRoomServiceInterface;
import com.aliyun.auikits.room.Constant;
import com.aliyun.auikits.room.KickOutMicInfo;
import com.aliyun.auikits.room.LeaveMicInfo;
import com.aliyun.auikits.room.RequestMicInfo;
import com.aliyun.auikits.common.util.CommonUtil;
import com.aliyun.auikits.rtc.ClientMode;
import com.aliyun.auikits.voiceroom.bean.MicInfo;
import com.aliyun.auikits.voiceroom.bean.RoomInfo;
import com.aliyun.auikits.voiceroom.bean.UserInfo;
import com.aliyun.auikits.voiceroom.callback.ActionCallback;
import com.aliyun.auikits.voiceroom.module.seat.SeatInfo;
import com.aliyun.auikits.voiceroom.module.seat.SeatManager;
import com.aliyun.auikits.voiceroom.module.seat.factory.SeatManagerFactory;
import com.aliyun.auikits.voiceroom.module.seat.protocol.Params;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ARTCRoomServiceImpl implements ARTCRoomServiceInterface {
    protected SeatManager mSeatManager; //麦位管理
    private static final String TAG = "ARTCRoomServiceImpl";
    private Set<UserInfo> mMuteList = new HashSet<>();
    protected ClientMode mMode;

    public ARTCRoomServiceImpl(ClientMode mode){
        this.mMode = mode;
    }

    @Override
    public String fetchIMLoginToken() {
        return null;
    }

    @Override
    public String fetchRTCAuthToken() {
        return null;
    }

    @Override
    public List<RoomInfo> getRoomList() {
        return null;
    }

    @Override
    public RoomInfo getRoomDetail() {
        return null;
    }

    @Override
    public int createRoom(RoomInfo info) {
        return 0;
    }

    @Override
    public int dismissRoom(String roomId) {
        JSONObject dismissJson = new JSONObject();
        try {
            dismissJson.put(Constant.KEY_TYPE, IMParams.MSG_TYPE_DISMISS_ROOM);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return IMService.getInstance().sendGroupMessage(roomId, IMParams.MSG_TYPE_DISMISS_ROOM, dismissJson.toString());
    }

    private void makeSureSeatManager(){
        if(mSeatManager == null){
            mSeatManager = SeatManagerFactory.createServerSeatManager(mMode);
        }
    }

    @Override
    public void getMicList(String roomId, ActionCallback callback) {
        makeSureSeatManager();
        mSeatManager.getSeatList(roomId, new ActionCallback() {
            @Override
            public void onResult(int code, String msg, Map<String, Object> params) {
                if(params.containsKey(Params.KEY_RESPONSE)){
                    JSONObject rs = (JSONObject) params.get(Params.KEY_RESPONSE);
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
                            s.seatIndex = mem.optInt(Constant.KEY_INDEX);
                            seats.add(s);
                        }
                        Map<String, Object> rsParams = new Hashtable<>();
                        rsParams.put(Constant.KEY_SEAT_LIST, seats);
                        CommonUtil.actionCallback(callback, 0, null, rsParams);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void requestMic(RequestMicInfo requestMic, ActionCallback callback) {
        makeSureSeatManager();
        SeatInfo seat = new SeatInfo();
        seat.roomId = requestMic.roomInfo.roomId;
        seat.userId = requestMic.userInfo.userId;
        seat.userName = requestMic.userInfo.userName;
        seat.userAvatar = requestMic.userInfo.avatarUrl;
        mSeatManager.joinSeat(seat, callback);
    }

    @Override
    public int agreeRequestMic(MicInfo mic) {
        return 0;
    }

    @Override
    public int rejectRequestMic(MicInfo mic) {
        return 0;
    }

    @Override
    public void leaveMic(LeaveMicInfo leaveMicInfo, ActionCallback callback) {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put(Constant.KEY_USER_ID, leaveMicInfo.userInfo.userId);
            jsonObj.put(Constant.KEY_SEAT_INDEX, leaveMicInfo.micPos);
        } catch (JSONException e) {
            e.printStackTrace();
            CommonUtil.actionCallback(callback, -1, "" + e.getMessage(), null);
            return;
        }
        //发送麦位信息同步通知
        IMService.getInstance().sendGroupMessage(leaveMicInfo.roomInfo.roomId, IMParams.MSG_TYPE_SYNC_LEAVE_MIC, jsonObj.toString());
        if(!leaveMicInfo.isHost){ //非主持人下麦还需要请求服务端通知
            SeatInfo seat = new SeatInfo();
            seat.roomId = leaveMicInfo.roomInfo.roomId;
            seat.userId = leaveMicInfo.userInfo.userId;
            seat.userName = leaveMicInfo.userInfo.userName;
            seat.userAvatar = leaveMicInfo.userInfo.avatarUrl;
            seat.seatIndex = leaveMicInfo.micPos;
            makeSureSeatManager();
            mSeatManager.leaveSeat(seat, new ActionCallback() {
                @Override
                public void onResult(int code, String msg, Map<String, Object> params) {
                    if(code == 0){
                        if(params.containsKey(Params.KEY_CODE)){
                            int rscode = (int)params.get(Params.KEY_CODE);
                            if(rscode == 200){
                                CommonUtil.actionCallback(callback, 0, "leave seat response success", null);
                            }else{
                                CommonUtil.actionCallback(callback, -1, "leave seat response fail", null);
                            }
                        }
                    }else{
                        CommonUtil.actionCallback(callback, code, msg, null);
                    }
                }
            });
        }else{
            CommonUtil.actionCallback(callback, 0, null, null);
        }
    }

    @Override
    public int lockMic(MicInfo mic) {
        return 0;
    }

    @Override
    public int kickOutMic(KickOutMicInfo kickInfo) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constant.KEY_TYPE, IMParams.MSG_TYPE_KICK_OUT);
            jsonObject.put(Constant.KEY_USER_ID, kickInfo.userInfo.userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //发送踢人消息
        IMService.getInstance().sendMessage(kickInfo.roomInfo.roomId, kickInfo.userInfo.userId, IMParams.MSG_TYPE_KICK_OUT, jsonObject.toString());
        return 0;
    }

    @Override
    public int mute(String groupId, UserInfo user, boolean mute) {
        if(mute)
            mMuteList.add(user);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constant.KEY_TYPE, IMParams.MSG_TYPE_MUTE);
            jsonObject.put(Constant.KEY_USER_ID, user.userId);
            jsonObject.put(Constant.KEY_MUTE, mute);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //发送禁言消息
        IMService.getInstance().sendMessage(groupId, user.userId, IMParams.MSG_TYPE_MUTE, jsonObject.toString());
        return 0;
    }

    @Override
    public List<UserInfo> getMuteList() {
        return new ArrayList<>(mMuteList);
    }

    @Override
    public int muteAll(String groupId, boolean mute) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constant.KEY_TYPE, IMParams.MSG_TYPE_MUTE_ALL);
            jsonObject.put(Constant.KEY_MUTE, mute);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //发送禁言消息
        IMService.getInstance().sendGroupMessage(groupId, IMParams.MSG_TYPE_MUTE_ALL, jsonObject.toString());
        return 0;
    }
}
