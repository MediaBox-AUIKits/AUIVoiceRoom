package com.aliyun.auikits.voicechat.vm;



import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.ViewModel;

import com.aliyun.auikits.voicechat.R;
import com.aliyun.auikits.voicechat.model.entity.ChatMember;
import com.aliyun.auikits.voiceroom.bean.NetworkState;


public class ChatMicMemberViewModel extends ViewModel {

    public ObservableField<String> id = new ObservableField<String>();
    public ObservableField<String> name = new ObservableField<String>();
    public ObservableField<String> avatar = new ObservableField<String>();
    public ObservableInt networkStatusIconRes = new ObservableInt();
    public ObservableInt microphoneStatus = new ObservableInt();
    public ObservableInt identifyFlag = new ObservableInt();
    public ObservableInt identifyFlagTextRes = new ObservableInt();
    public ObservableInt identifyFlagBgRes = new ObservableInt();
    public ObservableBoolean isTalking = new ObservableBoolean(false);

    public void bind(ChatMember data) {
        this.id.set(data.getId());
        this.name.set(data.getName());
        this.avatar.set(data.getAvatar());

        this.microphoneStatus.set(data.getMicrophoneStatus());
        updateIdentifyFlag(data.getIdentifyFlag());
        updateNetworkStatus(data.getNetworkStatus());
        if(data.getMicrophoneStatus() == ChatMember.MICROPHONE_STATUS_OFF) {
            this.isTalking.set(false);
        } else {
            this.isTalking.set(data.isSpeaking());
        }
    }

    private void updateIdentifyFlag(int identifyFlag) {
        this.identifyFlag.set(identifyFlag);
        if(identifyFlag == ChatMember.IDENTIFY_FLAG_COMPERE) {
            identifyFlagTextRes.set(R.string.voicechat_chat_compere_flag);
            identifyFlagBgRes.set(R.drawable.voicechat_chat_compere_flag_bg);
        } else if(identifyFlag == ChatMember.IDENTIFY_FLAG_SELF) {
            identifyFlagTextRes.set(R.string.voicechat_chat_self_flag);
            identifyFlagBgRes.set(R.drawable.voicechat_chat_self_flag_bg);
        } else {
            identifyFlagTextRes.set(R.string.voicechat_empty);
            identifyFlagBgRes.set(0);
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
