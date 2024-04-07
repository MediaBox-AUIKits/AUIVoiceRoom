package com.aliyun.auikits.voicechat.widget.card;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.databinding.DataBindingUtil;

import com.aliyun.auikits.voicechat.R;
import com.aliyun.auikits.voicechat.databinding.VoicechatChatMemberEmptyCardBinding;
import com.aliyun.auikits.voicechat.base.card.BaseCard;
import com.aliyun.auikits.voicechat.base.card.CardEntity;
import com.aliyun.auikits.voicechat.model.entity.ChatMember;
import com.aliyun.auikits.voicechat.vm.ChatMemberEmptyViewModel;

public class ChatMemberEmptyCard extends BaseCard {
    private VoicechatChatMemberEmptyCardBinding binding;
    private ChatMemberEmptyViewModel vm;
    public ChatMemberEmptyCard(Context context) {
        super(context);

    }

    @Override
    public void onCreate(Context context) {
        this.vm = new ChatMemberEmptyViewModel();
        this.binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.voicechat_chat_member_empty_card, this, true);
        this.binding.setViewModel(vm);
    }


    @Override
    public void onBind(CardEntity entity) {
        super.onBind(entity);
        this.vm.bind(this.getContext(), (ChatMember) entity.bizData);
        this.binding.executePendingBindings();
    }
}
