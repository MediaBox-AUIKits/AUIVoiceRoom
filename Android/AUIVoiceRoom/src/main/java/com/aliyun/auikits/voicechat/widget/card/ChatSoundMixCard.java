package com.aliyun.auikits.voicechat.widget.card;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.databinding.DataBindingUtil;

import com.aliyun.auikits.voicechat.R;
import com.aliyun.auikits.voicechat.databinding.VoicechatSoundMixCardBinding;
import com.aliyun.auikits.voicechat.base.card.BaseCard;
import com.aliyun.auikits.voicechat.base.card.CardEntity;
import com.aliyun.auikits.voicechat.model.entity.ChatSoundMix;
import com.aliyun.auikits.voicechat.vm.ChatSoundMixViewModel;

public class ChatSoundMixCard extends BaseCard {
    private VoicechatSoundMixCardBinding binding;
    private ChatSoundMixViewModel vm;
    public ChatSoundMixCard(Context context) {
        super(context);

    }

    @Override
    public void onCreate(Context context) {
        this.vm = new ChatSoundMixViewModel();
        this.binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.voicechat_sound_mix_card, this, true);
        this.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        this.binding.setViewModel(vm);
    }


    @Override
    public void onBind(CardEntity entity) {
        super.onBind(entity);
        this.vm.bind((ChatSoundMix) entity.bizData);
        this.binding.executePendingBindings();
    }
}
