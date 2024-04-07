package com.aliyun.auikits.voicechat.widget.card;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.databinding.DataBindingUtil;

import com.aliyun.auikits.voicechat.R;
import com.aliyun.auikits.voicechat.databinding.VoicechatChatMicMemberCardBinding;
import com.aliyun.auikits.voicechat.base.card.BaseCard;
import com.aliyun.auikits.voicechat.base.card.CardEntity;
import com.aliyun.auikits.voicechat.model.entity.ChatMember;
import com.aliyun.auikits.voicechat.vm.ChatMicMemberViewModel;

public class ChatMicMemberCard extends BaseCard {
    private VoicechatChatMicMemberCardBinding binding;
    private ChatMicMemberViewModel vm;
    public ChatMicMemberCard(Context context) {
        super(context);

    }

    @Override
    public void onCreate(Context context) {
        this.vm = new ChatMicMemberViewModel();
        this.binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.voicechat_chat_mic_member_card, this, true);
        this.binding.setViewModel(vm);
    }


    @Override
    public void onBind(CardEntity entity) {
        super.onBind(entity);
        this.vm.bind((ChatMember) entity.bizData);
        this.binding.executePendingBindings();
    }
}
