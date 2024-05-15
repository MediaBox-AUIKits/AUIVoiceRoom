package com.aliyun.auikits.voicechat.widget.card;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.databinding.DataBindingUtil;

import com.aliyun.auikits.voice.ARTCVoiceRoomEngine;
import com.aliyun.auikits.voicechat.R;
import com.aliyun.auikits.voicechat.databinding.VoicechatSoundEffectCardBinding;
import com.aliyun.auikits.voicechat.base.card.BaseCard;
import com.aliyun.auikits.voicechat.base.card.CardEntity;
import com.aliyun.auikits.voicechat.model.entity.ChatMusicItem;
import com.aliyun.auikits.voicechat.vm.ChatSoundEffectViewModel;

public class ChatSoundEffectCard extends BaseCard {
    private VoicechatSoundEffectCardBinding binding;
    private ChatSoundEffectViewModel vm;
    public ChatSoundEffectCard(Context context) {
        super(context);

    }

    @Override
    public void onCreate(Context context) {
        this.vm = new ChatSoundEffectViewModel();
        this.binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.voicechat_sound_effect_card, this, true);
        this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        this.binding.setViewModel(vm);
    }

    @Override
    public void bindEngine(ARTCVoiceRoomEngine engine) {
        super.bindEngine(engine);
        if(vm != null){
            vm.bindEngine(engine);
        }
    }

    @Override
    public void onBind(CardEntity entity) {
        super.onBind(entity);
        this.vm.bind((ChatMusicItem) entity.bizData);
        this.binding.executePendingBindings();
    }
}
