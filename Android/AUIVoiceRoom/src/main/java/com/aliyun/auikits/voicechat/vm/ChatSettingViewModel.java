package com.aliyun.auikits.voicechat.vm;

import android.widget.CompoundButton;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.ViewModel;

import com.aliyun.auikits.voice.ARTCVoiceRoomEngine;


public class ChatSettingViewModel extends ViewModel {

    public ObservableBoolean earbackSwitch = new ObservableBoolean(false);
    private ARTCVoiceRoomEngine mEngine;

    public void bind(ARTCVoiceRoomEngine engine) {
        this.mEngine = engine;
    }



    public void onEarbackSwitchChange(CompoundButton btn, boolean checked) {
        this.earbackSwitch.set(!this.earbackSwitch.get());
        if(mEngine != null){
            mEngine.enableEarBack(checked);
        }
    }
}
