package com.aliyun.auikits.voiceroom.module.seat.impl;

import android.text.TextUtils;

import com.aliyun.auikits.biz.ktv.KTVServerConstant;
import com.aliyun.auikits.biz.voiceroom.VoiceRoomServerConstant;
import com.aliyun.auikits.rtc.ClientMode;
import com.aliyun.auikits.single.server.Server;
import com.aliyun.auikits.single.Single;
import com.aliyun.auikits.single.Singleton;
import com.aliyun.auikits.common.util.CommonUtil;
import com.aliyun.auikits.voiceroom.module.seat.SeatInfo;
import com.aliyun.auikits.voiceroom.callback.ActionCallback;
import com.aliyun.auikits.voiceroom.module.seat.SeatManager;
import com.aliyun.auikits.voiceroom.module.seat.protocol.Params;
import com.aliyun.auikits.voiceroom.network.HttpRequest;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ServerSeatManager implements SeatManager, Single {

    private String mToken;

    private Map<String, String> mHeaders;

    private ClientMode mMode;

    public ServerSeatManager(){
        this.mToken = Singleton.getInstance(Server.class).getAuthorizeToken();
        if(TextUtils.isEmpty(this.mToken))
            throw new IllegalStateException("you should config the server token first!");
        this.mHeaders = new Hashtable<>();
        mHeaders.put("Authorization", mToken);
    }

    public void setClientMode(ClientMode mode){
        this.mMode = mode;
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
        String url = VoiceRoomServerConstant.JOIN_SEAT_URL;
        if(mMode == ClientMode.KTV){
            url = KTVServerConstant.JOIN_SEAT_URL;
        }
        HttpRequest.getInstance().post(url, mHeaders, jsonObj, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                onFailureCallback(-1, "join seat failed except: " + e.getMessage(), callback);
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
                            CommonUtil.actionCallback(callback, -1, String.format("http request failed code[%d]", code), null);
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
                        Map<String, Object> params = new Hashtable<>();
                        params.put(Params.KEY_RESPONSE, finalTarget);
                        CommonUtil.actionCallback(callback, 0, null, params);
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                        onFailureCallback(-2, "join seat failed except: " + e.getMessage(), callback);
                        return;
                    }
                }
            }
        });
    }

    private void onFailureCallback(int code, String msg, ActionCallback callback){
        if(callback == null)
            return;
        CommonUtil.actionCallback(callback, code, msg, null);
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
        String url = VoiceRoomServerConstant.LEAVE_SEAT_URL;
        if(mMode == ClientMode.KTV){
            url = KTVServerConstant.LEAVE_SEAT_URL;
        }
        HttpRequest.getInstance().post(url, mHeaders, reqObj, new Callback() {
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
                        Map<String, Object> params = new Hashtable<>();
                        int code = resp.optInt(Params.KEY_CODE);
                        params.put(Params.KEY_CODE, code);
                        CommonUtil.actionCallback(callback, 0, null, params);
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                        onFailureCallback(-1, "json except: " + e.getMessage(), null);
                        return;
                    }
                }
            }
        });
    }

    @Override
    public void getSeatList(String roomId, ActionCallback callback) {
        JSONObject reqObj = new JSONObject();
        try {
            reqObj.put(Params.KEY_ID, roomId);
            String url = VoiceRoomServerConstant.GET_SEAT_LIST_URL;
            if(mMode == ClientMode.KTV){
                url = KTVServerConstant.GET_SEAT_LIST_URL;
            }
            HttpRequest.getInstance().post(url, mHeaders, reqObj, new Callback() {
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
                            Map<String, Object> params = new Hashtable<>();
                            if(code == 200){
                                params.put(Params.KEY_RESPONSE, resp);
                            }
                            CommonUtil.actionCallback(callback, 0, null, params);
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                            onFailureCallback(-1, "json except: " + e.getMessage(), null);
                            return;
                        }
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            onFailureCallback(-1, "json except: " + e.getMessage(), null);
        }
    }
}
