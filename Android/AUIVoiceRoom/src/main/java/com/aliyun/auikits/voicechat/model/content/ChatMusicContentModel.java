package com.aliyun.auikits.voicechat.model.content;


import com.alivc.auicommon.common.base.util.ThreadUtil;
import com.aliyun.auikits.voicechat.base.card.CardEntity;
import com.aliyun.auikits.voicechat.base.feed.AbsContentModel;
import com.aliyun.auikits.voicechat.base.feed.BizParameter;
import com.aliyun.auikits.voicechat.base.feed.IBizCallback;
import com.aliyun.auikits.voicechat.base.feed.IContentObserver;
import com.aliyun.auikits.voicechat.model.entity.ChatMusicItem;
import com.aliyun.auikits.voicechat.widget.card.CardTypeDef;

import java.util.ArrayList;
import java.util.List;

/**
 * 背景音乐内容数据
 */
public class ChatMusicContentModel extends AbsContentModel<CardEntity> {


    private ChatMusicItem[] musicArray = new ChatMusicItem[] {
            new ChatMusicItem("1", "hello", "world", ""),
            new ChatMusicItem("2", "hello222", "world1111", ""),
            new ChatMusicItem("3", "hello3333", "world222", ""),
    };

    private int lastPlayingItem = -1;

    public ChatMusicContentModel() {
    }

    @Override
    public void initData(BizParameter parameter, IBizCallback<CardEntity> callback) {
        ThreadUtil.runOnSubThread(new Runnable() {
            @Override
            public void run() {
                List<CardEntity> cardEntityList = getCardDataList();

                ThreadUtil.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(callback != null) {
                            callback.onSuccess(cardEntityList);
                        }
                    }
                });

            }
        });


    }

    @Override
    public void fetchData(boolean isPullToRefresh, BizParameter parameter, IBizCallback<CardEntity> callback) {
    }

    public void playOrStopItem(int position) {
        ThreadUtil.runOnSubThread(new Runnable() {
            @Override
            public void run() {
                List<CardEntity> cardEntityList = getCardDataList();

                CardEntity cardEntity = cardEntityList.get(position);
                if(lastPlayingItem == -1) {
                    ((ChatMusicItem)cardEntity.bizData).setPlaying(true);
                    lastPlayingItem = position;
                } else if(position == lastPlayingItem) {
                    ((ChatMusicItem)cardEntity.bizData).setPlaying(false);
                    lastPlayingItem = -1;
                } else {
                    ((ChatMusicItem)cardEntity.bizData).setPlaying(true);
                    lastPlayingItem = position;
                }

                ThreadUtil.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for(IContentObserver<CardEntity> observer : ChatMusicContentModel.this.observers) {
                            observer.onContentUpdate(cardEntityList);
                        }
                    }
                });

            }
        });
    }

    private List<CardEntity> getCardDataList() {
        List<CardEntity> cardDataList = new ArrayList<>();
        for(int i = 0; i <= 2; i++) {
            CardEntity cardEntity = new CardEntity();
            cardEntity.cardType = CardTypeDef.CHAT_MUSIC_CARD;
            ChatMusicItem chatMusicItem = musicArray[i];
            chatMusicItem.setPlaying(false);
            cardEntity.bizData = chatMusicItem;
            cardDataList.add(cardEntity);
        }

        return cardDataList;
    }
}
