package com.aliyun.auikits.voicechat.widget.card;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.databinding.DataBindingUtil;

import com.aliyun.auikits.voicechat.R;
import com.aliyun.auikits.voicechat.databinding.VoicechatChatMessageCardBinding;
import com.aliyun.auikits.voicechat.base.card.BaseCard;
import com.aliyun.auikits.voicechat.base.card.CardEntity;
import com.aliyun.auikits.voicechat.model.entity.ChatMessage;
import com.aliyun.auikits.voicechat.vm.ChatMessageViewModel;

public class ChatMessageCard extends BaseCard {
    private VoicechatChatMessageCardBinding binding;
    private ChatMessageViewModel vm;
    public ChatMessageCard(Context context) {
        super(context);
    }

    @Override
    public void onCreate(Context context) {
        this.vm = new ChatMessageViewModel();
        this.binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.voicechat_chat_message_card, this, true);
        this.binding.setViewModel(this.vm);
    }


    @Override
    public void onBind(CardEntity entity) {
        super.onBind(entity);
        this.vm.bind(getContext(), (ChatMessage) entity.bizData);
        this.binding.executePendingBindings();
    }
}
