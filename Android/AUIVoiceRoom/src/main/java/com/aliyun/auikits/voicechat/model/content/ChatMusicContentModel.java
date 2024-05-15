package com.aliyun.auikits.voicechat.model.content;


import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.alivc.auicommon.common.base.util.ThreadUtil;
import com.aliyun.auikits.common.util.CommonUtil;
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
 * 背景音乐内容数据
 */
public class ChatMusicContentModel extends AbsContentModel<CardEntity> {
    private List<ChatMusicItem> musicArray = new ArrayList<>();
    private Context mContext;
    private static String FOLDER_NAME = "test_audios2/background_music";
    private String mPlayingUrl = null;
    private boolean mApplying = false;

    public ChatMusicContentModel(Context context) {
        this.mContext = context;
    }

    @Override
    public void initData(BizParameter parameter, IBizCallback<CardEntity> callback) {
        ThreadUtil.runOnSubThread(new Runnable() {
            @Override
            public void run() {
                musicArray.clear();
                musicArray.add(new ChatMusicItem("1", "家庭蒙太奇", "家庭蒙太奇",
                        new File(mContext.getExternalFilesDir(null), String.format("%s/家庭蒙太奇.mp3", FOLDER_NAME)).getAbsolutePath()));
                musicArray.add(new ChatMusicItem("2", "刚刚呼吸", "刚刚呼吸",
                        new File(mContext.getExternalFilesDir(null), String.format("%s/刚刚呼吸.mp3", FOLDER_NAME)).getAbsolutePath()));
                musicArray.add(new ChatMusicItem("2", "Seagull", "Seagull",
                        new File(mContext.getExternalFilesDir(null), String.format("%s/Seagull.mp3", FOLDER_NAME)).getAbsolutePath()));
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

    public void notifyContentUpdate(){
        List<CardEntity> cardEntityList = getCardDataList();
        ThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(IContentObserver<CardEntity> observer : ChatMusicContentModel.this.observers) {
                    observer.onContentUpdate(cardEntityList);
                }
            }
        });
    }

    private List<CardEntity> getCardDataList() {
        List<CardEntity> cardDataList = new ArrayList<>();
        for(int i = 0; i < musicArray.size(); i++) {
            CardEntity cardEntity = new CardEntity();
            cardEntity.cardType = CardTypeDef.CHAT_MUSIC_CARD;
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
        mPlayingUrl = url;
    }

    //更新应用状态
    public void onUpdateApplying(boolean applying){
        mApplying = applying;
    }

    //更新播放状态
    public void onUpdatePlayState(boolean playing){
        List<CardEntity> cardList = getCardDataList();
        for(CardEntity entity : cardList){
            ChatMusicItem musicItem = (ChatMusicItem) entity.bizData;
            if(TextUtils.equals(musicItem.getUrl(), mPlayingUrl)){
                if(mApplying){
                    musicItem.setPlaying(false);
                    musicItem.setApplying(playing);
                }else{
                    musicItem.setApplying(false);
                    musicItem.setPlaying(playing);
                }
            }else{
                musicItem.setPlaying(false);
                musicItem.setApplying(false);
            }
        }
    }
}
