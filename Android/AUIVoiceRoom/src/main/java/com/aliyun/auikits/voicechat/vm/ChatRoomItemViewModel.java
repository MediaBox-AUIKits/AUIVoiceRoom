package com.aliyun.auikits.voicechat.vm;

import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

import com.aliyun.auikits.voicechat.model.entity.ChatRoomItem;

import java.util.List;


public class ChatRoomItemViewModel extends ViewModel {
    public ObservableField<String> id = new ObservableField<String>();
    public ObservableField<String> title = new ObservableField<String>();

    public ObservableField<String> avatar1 = new ObservableField<String>();
    public ObservableField<String> avatar2 = new ObservableField<String>();
    public ObservableField<String> avatar3 = new ObservableField<String>();

    public ObservableField<String> memberNum = new ObservableField<String>();


    public void bind(ChatRoomItem data) {
        this.id.set(data.getRoomId());
        this.title.set(data.getTitle());
        List<String> avatarList = data.getAvatarList();
        if(avatarList != null) {
            int avatarSize = avatarList.size();
            if(avatarSize >= 1) {
                avatar1.set(avatarList.get(0));
            }

            if(avatarSize >= 2) {
                avatar2.set(avatarList.get(1));
            }

            if(avatarSize >= 3) {
                avatar3.set(avatarList.get(2));
            }
        }

        int memberNum = data.getMemberNum();
        if(memberNum > 999) {
            this.memberNum.set("999+");
        } else {
            this.memberNum.set(String.valueOf(memberNum));
        }
    }
}
