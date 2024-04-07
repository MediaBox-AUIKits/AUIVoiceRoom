package com.aliyun.auikits.voicechat.vm;

import android.content.Context;

import androidx.databinding.Observable;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.aliyun.auikits.voicechat.base.card.CardEntity;
import com.aliyun.auikits.voicechat.model.entity.ChatMember;
import com.aliyun.auikits.voicechat.model.entity.ChatRoom;
import com.aliyun.auikits.voicechat.model.entity.ChatRoomCallback;
import com.aliyun.auikits.voicechat.widget.card.CardTypeDef;
import com.aliyun.auikits.voiceroom.AUIVoiceRoom;
import com.aliyun.auikits.voiceroom.AUIVoiceRoomCallback;
import com.aliyun.auikits.voiceroom.bean.UserInfo;

import java.util.List;


public class ChatViewModel extends ViewModel {

    public ChatMicMemberViewModel compereViewModel;
    public ChatHeaderViewModel headerViewModel;
    public ChatToolbarViewModel toolbarViewModel;
    public ChatConnectViewModel chatConnectViewModel;
    private AUIVoiceRoomCallback roomCallback;
    private AUIVoiceRoom roomController;
    private ChatRoom chatRoom;

    public void bind(Context context, ChatRoom room, AUIVoiceRoom roomController) {
        this.chatRoom =  room;
        //监听主持人的连麦及网络状态
        this.roomCallback = new ChatRoomCallback() {

            @Override
            public void onUserMicOn(UserInfo user) {
                if(ChatViewModel.this.roomController.isHost(user)) {
                    ChatMember chatMember = ChatViewModel.this.chatRoom.getCompere();
                    chatMember.setMicrophoneStatus(ChatMember.MICROPHONE_STATUS_ON);
                    compereViewModel.bind(chatMember);
                }
            }

            @Override
            public void onUserMicOff(UserInfo user) {
                if(ChatViewModel.this.roomController.isHost(user)) {
                    ChatMember chatMember = ChatViewModel.this.chatRoom.getCompere();
                    chatMember.setMicrophoneStatus(ChatMember.MICROPHONE_STATUS_OFF);
                    compereViewModel.bind(chatMember);
                }
            }

            @Override
            public void onUserSpeakState(UserInfo user) {
                boolean compereSpeaking = ChatViewModel.this.roomController.isHost(user);
                if(compereSpeaking) {
                    ChatMember chatMember = ChatViewModel.this.chatRoom.getCompere();
                    if(user.speaking != chatMember.isSpeaking()) {
                        chatMember.setSpeaking(user.speaking);
                        compereViewModel.bind(chatMember);
                    }

                }

            }

            @Override
            public void onUserNetworkState(UserInfo user) {
                if(ChatViewModel.this.roomController.isHost(user)) {
                    ChatMember chatMember = ChatViewModel.this.chatRoom.getCompere();
                    chatMember.setNetworkStatus(user.networkState);
                    compereViewModel.bind(chatMember);
                }
            }

        } ;
        this.roomController = roomController;
        this.roomController.addRoomCallback(this.roomCallback);

        ViewModelProvider viewModelProvider = new ViewModelProvider((ViewModelStoreOwner) context);
        compereViewModel = viewModelProvider.get(ChatMicMemberViewModel.class);
        headerViewModel = viewModelProvider.get(ChatHeaderViewModel.class);
        toolbarViewModel = viewModelProvider.get(ChatToolbarViewModel.class);
        chatConnectViewModel = viewModelProvider.get(ChatConnectViewModel.class);

        headerViewModel.bind(room, this.roomController);
        compereViewModel.bind(room.getCompere());
        toolbarViewModel.bind(room.getSelf(), this.roomController);
        chatConnectViewModel.bind(room, this.roomController);
    }

    public void unBind() {
        this.headerViewModel.unBind();
        this.toolbarViewModel.unBind();
        this.chatConnectViewModel.unBind();
        this.roomController.removeRoomCallback(this.roomCallback);
        roomController = null;
    }

    public AUIVoiceRoom getRoomController() {
        return roomController;
    }
}
