package com.aliyun.auikits.voicechat.model.content;

import androidx.annotation.NonNull;

import com.aliyun.auikits.voicechat.base.card.CardEntity;
import com.aliyun.auikits.voicechat.base.feed.AbsContentModel;
import com.aliyun.auikits.voicechat.base.feed.BizParameter;
import com.aliyun.auikits.voicechat.base.feed.IBizCallback;
import com.aliyun.auikits.voicechat.model.entity.ChatMember;
import com.aliyun.auikits.voicechat.model.entity.ChatRoomCallback;
import com.aliyun.auikits.voicechat.widget.card.CardTypeDef;
import com.aliyun.auikits.voiceroom.AUIVoiceRoom;
import com.aliyun.auikits.voiceroom.AUIVoiceRoomCallback;
import com.aliyun.auikits.voiceroom.bean.UserInfo;
import com.aliyun.auikits.voiceroom.callback.ActionCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class ChatMicMemberContentModel extends AbsContentModel<CardEntity> {
    public static final int MAX_MEMBER_COUNT = 8;
    private ChatMember self = null;
    private AUIVoiceRoom roomController = null;
    private AUIVoiceRoomCallback roomCallback;
    private List<CardEntity> roomMemberList = new ArrayList<>();
    private int lastSpeakingPos = -1;

    public ChatMicMemberContentModel(ChatMember self, AUIVoiceRoom roomController) {
        this.self = self;

        for(int i = 1; i <= MAX_MEMBER_COUNT; i++) {
            CardEntity cardEntity = new CardEntity();

            cardEntity.cardType = CardTypeDef.CHAT_MEMBER_EMPTY_CARD;
            ChatMember chatMember = new ChatMember();
            chatMember.setIndex(i);
            cardEntity.bizData = chatMember;
            roomMemberList.add(cardEntity);
        }

        this.roomCallback = new ChatRoomCallback() {
            @Override
            public void onUserJoinMic(UserInfo user) {
                if(user.micPosition > 0 && user.micPosition <= MAX_MEMBER_COUNT) {
                    CardEntity cardEntity = roomMemberList.get(user.micPosition-1);
                    cardEntity.cardType = CardTypeDef.CHAT_MEMBER_CARD;
                    ChatMember chatMember;
                    chatMember = new ChatMember(user);
                    cardEntity.bizData = chatMember;
                    if(user.equals(roomController.getCurrentUser())) {
                        chatMember.setIdentifyFlag(ChatMember.IDENTIFY_FLAG_SELF);
                    }
                    updateContent(cardEntity, user.micPosition-1);
                }
            }

            @Override
            public void onUserLeaveMic(UserInfo user) {
                if(user.micPosition > 0 && user.micPosition <= MAX_MEMBER_COUNT) {
                    CardEntity cardEntity = roomMemberList.get(user.micPosition-1);
                    cardEntity.cardType = CardTypeDef.CHAT_MEMBER_EMPTY_CARD;
                    ChatMember chatMember = new ChatMember();
                    chatMember.setIndex(user.micPosition);
                    cardEntity.bizData = chatMember;
                    updateContent(cardEntity, user.micPosition-1);
                }
            }

            @Override
            public void onUserMicOn(UserInfo user) {
                if(user.micPosition > 0 && user.micPosition <= MAX_MEMBER_COUNT) {
                    CardEntity cardEntity = roomMemberList.get(user.micPosition-1);
                    if(Objects.equals(cardEntity.cardType, CardTypeDef.CHAT_MEMBER_CARD)) {
                        ChatMember chatMember = (ChatMember) cardEntity.bizData;
                        if(user.userId.equals(chatMember.getId())) {
                            chatMember.setMicrophoneStatus(ChatMember.MICROPHONE_STATUS_ON);
                            updateContent(cardEntity, user.micPosition-1);
                        }
                    }

                }
            }

            @Override
            public void onUserMicOff(UserInfo user) {
                if(user.micPosition > 0 && user.micPosition <= MAX_MEMBER_COUNT) {
                    CardEntity cardEntity = roomMemberList.get(user.micPosition-1);
                    if(Objects.equals(cardEntity.cardType, CardTypeDef.CHAT_MEMBER_CARD)) {
                        ChatMember chatMember = (ChatMember) cardEntity.bizData;
                        if(user.userId.equals(chatMember.getId())) {
                            chatMember.setMicrophoneStatus(ChatMember.MICROPHONE_STATUS_OFF);
                            updateContent(cardEntity, user.micPosition-1);
                        }
                    }
                }
            }

            @Override
            public void onUserSpeakState(UserInfo user) {
                //有人在讲话，且和上次讲话的用户不是同一个,则把上次讲话的状态设置成false
                if(user.speaking && lastSpeakingPos > 0 && lastSpeakingPos != user.micPosition) {
                    CardEntity cardEntity = roomMemberList.get(lastSpeakingPos-1);
                    if(Objects.equals(cardEntity.cardType, CardTypeDef.CHAT_MEMBER_CARD)) {
                        ChatMember chatMember = (ChatMember) cardEntity.bizData;
                        if(chatMember.isSpeaking()) {
                            chatMember.setSpeaking(false);
                            updateContent(cardEntity, lastSpeakingPos-1);
                        }
                    }

                }
                //如果是连麦客户的讲话状态变化
                if(user.micPosition > 0 && user.micPosition <= MAX_MEMBER_COUNT) {
                    CardEntity cardEntity = roomMemberList.get(user.micPosition-1);
                    if(Objects.equals(cardEntity.cardType, CardTypeDef.CHAT_MEMBER_CARD)) {
                        ChatMember chatMember = (ChatMember) cardEntity.bizData;
                        if(user.userId.equals(chatMember.getId()) && user.speaking != chatMember.isSpeaking()) {
                            chatMember.setSpeaking(user.speaking);
                            updateContent(cardEntity, user.micPosition-1);
                        }
                    }
                }

                //更新讲话的麦位： 有可能是主持人及连麦客户
                if(user.speaking) {
                    //如果有用户在讲话，判断当前用户是否是麦上用户，如果是则更新位置
                    if(user.micPosition > 0 && user.micPosition <= MAX_MEMBER_COUNT) {
                        lastSpeakingPos = user.micPosition;
                    } else {
                        //不是麦上用户（主持人），则设置成-1
                        lastSpeakingPos = -1;
                    }
                } else {
                    //如果没有讲话，则设置成-1
                    lastSpeakingPos = -1;
                }

            }

            @Override
            public void onUserNetworkState(UserInfo user) {
                if(user.micPosition > 0 && user.micPosition <= MAX_MEMBER_COUNT) {
                    CardEntity cardEntity = roomMemberList.get(user.micPosition-1);
                    if(Objects.equals(cardEntity.cardType, CardTypeDef.CHAT_MEMBER_CARD)) {
                        ChatMember chatMember = (ChatMember) cardEntity.bizData;
                        if(user.userId.equals(chatMember.getId())) {
                            chatMember.setNetworkStatus(user.networkState);
                            updateContent(cardEntity, user.micPosition-1);
                        }

                    }
                }
            }

            @Override
            public void onRoomMicListChanged(List<UserInfo> micUsers) {
                for(UserInfo micUser : micUsers) {
                    if(micUser.micPosition >0 && micUser.micPosition <= MAX_MEMBER_COUNT) {
                        CardEntity cardEntity = roomMemberList.get(micUser.micPosition - 1);
                        ChatMember chatMember = new ChatMember(micUser);
                        cardEntity.bizData = chatMember;
                        if(micUser.equals(roomController.getCurrentUser())) {
                            chatMember.setIdentifyFlag(ChatMember.IDENTIFY_FLAG_SELF);
                        }
                        cardEntity.cardType = CardTypeDef.CHAT_MEMBER_CARD;
                    }
                }
                updateContent(roomMemberList);
            }
        };
        this.roomController = roomController;
        this.roomController.addRoomCallback(this.roomCallback);

    }

    public void release() {
        this.roomController.removeRoomCallback(this.roomCallback);
    }


    @Override
    public void initData(BizParameter parameter, IBizCallback<CardEntity> callback) {

        if(callback != null) {
            callback.onSuccess(roomMemberList);
        }

        //只是发起请求，会在onRoomMicListChanged中返回
        roomController.listMicUserList(new ActionCallback() {
            @Override
            public void onResult(int code, String msg, Map<String, Object> params) {
            }
        });

    }

    @Override
    public void fetchData(boolean isPullToRefresh, BizParameter parameter, IBizCallback<CardEntity> callback) {

    }

}
