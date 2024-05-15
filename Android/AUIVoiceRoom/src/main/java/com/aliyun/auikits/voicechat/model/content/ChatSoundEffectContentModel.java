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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 音效内容数据
 */
public class ChatSoundEffectContentModel extends AbsContentModel<CardEntity> {


    private List<ChatMusicItem> musicArray = new ArrayList<>();

    private Context mContext;
    private static String FOLDER_NAME = "test_audios2/audio_effect";
    private String mPlayUrl = null;
    private boolean mApplying = false;

    public ChatSoundEffectContentModel(Context context) {
        mContext = context;
    }

    @Override
    public void initData(BizParameter parameter, IBizCallback<CardEntity> callback) {
        ThreadUtil.runOnSubThread(new Runnable() {
            @Override
            public void run() {
                musicArray.clear();
                musicArray.add(new ChatMusicItem("1", mContext.getString(R.string.voicechat_open), "",
                        new File(mContext.getExternalFilesDir(null), String.format("%s/开场.aac", FOLDER_NAME)).getAbsolutePath()));
                musicArray.add(new ChatMusicItem("2", mContext.getString(R.string.voicechat_applause), "",
                        new File(mContext.getExternalFilesDir(null), String.format("%s/鼓掌.aac", FOLDER_NAME)).getAbsolutePath()));
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

    public void notifyContentUpdate() {
        List<CardEntity> cardEntityList = getCardDataList();
        ThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(IContentObserver<CardEntity> observer : ChatSoundEffectContentModel.this.observers) {
                    observer.onContentUpdate(cardEntityList);
                }
            }
        });
    }

    private List<CardEntity> getCardDataList() {
        List<CardEntity> cardDataList = new ArrayList<>();
        for(int i = 0; i < musicArray.size(); i++) {
            CardEntity cardEntity = new CardEntity();
            cardEntity.cardType = CardTypeDef.CHAT_SOUND_EFFECT_CARD;
            ChatMusicItem chatMusicItem = musicArray.get(i);
            cardEntity.bizData = chatMusicItem;
            cardDataList.add(cardEntity);
        }

        return cardDataList;
    }

    public CardEntity getTargetData(int pos){
        List<CardEntity> cardList = getCardDataList();
        if(pos < cardList.size())
            return cardList.get(pos);
        return null;
    }

    //更新播放源
    public void onUpdatePlaySource(String url){
        mPlayUrl = url;
    }

    //更新applying状态
    public void onUpdateApplyState(boolean apply){
        mApplying = apply;
    }

    //更新播放状态
    public void onUpdatePlayState(boolean play){

    }
}
