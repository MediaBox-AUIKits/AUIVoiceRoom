package com.aliyun.auikits.voicechat.vm;

import android.widget.CompoundButton;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.ViewModel;


public class ChatSettingViewModel extends ViewModel {

    public ObservableBoolean earbackSwitch = new ObservableBoolean(false);


    public void bind() {
        //TODO SDK 绑定当前音量
    }



    public void onEarbackSwitchChange(CompoundButton btn, boolean checked) {
        //TODO SDK 对接耳返控制器
        this.earbackSwitch.set(!this.earbackSwitch.get());
    }
}
