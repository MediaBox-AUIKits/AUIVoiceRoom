package com.aliyun.auikits.voicechat.model.content;


import android.content.Context;

import com.alivc.auicommon.common.base.util.ThreadUtil;
import com.aliyun.auikits.voicechat.R;
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
 * 音效内容数据
 */
public class ChatSoundEffectContentModel extends AbsContentModel<CardEntity> {


    private ChatMusicItem[] musicArray;

    private int lastPlayingItem = -1;

    public ChatSoundEffectContentModel(Context context) {
        musicArray = new ChatMusicItem[] {
                new ChatMusicItem("1", context.getString(R.string.voicechat_laughter), "", ""),
                new ChatMusicItem("2", context.getString(R.string.voicechat_applause), "", ""),
        };
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
                        for(IContentObserver<CardEntity> observer : ChatSoundEffectContentModel.this.observers) {
                            observer.onContentUpdate(cardEntityList);
                        }
                    }
                });

            }
        });
    }

    private List<CardEntity> getCardDataList() {
        List<CardEntity> cardDataList = new ArrayList<>();
        for(int i = 0; i <= 1; i++) {
            CardEntity cardEntity = new CardEntity();
            cardEntity.cardType = CardTypeDef.CHAT_SOUND_EFFECT_CARD;
            ChatMusicItem chatMusicItem = musicArray[i];
            chatMusicItem.setPlaying(false);
            cardEntity.bizData = chatMusicItem;
            cardDataList.add(cardEntity);
        }

        return cardDataList;
    }
}
