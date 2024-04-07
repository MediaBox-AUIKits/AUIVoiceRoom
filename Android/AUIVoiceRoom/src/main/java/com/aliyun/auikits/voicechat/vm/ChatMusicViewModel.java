package com.aliyun.auikits.voicechat.vm;

import android.widget.SeekBar;

import androidx.databinding.ObservableInt;
import androidx.lifecycle.ViewModel;

public class ChatMusicViewModel extends ViewModel {

    public ObservableInt humanVolume = new ObservableInt(50);
    public ObservableInt musicVolume = new ObservableInt(50);


    public void bind() {
        //TODO SDK 绑定当前音量
    }


    public void onHumanVolumeChanged(SeekBar seekBar, int progress, boolean fromUser) {
        humanVolume.set(progress);

        //TODO SDK 实时变更音量
    }

    public void onMusicVolumeChanged(SeekBar seekBar, int progress, boolean fromUser) {
        musicVolume.set(progress);
        //TODO SDK 实时变更音量
    }
}
