package com.aliyun.auikits.voicechat.widget.card;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.databinding.DataBindingUtil;

import com.aliyun.auikits.voicechat.R;
import com.aliyun.auikits.voicechat.databinding.VoicechatChatMemberListCardBinding;
import com.aliyun.auikits.voicechat.base.card.BaseCard;
import com.aliyun.auikits.voicechat.base.card.CardEntity;
import com.aliyun.auikits.voicechat.model.entity.ChatMember;
import com.aliyun.auikits.voicechat.vm.ChatMemberListViewModel;

public class ChatMemberListCard extends BaseCard {
    private VoicechatChatMemberListCardBinding binding;
    private ChatMemberListViewModel vm;
    public ChatMemberListCard(Context context) {
        super(context);

    }

    @Override
    public void onCreate(Context context) {
        this.vm = new ChatMemberListViewModel();
        this.binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.voicechat_chat_member_list_card, this, true);
        this.binding.setViewModel(vm);
    }


    @Override
    public void onBind(CardEntity entity) {
        super.onBind(entity);
        this.vm.bind((ChatMember) entity.bizData);
        this.binding.executePendingBindings();
    }
}
