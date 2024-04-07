package com.aliyun.auikits.voicechat.vm;

import android.content.Context;

import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

import com.aliyun.auikits.voicechat.R;
import com.aliyun.auikits.voicechat.model.entity.ChatMember;


public class ChatMemberEmptyViewModel extends ViewModel {

    public ObservableField<String> name = new ObservableField<String>();

    public void bind(Context context, ChatMember data) {
        this.name.set(data.getIndex() + context.getString(R.string.voicechat_chat_member_empty_suffix));
    }
}
