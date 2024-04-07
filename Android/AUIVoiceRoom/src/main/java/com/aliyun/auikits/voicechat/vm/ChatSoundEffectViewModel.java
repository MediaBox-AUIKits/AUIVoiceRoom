package com.aliyun.auikits.voicechat.vm;

import android.view.View;
import android.widget.SeekBar;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.ViewModel;

import com.aliyun.auikits.voicechat.model.entity.ChatMusicItem;

public class ChatSoundEffectViewModel extends ViewModel {

    public ObservableField<String> effectName = new ObservableField<String>();
    public ObservableInt effectVolume = new ObservableInt(50);
    public ObservableBoolean isPlaying = new ObservableBoolean(false);

    public void bind(ChatMusicItem musicItem) {
        //TODO SDK 绑定当前音量
        this.effectName.set(musicItem.getTitle());
        this.isPlaying.set(musicItem.isPlaying());
    }


    public void onEffectVolumeChanged(SeekBar seekBar, int progress, boolean fromUser) {
        effectVolume.set(progress);

        //TODO SDK 实时变更音量
    }


    public void onEffectPlayOrStop(View view) {
        //TODO APP 对接播放逻辑
        boolean tmpLaughterPlaying =!isPlaying.get();

        isPlaying.set(tmpLaughterPlaying);
    }


    public void onApplySoundEffect(View view) {
        //TODO APP 对接使用逻辑
//        if(view.getId() == R.id.btn_apply_laughter) {
//
//        } else if(view.getId() == R.id.btn_apply_applause) {
//
//        }
    }
}
