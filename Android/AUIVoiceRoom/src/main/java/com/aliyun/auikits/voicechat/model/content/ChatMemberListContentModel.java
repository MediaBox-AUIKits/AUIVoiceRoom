package com.aliyun.auikits.voicechat.model.content;

import com.alivc.auicommon.common.base.util.ThreadUtil;
import com.aliyun.auikits.voicechat.base.card.CardEntity;
import com.aliyun.auikits.voicechat.base.feed.AbsContentModel;
import com.aliyun.auikits.voicechat.base.feed.BizParameter;
import com.aliyun.auikits.voicechat.base.feed.IBizCallback;
import com.aliyun.auikits.voicechat.model.entity.ChatMember;
import com.aliyun.auikits.voicechat.util.UserFaker;
import com.aliyun.auikits.voicechat.widget.card.CardTypeDef;
import com.github.javafaker.Faker;

import java.util.ArrayList;
import java.util.List;

public class ChatMemberListContentModel extends AbsContentModel<CardEntity> {

    public ChatMemberListContentModel() {
    }

    @Override
    public void initData(BizParameter parameter, IBizCallback<CardEntity> callback) {

        //TODO 读取当前所有成员
        ThreadUtil.runOnSubThread(new Runnable() {

            @Override
            public void run() {
                Faker faker = new Faker();
                List<CardEntity> chatRoomItemList = new ArrayList<>();
                for(int i = 1; i <= 10; i++) {
                    UserFaker userFaker = UserFaker.generateFakeUser();
                    CardEntity cardEntity = new CardEntity();

                    cardEntity.cardType = CardTypeDef.CHAT_MEMBER_CARD;
                    ChatMember chatMember;
                    chatMember = new ChatMember(String.valueOf(faker.number().numberBetween(10000, 20000)));
                    chatMember.setIndex(i);
                    chatMember.setName(userFaker.getName());
                    chatMember.setAvatar(userFaker.getAvatarUrl());
                    chatMember.setMicrophoneStatus(faker.number().numberBetween(ChatMember.MICROPHONE_STATUS_ON, ChatMember.MICROPHONE_STATUS_DISABLE));

                    if(i == 1) {
                        chatMember.setIdentifyFlag(ChatMember.IDENTIFY_FLAG_COMPERE);
                    } else if(i <=5) {
                        chatMember.setIdentifyFlag(ChatMember.IDENTIFY_FLAG_CHAT);
                    } else {
                        chatMember.setIdentifyFlag(ChatMember.IDENTIFY_FLAG_AUDIENCE);
                    }

                    cardEntity.bizData = chatMember;
                    chatRoomItemList.add(cardEntity);
                }

                ThreadUtil.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(callback != null) {
                            callback.onSuccess(chatRoomItemList);
//                            callback.onError(-1, "");
                        }
                    }
                });

            }
        });

    }

    @Override
    public void fetchData(boolean isPullToRefresh, BizParameter parameter, IBizCallback<CardEntity> callback) {
        ThreadUtil.runOnSubThread(new Runnable() {

            @Override
            public void run() {
                Faker faker = new Faker();
                List<CardEntity> chatRoomItemList = new ArrayList<>();
                for(int i = 1; i <= 10; i++) {
                    UserFaker userFaker = UserFaker.generateFakeUser();
                    CardEntity cardEntity = new CardEntity();

                    cardEntity.cardType = CardTypeDef.CHAT_MEMBER_CARD;
                    ChatMember chatMember;
                    chatMember = new ChatMember(String.valueOf(faker.number().numberBetween(10000, 20000)));
                    chatMember.setIndex(i);
                    chatMember.setName(userFaker.getName());
                    chatMember.setAvatar(userFaker.getAvatarUrl());
                    chatMember.setMicrophoneStatus(faker.number().numberBetween(ChatMember.MICROPHONE_STATUS_ON, ChatMember.MICROPHONE_STATUS_DISABLE));
                    chatMember.setIdentifyFlag(ChatMember.IDENTIFY_FLAG_AUDIENCE);
                    cardEntity.bizData = chatMember;
                    chatRoomItemList.add(cardEntity);
                }

                ThreadUtil.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(callback != null) {
                            callback.onSuccess(chatRoomItemList);
                        }
                    }
                });

            }
        });

    }
}
