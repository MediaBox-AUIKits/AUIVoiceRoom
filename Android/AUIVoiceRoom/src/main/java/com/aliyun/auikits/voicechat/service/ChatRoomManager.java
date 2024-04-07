package com.aliyun.auikits.voicechat.service;


import android.util.Log;

import com.aliyun.auikits.voiceroom.AUIVoiceRoom;
import com.aliyun.auikits.voiceroom.factory.AUIVoiceRoomFactory;

import java.util.concurrent.ConcurrentHashMap;

public class ChatRoomManager {
    private static final String TAG = "ChatRoomManager";
    public static final int CODE_SUCCESS = 0;
    private ConcurrentHashMap<String, Object> globalParams = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, AUIVoiceRoom> roomMap = new ConcurrentHashMap<>();

    private ChatRoomManager() {

    }

    private static class ChatRoomManagerInstance {
        private static ChatRoomManager instance = new ChatRoomManager();
    }

    public static ChatRoomManager getInstance() {
        return ChatRoomManagerInstance.instance;
    }

    public AUIVoiceRoom createVoiceRoom(String roomId) {
        AUIVoiceRoom room = this.roomMap.get(roomId);
        if(room == null) {
            room = AUIVoiceRoomFactory.createVoiceRoom();

            Log.v(TAG, "create RoomController :" + room.hashCode());
            this.roomMap.put(roomId, room);
        } else {
            Log.v(TAG, "hit cache RoomController :" + roomId);
        }

        return room;
    }

    public AUIVoiceRoom getVoiceRoom(String roomId) {
        return this.roomMap.get(roomId);
    }

    public void destroyVoiceRoom(AUIVoiceRoom roomController) {
        destroyVoiceRoom(roomController.getRoomInfo().roomId);
    }

    public void destroyVoiceRoom(String roomId) {
        AUIVoiceRoom auiVoiceRoom = getVoiceRoom(roomId);
        if(auiVoiceRoom != null) {
            auiVoiceRoom.release();
            Log.v(TAG, "destroy RoomController :" + auiVoiceRoom.hashCode());
            this.roomMap.remove(roomId);
        }

    }

    public void addGlobalParam(String key, Object value) {
        globalParams.put(key, value);
    }

    public Object getGlobalParam(String key) {
        return globalParams.get(key);
    }

    public void destroy() {
        for(AUIVoiceRoom room : this.roomMap.values()) {
            room.release();
        }
        this.roomMap.clear();
        this.globalParams.clear();
    }

}
