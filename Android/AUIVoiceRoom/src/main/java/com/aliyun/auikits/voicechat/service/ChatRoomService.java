package com.aliyun.auikits.voicechat.service;

import android.util.Log;

import com.aliyun.auikits.voicechat.model.entity.ChatRoomCallback;
import com.aliyun.auikits.voice.ARTCVoiceRoomEngine;
import com.aliyun.auikits.voiceroom.bean.MicInfo;
import com.aliyun.auikits.voiceroom.bean.MicRequestResult;
import com.aliyun.auikits.voiceroom.bean.RoomInfo;
import com.aliyun.auikits.voiceroom.bean.UserInfo;
import com.aliyun.auikits.voiceroom.callback.ActionCallback;
import com.aliyun.auikits.voiceroom.external.RtcInfo;

import java.lang.ref.WeakReference;
import java.util.Map;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;

/**
 * 封装了场景化API的服务封装
 */
public class ChatRoomService {
    public static final int RST_JOIN_MIC_SUCCESS = 0;
    public static final int RST_JOIN_MIC_FULL = 1;
    public static final int RST_JOIN_MIC_ALREADY_JOIN = 2;
    private static final String TAG = "ChatRoomService";

    public static Observable<Integer> joinMic(ARTCVoiceRoomEngine roomController, boolean microphoneSwitch) {

        return Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> emitter) throws Throwable {
                ChatRoomCallback roomCallback = new ChatRoomCallback() {
                    //请求连麦收到响应
                    @Override
                    public void onResponseMic(MicRequestResult rs) {
//                        rs.agree = true;
                        if(rs.reason == RST_JOIN_MIC_SUCCESS || rs.reason == RST_JOIN_MIC_ALREADY_JOIN) {
                            //同意后上麦
                            //调用最新接口
                            MicInfo micInfo = new MicInfo(rs.micPosition, !microphoneSwitch);
                            final WeakReference<ChatRoomCallback> tempRoomCallback = new WeakReference<>(this);
                            roomController.joinMic(micInfo, new ActionCallback() {
                                @Override
                                public void onResult(int code, String msg, Map<String, Object> params) {
                                    //错误的话往外回传，并
                                    if(code != ChatRoomManager.CODE_SUCCESS) {
                                        if(tempRoomCallback.get() != null) {
                                            roomController.removeObserver(tempRoomCallback.get());
                                        }

                                        emitter.onNext(code);
                                        emitter.onComplete();
                                    }
                                }
                            });
                        } else {
                            roomController.removeObserver(this);
                            emitter.onNext(rs.reason);
                            emitter.onComplete();
                        }
                    }

                    @Override
                    public void onJoinedMic(UserInfo user) {
                        //当前用户
                        if(user.equals(roomController.getCurrentUser())) {
                            emitter.onNext(ChatRoomManager.CODE_SUCCESS);
                            emitter.onComplete();
                            roomController.removeObserver(this);
                        }
                    }
                };

                roomController.addObserver(roomCallback);
                roomController.requestMic(new ActionCallback() {
                    @Override
                    public void onResult(int code, String msg, Map<String, Object> params) {
                        if(code != ChatRoomManager.CODE_SUCCESS) {
                            roomController.removeObserver(roomCallback);
                            emitter.onNext(code);
                            emitter.onComplete();
                        }
                        //请求成功在ChatRoomCallback的onResponseMic等待响应
                    }
                });
            }
        });

    }

    public static Observable<Integer> leaveMic(ARTCVoiceRoomEngine roomController) {
        return Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> emitter) throws Throwable {
                ChatRoomCallback roomCallback = new ChatRoomCallback() {
                    @Override
                    public void onLeavedMic(UserInfo user) {
                        //自动收到的回调
                        if(user.equals(roomController.getCurrentUser())) {
                            roomController.removeObserver(this);
                            emitter.onNext(ChatRoomManager.CODE_SUCCESS);
                            emitter.onComplete();
                        }

                    }
                };

                ActionCallback actionCallback = new ActionCallback() {
                    @Override
                    public void onResult(int code, String msg, Map<String, Object> params) {
                        if(code != ChatRoomManager.CODE_SUCCESS) {
                            emitter.onNext(code);
                            emitter.onComplete();
                            roomController.removeObserver(roomCallback);
                        }
                    }
                };
                roomController.addObserver(roomCallback);
                roomController.leaveMic(actionCallback);
            }
        });
    }


    public static Observable<Integer> joinRoom(ARTCVoiceRoomEngine roomController, RoomInfo roomInfo, RtcInfo rtcInfo) {

        return Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> emitter) throws Throwable {
                ChatRoomCallback roomCallback = new ChatRoomCallback() {
                    @Override
                    public void onJoin(String roomId, String uid) {
                        Log.v(TAG, "onJoin:" + roomId);
                        if(roomId.equals(roomInfo.roomId)) {
                            roomController.removeObserver(this);
                            emitter.onNext(ChatRoomManager.CODE_SUCCESS);
                            emitter.onComplete();
                        }
                    }
                };

                ActionCallback actionCallback = new ActionCallback() {
                    @Override
                    public void onResult(int code, String msg, Map<String, Object> params) {
                        Log.v(TAG, "join room:" + code + ",msg:" + msg + ",roomId:" + roomInfo.roomId);
                        if(code != ChatRoomManager.CODE_SUCCESS) {
                            emitter.onNext(code);
                            emitter.onComplete();
                            roomController.removeObserver(roomCallback);
                        }
                    }
                };
                roomController.addObserver(roomCallback);
                roomController.joinRoom(roomInfo, rtcInfo, actionCallback);
            }
        });

    }

    public static Observable<Integer> exitRoom(ARTCVoiceRoomEngine roomController, boolean isCompere) {
        return Observable.create(new ObservableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(@NonNull ObservableEmitter<Integer> emitter) throws Throwable {
                        if(isCompere) {
                            roomController.dismissRoom(null);
                        } else {
                            roomController.leaveRoom(null);
                        }

                        emitter.onNext(ChatRoomManager.CODE_SUCCESS);
                        emitter.onComplete();
                    }
                });
    }
}
