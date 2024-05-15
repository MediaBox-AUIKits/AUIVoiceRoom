package com.aliyun.auikits.voicechat.model.content;


import com.alivc.auicommon.common.base.util.ThreadUtil;
import com.aliyun.auikits.voicechat.R;
import com.aliyun.auikits.voicechat.base.card.CardEntity;
import com.aliyun.auikits.voicechat.base.feed.AbsContentModel;
import com.aliyun.auikits.voicechat.base.feed.BizParameter;
import com.aliyun.auikits.voicechat.base.feed.IBizCallback;
import com.aliyun.auikits.voicechat.base.feed.IContentObserver;
import com.aliyun.auikits.voicechat.model.entity.ChatSoundMix;
import com.aliyun.auikits.voicechat.widget.card.CardTypeDef;

import java.util.ArrayList;
import java.util.List;

/**
 * 调音台混响内容数据
 */
public class ChatReverbContentModel extends AbsContentModel<CardEntity> {


    private ChatSoundMix[] effectArray = new ChatSoundMix[] {
            new ChatSoundMix("0", R.string.voicechat_none, R.drawable.voicechat_ic_none),
            new ChatSoundMix("1", R.string.voicechat_reverb_rensheng, R.drawable.voicechat_ic_reverb_rensheng),
            new ChatSoundMix("2", R.string.voicechat_reverb_rensheng2, R.drawable.voicechat_ic_reverb_rensheng2),
            new ChatSoundMix("3", R.string.voicechat_reverb_zaotang, R.drawable.voicechat_ic_reverb_zaotang),
            new ChatSoundMix("4", R.string.voicechat_reverb_minglaing, R.drawable.voicechat_ic_reverb_minglaing),
            new ChatSoundMix("5", R.string.voicechat_reverb_heian, R.drawable.voicechat_ic_reverb_heian),
            new ChatSoundMix("6", R.string.voicechat_reverb_dafangjian, R.drawable.voicechat_ic_reverb_dafangjian),
            new ChatSoundMix("7", R.string.voicechat_reverb_zhongdeng, R.drawable.voicechat_ic_reverb_zhongdeng),
            new ChatSoundMix("8", R.string.voicechat_reverb_zoulang, R.drawable.voicechat_ic_reverb_zouliang),
    };

    private int selectPos = 0;

    public ChatReverbContentModel(int selectPosition) {
        this.selectPos = selectPosition;
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

    public void selectItem(int position) {
        this.selectPos = position;
        ThreadUtil.runOnSubThread(new Runnable() {
            @Override
            public void run() {
                List<CardEntity> cardEntityList = getCardDataList();

                ThreadUtil.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for(IContentObserver<CardEntity> observer : ChatReverbContentModel.this.observers) {
                            observer.onContentUpdate(cardEntityList);
                        }
                    }
                });

            }
        });
    }

    private List<CardEntity> getCardDataList() {
        List<CardEntity> cardDataList = new ArrayList<>();
        for(int i = 0; i < effectArray.length; i++) {
            CardEntity cardEntity = new CardEntity();
            cardEntity.cardType = CardTypeDef.CHAT_SOUND_MIX_CARD;
            ChatSoundMix chatSoundEffects = effectArray[i];
            chatSoundEffects.setSelected(false);
            cardEntity.bizData = chatSoundEffects;
            cardDataList.add(cardEntity);
        }
        ((ChatSoundMix)cardDataList.get(selectPos).bizData).setSelected(true);

        return cardDataList;
    }

    //获取当前选中的
    public CardEntity getSelectedItem(){
        List<CardEntity> cardList = getCardDataList();
        if(selectPos < cardList.size())
            return cardList.get(selectPos);
        return null;
    }
}
