package com.aliyun.auikits.voicechat.vm;


import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.ViewModel;

import com.aliyun.auikits.voicechat.model.entity.ChatSoundMix;


public class ChatSoundMixViewModel extends ViewModel {

    public ObservableInt effectImage = new ObservableInt();
    public ObservableInt effectName = new ObservableInt();
    public ObservableBoolean selected = new ObservableBoolean(false);

    public void bind(ChatSoundMix effect) {
        this.effectName.set(effect.getNameRes());
        this.effectImage.set(effect.getImageRes());
        this.selected.set(effect.isSelected());
    }
}
