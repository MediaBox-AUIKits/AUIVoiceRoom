package com.aliyun.auikits.voicechat.vm;

import android.content.Context;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.aliyun.auikits.voicechat.model.entity.ChatMember;
import com.aliyun.auikits.voicechat.model.entity.ChatRoom;
import com.aliyun.auikits.voicechat.model.entity.ChatRoomCallback;
import com.aliyun.auikits.voice.ARTCVoiceRoomEngine;
import com.aliyun.auikits.voice.ARTCVoiceRoomEngineDelegate;
import com.aliyun.auikits.voiceroom.bean.UserInfo;


public class ChatViewModel extends ViewModel {

    public ChatMicMemberViewModel compereViewModel;
    public ChatHeaderViewModel headerViewModel;
    public ChatToolbarViewModel toolbarViewModel;
    public ChatConnectViewModel chatConnectViewModel;
    private ARTCVoiceRoomEngineDelegate roomCallback;
    private ARTCVoiceRoomEngine roomController;
    private ChatRoom chatRoom;

    public void bind(Context context, ChatRoom room, ARTCVoiceRoomEngine roomController) {
        this.chatRoom =  room;
        //监听主持人的连麦及网络状态
        this.roomCallback = new ChatRoomCallback() {

            @Override
            public void onMicUserMicrophoneChanged(UserInfo user, boolean open) {
                if(open){
                    if(ChatViewModel.this.roomController.isAnchor(user)) {
                        ChatMember chatMember = ChatViewModel.this.chatRoom.getCompere();
                        chatMember.setMicrophoneStatus(ChatMember.MICROPHONE_STATUS_ON);
                        compereViewModel.bind(chatMember);
                    }
                }else{
                    if(ChatViewModel.this.roomController.isAnchor(user)) {
                        ChatMember chatMember = ChatViewModel.this.chatRoom.getCompere();
                        chatMember.setMicrophoneStatus(ChatMember.MICROPHONE_STATUS_OFF);
                        compereViewModel.bind(chatMember);
                    }
                }
            }

            @Override
            public void onMicUserSpeakStateChanged(UserInfo user) {
                boolean compereSpeaking = ChatViewModel.this.roomController.isAnchor(user);
                if(compereSpeaking) {
                    ChatMember chatMember = ChatViewModel.this.chatRoom.getCompere();
                    if(user.speaking != chatMember.isSpeaking()) {
                        chatMember.setSpeaking(user.speaking);
                        compereViewModel.bind(chatMember);
                    }

                }

            }

            @Override
            public void onNetworkStateChanged(UserInfo user) {
                if(ChatViewModel.this.roomController.isAnchor(user)) {
                    ChatMember chatMember = ChatViewModel.this.chatRoom.getCompere();
                    chatMember.setNetworkStatus(user.networkState);
                    compereViewModel.bind(chatMember);
                }
            }

        } ;
        this.roomController = roomController;
        this.roomController.addObserver(this.roomCallback);

        ViewModelProvider viewModelProvider = new ViewModelProvider((ViewModelStoreOwner) context);
        compereViewModel = viewModelProvider.get(ChatMicMemberViewModel.class);
        headerViewModel = viewModelProvider.get(ChatHeaderViewModel.class);
        toolbarViewModel = viewModelProvider.get(ChatToolbarViewModel.class);
        chatConnectViewModel = viewModelProvider.get(ChatConnectViewModel.class);

        headerViewModel.bind(room, this.roomController);
        compereViewModel.bind(room.getCompere());
        toolbarViewModel.bind(room.isCompere(), room.getSelf(), this.roomController);
        chatConnectViewModel.bind(room, this.roomController);
        toolbarViewModel.setMicEntryShowObservable(chatConnectViewModel.getShowMicEntryObservable());
    }

    public void unBind() {
        this.headerViewModel.unBind();
        this.toolbarViewModel.unBind();
        this.chatConnectViewModel.unBind();
        this.roomController.removeObserver(this.roomCallback);
        roomController = null;
    }

    public ARTCVoiceRoomEngine getRoomController() {
        return roomController;
    }
}
