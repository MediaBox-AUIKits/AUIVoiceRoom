package com.aliyun.auikits.voicechat.vm;

import android.widget.SeekBar;

import androidx.databinding.ObservableInt;
import androidx.lifecycle.ViewModel;

import com.aliyun.auikits.voice.ARTCVoiceRoomEngine;

public class ChatMusicViewModel extends ViewModel {

    public ObservableInt humanVolume = new ObservableInt(50);
    public ObservableInt musicVolume = new ObservableInt(50);
    private ARTCVoiceRoomEngine mEngine;

    public void bind(ARTCVoiceRoomEngine engine) {
        this.mEngine = engine;
    }

    public void onHumanVolumeChanged(SeekBar seekBar, int progress, boolean fromUser) {
        humanVolume.set(progress);
        if(mEngine != null){
            mEngine.setRecordingVolume(humanVolume.get());
        }
    }

    public void onMusicVolumeChanged(SeekBar seekBar, int progress, boolean fromUser) {
        musicVolume.set(progress);
        if(mEngine != null){
            mEngine.setAccompanyVolume(musicVolume.get());
        }
    }

    public int getHumanVolumeInt(){
        return humanVolume.get();
    }

    public int getMusicVolumeInt(){
        return musicVolume.get();
    }
}
