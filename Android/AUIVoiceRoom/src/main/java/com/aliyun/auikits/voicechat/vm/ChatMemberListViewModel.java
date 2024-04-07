package com.aliyun.auikits.voicechat.vm;



import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.ViewModel;

import com.aliyun.auikits.voicechat.R;
import com.aliyun.auikits.voicechat.model.entity.ChatMember;
import com.aliyun.auikits.voiceroom.bean.NetworkState;


public class ChatMemberListViewModel extends ViewModel {

    public ObservableField<String> id = new ObservableField<String>();
    public ObservableField<String> name = new ObservableField<String>();
    public ObservableField<String> avatar = new ObservableField<String>();
    public ObservableInt networkStatusIconRes = new ObservableInt();
    public ObservableInt identifyFlag = new ObservableInt();
    public ObservableInt identifyFlagTextRes = new ObservableInt();
    public ObservableInt identifyFlagBgRes = new ObservableInt();

    public void bind(ChatMember data) {
        this.id.set(data.getId());
        this.name.set(data.getName());
        this.avatar.set(data.getAvatar());

        updateIdentifyFlag(data.getIdentifyFlag());
        updateNetworkStatus(data.getNetworkStatus());
    }

    private void updateIdentifyFlag(int identifyFlag) {
        this.identifyFlag.set(identifyFlag);
        if(identifyFlag == ChatMember.IDENTIFY_FLAG_COMPERE) {
            identifyFlagTextRes.set(R.string.voicechat_chat_member_list_compere_flag);
            identifyFlagBgRes.set(R.drawable.voicechat_chat_member_list_compere_flag_bg);
        } else if(identifyFlag == ChatMember.IDENTIFY_FLAG_CHAT) {
            identifyFlagTextRes.set(R.string.voicechat_chat_member_list_chat_flag);
            identifyFlagBgRes.set(R.drawable.voicechat_chat_member_list_other_flag_bg);
        } else {
            identifyFlagTextRes.set(R.string.voicechat_chat_member_list_audience_flag);
            identifyFlagBgRes.set(R.drawable.voicechat_chat_member_list_other_flag_bg);
        }
    }

    private void updateNetworkStatus(NetworkState networkStatus) {
        if(networkStatus == NetworkState.DISCONNECT) {
            this.networkStatusIconRes.set(R.drawable.voicechat_ic_signal_strenth_style2_03);
        } else if(networkStatus == NetworkState.WEAK) {
            this.networkStatusIconRes.set(R.drawable.voicechat_ic_signal_strenth_style2_02);
        } else {
            this.networkStatusIconRes.set(R.drawable.voicechat_ic_signal_strenth_style2_01);
        }

    }

}
