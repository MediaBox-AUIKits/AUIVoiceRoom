package com.aliyun.auikits.voiceroom.module.seat.impl;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.aliyun.auikits.voiceroom.bean.RoomInfo;
import com.aliyun.auikits.voiceroom.module.seat.SeatInfo;
import com.aliyun.auikits.voiceroom.callback.ActionCallback;
import com.aliyun.auikits.voiceroom.module.seat.SeatManager;
import com.aliyun.auikits.voiceroom.module.seat.callback.SeatManagerCallback;
import com.aliyun.auikits.voiceroom.module.seat.protocol.Params;
import com.aliyun.auikits.voiceroom.network.HttpRequest;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ServerSeatManager implements SeatManager {

    private SeatManagerCallback mCallback;
    private static final String HOST = "http://chatroom.h5video.vip";
    private static final String JOIN_SEAT_URL = HOST + "/api/chatroom/joinMic";
    private static final String LEAVE_SEAT_URL = HOST + "/api/chatroom/leaveMic";
    private static final String GET_SEAT_LIST_URL = HOST + "/api/chatroom/getMeetingInfo";
    private String mToken;

    private Handler mUIHandler = new Handler(Looper.getMainLooper());
    private Map<String, String> mHeaders;

    public ServerSeatManager(String serverToken, SeatManagerCallback callback){
        this.mCallback = callback;
        this.mToken = serverToken;
        this.mHeaders = new Hashtable<>();
        mHeaders.put("Authorization", mToken);
    }

    @Override
    public void joinSeat(SeatInfo seat, ActionCallback callback) {
        JSONObject jsonObj = new JSONObject();
        JSONObject extendObj = new JSONObject();
        try {
            extendObj.put(Params.KEY_USER_NICK, seat.userName);
            extendObj.put(Params.KEY_USER_AVATAR, seat.userAvatar);
            jsonObj.put(Params.KEY_ID, seat.roomId);
            jsonObj.put(Params.KEY_USER_ID, seat.userId);
            jsonObj.put(Params.KEY_EXTENDS, extendObj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HttpRequest.getInstance().post(JOIN_SEAT_URL, mHeaders, jsonObj, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                mUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onFailureCallback(-1, "join seat failed except: " + e.getMessage(), callback);
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if(response.code() != 200){
                    onFailureCallback(-1, "join seat failed http: " + response.code(), callback);
                }else{
                    try {
                        JSONObject resp = new JSONObject(response.body().string());
                        int code = resp.optInt(Params.KEY_CODE);
                        if(code != 200){
                            mUIHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mCallback.onResponseJoinSeat(null);
                                }
                            });
                            return;
                        }

                        JSONObject target = resp;
                        JSONArray members = resp.optJSONArray(Params.KEY_MEMBERS);
                        if(members != null){
                            for(int i = 0; i < members.length(); ++i){
                                JSONObject mem = members.getJSONObject(i);
                                String uid = mem.optString(Params.KEY_USER_ID);
                                if(TextUtils.equals(uid, seat.userId)){
                                    target = mem;
                                    break;
                                }
                            }
                        }
                        final JSONObject finalTarget = target;
                        mUIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mCallback.onResponseJoinSeat(finalTarget);
                            }
                        });
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                        onFailureCallback(-1, "join seat failed except: " + e.getMessage(), callback);
                        return;
                    }
                    onSuccessCallback(callback);
                }
            }
        });
    }

    private void onFailureCallback(int code, String msg, ActionCallback callback){
        if(callback == null)
            return;
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {

                callback.onResult(code, msg, null);
            }
        });
    }

    private void onSuccessCallback(ActionCallback callback){
        if(callback == null)
            return;
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {

                callback.onResult(0, "", null);
            }
        });
    }

    @Override
    public void leaveSeat(SeatInfo seat, ActionCallback callback) {
        JSONObject reqObj = new JSONObject();
        try {
            reqObj.put(Params.KEY_ID, seat.roomId);
            reqObj.put(Params.KEY_USER_ID, seat.userId);
            reqObj.put(Params.KEY_INDEX, seat.seatIndex);
        } catch (JSONException e) {
            e.printStackTrace();
            onFailureCallback(-1, "json except: " + e.getMessage(), null);
            return;
        }

        HttpRequest.getInstance().post(LEAVE_SEAT_URL, mHeaders, reqObj, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                onFailureCallback(-1, "http except: " + e.getMessage(), null);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if(response.code() != 200){
                    onFailureCallback(-1, "http code: " + response.code(), null);
                }else{
                    try {
                        JSONObject resp = new JSONObject(response.body().string());
                        mUIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mCallback.onResponseLeaveSeat(resp);
                            }
                        });
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                        onFailureCallback(-1, "json except: " + e.getMessage(), null);
                        return;
                    }
                    onSuccessCallback(callback);
                }
            }
        });
    }

    @Override
    public void getSeatList(RoomInfo roomInfo, ActionCallback callback) {
        JSONObject reqObj = new JSONObject();
        try {
            reqObj.put(Params.KEY_ID, roomInfo.roomId);
            HttpRequest.getInstance().post(GET_SEAT_LIST_URL, mHeaders, reqObj, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    onFailureCallback(-1, "http except: " + e.getMessage(), null);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    if(response.code() != 200){
                        onFailureCallback(-1, "http code: " + response.code(), null);
                    }else{
                        try {
                            JSONObject resp = new JSONObject(response.body().string());
                            int code = resp.optInt(Params.KEY_CODE);
                            mUIHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(code != 200){
                                        mCallback.onResponseQuerySeatList(null);
                                    }else{
                                        mCallback.onResponseQuerySeatList(resp);
                                    }
                                }
                            });
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                            onFailureCallback(-1, "json except: " + e.getMessage(), null);
                            return;
                        }
                        onSuccessCallback(callback);
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            onFailureCallback(-1, "json except: " + e.getMessage(), null);
        }
    }
}
