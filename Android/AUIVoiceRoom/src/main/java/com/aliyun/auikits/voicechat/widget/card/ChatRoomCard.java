package com.aliyun.auikits.voicechat.widget.card;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.databinding.DataBindingUtil;

import com.aliyun.auikits.voicechat.R;
import com.aliyun.auikits.voicechat.databinding.VoicechatListRoomItemBinding;
import com.aliyun.auikits.voicechat.base.card.BaseCard;
import com.aliyun.auikits.voicechat.base.card.CardEntity;
import com.aliyun.auikits.voicechat.model.entity.ChatRoomItem;
import com.aliyun.auikits.voicechat.vm.ChatRoomItemViewModel;

public class ChatRoomCard extends BaseCard {
    private VoicechatListRoomItemBinding binding;
    private ChatRoomItemViewModel vm;

    public ChatRoomCard(Context context) {
        super(context);
    }

    @Override
    public void onCreate(Context context) {
        this.binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.voicechat_list_room_item, this, true);
        this.vm = new ChatRoomItemViewModel();
        this.binding.setViewModel(vm);
    }

    @Override
    public void onBind(CardEntity cardEntity) {
        this.vm.bind((ChatRoomItem) cardEntity.bizData);
        this.binding.executePendingBindings();
    }

    @Override
    public void onUnBind() {

    }
}
