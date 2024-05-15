package com.aliyun.auikits.voicechat.vm;

import android.view.View;
import android.widget.SeekBar;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.ViewModel;

import com.aliyun.auikits.common.util.CommonUtil;
import com.aliyun.auikits.voice.ARTCVoiceRoomEngine;
import com.aliyun.auikits.voicechat.model.entity.ChatMusicItem;

public class ChatSoundEffectViewModel extends ViewModel {

    public ObservableField<String> effectName = new ObservableField<String>();
    public ObservableInt effectVolume = new ObservableInt(50);
    public ObservableBoolean isPlaying = new ObservableBoolean(false);
    private ARTCVoiceRoomEngine mEngine;
    private int mSoundId = -1;
    private ChatMusicItem mMusicItem;

    public void bind(ChatMusicItem musicItem) {
        this.mMusicItem = musicItem;
        this.effectName.set(musicItem.getTitle());
        this.isPlaying.set(musicItem.isPlaying());
        this.mMusicItem.setVolume(effectVolume.get());
    }

    public void bindEngine(ARTCVoiceRoomEngine engine){
        this.mEngine = engine;
    }

    public void onEffectVolumeChanged(SeekBar seekBar, int progress, boolean fromUser) {
        effectVolume.set(progress);
        if(mMusicItem != null){
            mSoundId = mMusicItem.getSoundId();
            mMusicItem.setVolume(effectVolume.get());
        }else{
            mSoundId = -1;
        }
        if(mSoundId >= 0 && mEngine != null){
            mEngine.setAudioEffectVolume(mSoundId, progress);
        }
    }
}
