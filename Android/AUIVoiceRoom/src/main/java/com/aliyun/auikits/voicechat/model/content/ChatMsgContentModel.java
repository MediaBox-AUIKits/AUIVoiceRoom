package com.aliyun.auikits.voicechat.model.content;

import android.content.Context;

import com.aliyun.auikits.voicechat.R;
import com.aliyun.auikits.voicechat.base.card.CardEntity;
import com.aliyun.auikits.voicechat.base.feed.AbsContentModel;
import com.aliyun.auikits.voicechat.base.feed.BizParameter;
import com.aliyun.auikits.voicechat.base.feed.IBizCallback;
import com.aliyun.auikits.voicechat.model.entity.ChatMember;
import com.aliyun.auikits.voicechat.model.entity.ChatMessage;
import com.aliyun.auikits.voicechat.model.entity.ChatRoom;
import com.aliyun.auikits.voicechat.model.entity.ChatRoomCallback;
import com.aliyun.auikits.voicechat.widget.card.CardTypeDef;
import com.aliyun.auikits.voice.ARTCVoiceRoomEngine;
import com.aliyun.auikits.voice.ARTCVoiceRoomEngineDelegate;
import com.aliyun.auikits.voiceroom.bean.UserInfo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ChatMsgContentModel extends AbsContentModel<CardEntity> {
    private ChatRoom chatRoom;
    private ARTCVoiceRoomEngine roomController;
    private ARTCVoiceRoomEngineDelegate roomCallback;
    private WeakReference<Context> contextRef;
    public ChatMsgContentModel(Context context, ChatRoom chatRoom, ARTCVoiceRoomEngine roomController) {
        super();
        this.chatRoom = chatRoom;
        this.roomController = roomController;
        this.contextRef = new WeakReference<>(context);
        this.roomCallback = new ChatRoomCallback() {

            @Override
            public void onJoinedMic(UserInfo user) {
                Context context1 = ChatMsgContentModel.this.contextRef.get();

                //主持人加入麦不添加进消息列表
                if(context1 != null && user.micPosition > 0) {
                    CardEntity cardEntity = new CardEntity();
                    cardEntity.cardType = CardTypeDef.CHAT_MESSAGE_CARD;
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setType(ChatMessage.TYPE_NOTICE);
                    chatMessage.setContent(String.format(context1.getString(R.string.voicechat_join_mic_suffix), user.userName, user.micPosition));
                    cardEntity.bizData = chatMessage;

                    List<CardEntity> cardEntityList = new ArrayList<>();
                    cardEntityList.add(cardEntity);
                    insertContent(cardEntityList);
                }
            }

            @Override
            public void onLeavedMic(UserInfo user) {
                Context context1 = ChatMsgContentModel.this.contextRef.get();
                //主持人离开麦不添加进消息列表
                if(context1 != null && user.micPosition > 0) {
                    CardEntity cardEntity = new CardEntity();
                    cardEntity.cardType = CardTypeDef.CHAT_MESSAGE_CARD;
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setType(ChatMessage.TYPE_NOTICE);
                    chatMessage.setContent(String.format(context1.getString(R.string.voicechat_leave_mic_suffix), user.userName, user.micPosition));
                    cardEntity.bizData = chatMessage;

                    List<CardEntity> cardEntityList = new ArrayList<>();
                    cardEntityList.add(cardEntity);
                    insertContent(cardEntityList);
                }
            }

            @Override
            public void onJoinedRoom(UserInfo user) {
                Context context1 = ChatMsgContentModel.this.contextRef.get();

                if(context1 != null) {
                    CardEntity cardEntity = new CardEntity();
                    cardEntity.cardType = CardTypeDef.CHAT_MESSAGE_CARD;
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setType(ChatMessage.TYPE_NOTICE);
                    chatMessage.setContent(String.format(context1.getString(R.string.voicechat_join_room_suffix), user.userName));
                    cardEntity.bizData = chatMessage;

                    List<CardEntity> cardEntityList = new ArrayList<>();
                    cardEntityList.add(cardEntity);
                    insertContent(cardEntityList);
                }
            }

            @Override
            public void onReceivedTextMessage(UserInfo user, String text) {
                CardEntity cardEntity = new CardEntity();
                cardEntity.cardType = CardTypeDef.CHAT_MESSAGE_CARD;

                ChatMember chatMember = new ChatMember(user);
                if(user.userId.equals(chatRoom.getCompere().getId())) {
                    chatMember.setIdentifyFlag(ChatMember.IDENTIFY_FLAG_COMPERE);
                } else if(user.userId.equals(chatRoom.getSelf().getId())) {
                    chatMember.setIdentifyFlag(ChatMember.IDENTIFY_FLAG_SELF);
                }

                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setType(ChatMessage.TYPE_CHAT_MSG);
                chatMessage.setMember(chatMember);
                chatMessage.setContent(text);
                cardEntity.bizData = chatMessage;

                List<CardEntity> cardEntityList = new ArrayList<>();
                cardEntityList.add(cardEntity);
                insertContent(cardEntityList);
            }

        };
        this.roomController.addObserver(this.roomCallback);
    }

    @Override
    public void release() {
        super.release();
        this.roomController.removeObserver(this.roomCallback);
    }

    @Override
    public void initData(BizParameter parameter, IBizCallback<CardEntity> callback) {

        List<CardEntity> chatMsgItemList = new ArrayList<>();
        if(contextRef != null && contextRef.get() != null) {
            Context context = contextRef.get();
            CardEntity cardEntity = new CardEntity();
            cardEntity.cardType = CardTypeDef.CHAT_MESSAGE_CARD;
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setType(ChatMessage.TYPE_NOTICE);
            chatMessage.setContent(context.getString(R.string.voicechat_msg_tips));
            cardEntity.bizData = chatMessage;
            chatMsgItemList.add(cardEntity);
        }

        if(callback != null) {
            callback.onSuccess(chatMsgItemList);
        }
    }

    @Override
    public void fetchData(boolean isPullToRefresh, BizParameter parameter, IBizCallback<CardEntity> callback) {

    }
}
