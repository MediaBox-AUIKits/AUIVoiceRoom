package com.aliyun.auikits.voicechat.vm;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aliyun.auikits.voicechat.R;
import com.aliyun.auikits.voicechat.adapter.ChatItemDecoration;
import com.aliyun.auikits.voicechat.base.card.CardListAdapter;
import com.aliyun.auikits.voicechat.base.feed.ContentViewModel;
import com.aliyun.auikits.voicechat.databinding.VoicechatDialogChatMemberListBinding;
import com.aliyun.auikits.voicechat.model.content.ChatMemberListContentModel;
import com.aliyun.auikits.voicechat.model.entity.ChatMember;
import com.aliyun.auikits.voicechat.model.entity.ChatRoom;
import com.aliyun.auikits.voicechat.model.entity.ChatRoomCallback;
import com.aliyun.auikits.voicechat.util.DisplayUtil;
import com.aliyun.auikits.voicechat.util.ToastHelper;
import com.aliyun.auikits.voicechat.widget.card.CardTypeDef;
import com.aliyun.auikits.voicechat.widget.card.ChatMemberListCard;
import com.aliyun.auikits.voicechat.widget.card.DefaultCardViewFactory;
import com.aliyun.auikits.voicechat.widget.helper.DialogHelper;
import com.aliyun.auikits.voicechat.widget.list.CustomViewHolder;
import com.aliyun.auikits.voice.ARTCVoiceRoomEngine;
import com.aliyun.auikits.voice.ARTCVoiceRoomEngineDelegate;
import com.aliyun.auikits.voiceroom.bean.NetworkState;
import com.aliyun.auikits.voiceroom.bean.UserInfo;
import com.orhanobut.dialogplus.DialogPlus;


public class ChatHeaderViewModel extends ViewModel {

    public ObservableField<String> id = new ObservableField<String>();
    public ObservableField<String> title = new ObservableField<String>();
    public ObservableInt networkStatusIconRes = new ObservableInt();
    public ObservableInt networkStatusTextRes = new ObservableInt();
    public ObservableField<String> memberNum = new ObservableField<String>();
    public ObservableField<String> compereAvatar = new ObservableField<String>();
    //当前用户是否主持人
    private boolean isCompere = false;
    private ARTCVoiceRoomEngineDelegate roomCallback;
    private ARTCVoiceRoomEngine roomController;
    private ChatRoom chatRoom;

    public void bind(ChatRoom aChatRoom, ARTCVoiceRoomEngine roomController) {
        this.roomController = roomController;
        this.chatRoom = aChatRoom;
        this.id.set(chatRoom.getId());
        this.title.set(chatRoom.getTitle());

        updateRoomMember();

        ChatMember compere = chatRoom.getCompere();
        if(compere != null) {
            updateNetworkStatus(compere.getNetworkStatus());
            this.compereAvatar.set(compere.getAvatar());
        }
        isCompere = chatRoom.isCompere();
        this.roomCallback = new ChatRoomCallback() {

            @Override
            public void onLeavedRoom(UserInfo userInfo) {
                chatRoom.setMemberNum(chatRoom.getMemberNum() - 1);
                updateRoomMember();
            }

            @Override
            public void onMemberCountChanged(int count) {
                super.onMemberCountChanged(count);
                chatRoom.setMemberNum(count);
                updateRoomMember();
            }

            @Override
            public void onNetworkStateChanged(UserInfo user) {
                //如果收到自己的网络变化通知，则更新状态
                if(user.userId.equals(ChatHeaderViewModel.this.chatRoom.getSelf().getId())) {
                    updateNetworkStatus(user.networkState);
                }
            }
        };
        this.roomController.addObserver(this.roomCallback);
        chatRoom.setMemberNum(roomController.getMemberCount());
        updateRoomMember();
    }

    public void unBind() {
        this.roomController.removeObserver(this.roomCallback);
    }

    public void updateNetworkStatus(NetworkState networkStatus) {
        if(networkStatus == NetworkState.WEAK) {
            this.networkStatusIconRes.set(R.drawable.voicechat_ic_signal_strenth_style1_02);
            this.networkStatusTextRes.set(R.string.voicechat_chat_network_status_weak);
        } else if(networkStatus == NetworkState.DISCONNECT) {
            this.networkStatusIconRes.set(R.drawable.voicechat_ic_signal_strenth_style1_03);
            this.networkStatusTextRes.set(R.string.voicechat_chat_network_status_disconnected);
        } else {
            this.networkStatusIconRes.set(R.drawable.voicechat_ic_signal_strenth_style1_01);
            this.networkStatusTextRes.set(R.string.voicechat_chat_network_status_strong);
        }
    }

    public void onRoomIdCopyClick(View view) {
        Context context = view.getContext();
        // 获取剪贴板管理器服务
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        // 创建一个ClipData对象
        String textToCopy = id.get();
        ClipData clip = ClipData.newPlainText("房间号", textToCopy); // 'label'是对剪贴板内容的描述，可以是任何文本

        // 复制ClipData到剪贴板
        clipboard.setPrimaryClip(clip);

        // 可选：显示用户反馈
        ToastHelper.showToast(context, R.string.voicechat_chat_room_id_copied, Toast.LENGTH_SHORT);

    }

    private void updateRoomMember() {
        int tempMemberNum = this.chatRoom.getMemberNum();
        if(tempMemberNum > 999) {
            this.memberNum.set("999+");
        } else {
            this.memberNum.set(String.valueOf(tempMemberNum));
        }
    }

    public void onMemberListClick(View view) {
    }

    public void onRoomCloseClick(View view) {
        DialogHelper.showCloseDialog(view.getContext() , this.roomController, this.isCompere);
    }
}
