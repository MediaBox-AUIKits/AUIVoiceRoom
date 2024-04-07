package com.aliyun.auikits.voicechat.vm;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.ViewModel;

import com.aliyun.auikits.voicechat.R;
import com.aliyun.auikits.voicechat.model.entity.ChatMember;
import com.aliyun.auikits.voicechat.model.entity.ChatRoom;
import com.aliyun.auikits.voicechat.model.entity.ChatRoomCallback;
import com.aliyun.auikits.voicechat.util.DisplayUtil;
import com.aliyun.auikits.voicechat.util.ToastHelper;
import com.aliyun.auikits.voicechat.widget.helper.DialogHelper;
import com.aliyun.auikits.voiceroom.AUIVoiceRoom;
import com.aliyun.auikits.voiceroom.AUIVoiceRoomCallback;
import com.aliyun.auikits.voiceroom.bean.NetworkState;
import com.aliyun.auikits.voiceroom.bean.UserInfo;


public class ChatHeaderViewModel extends ViewModel {

    public ObservableField<String> id = new ObservableField<String>();
    public ObservableField<String> title = new ObservableField<String>();
    public ObservableInt networkStatusIconRes = new ObservableInt();
    public ObservableInt networkStatusTextRes = new ObservableInt();
    public ObservableField<String> memberNum = new ObservableField<String>();
    public ObservableField<String> compereAvatar = new ObservableField<String>();
    //当前用户是否主持人
    private boolean isCompere = false;
    private AUIVoiceRoomCallback roomCallback;
    private AUIVoiceRoom roomController;
    private ChatRoom chatRoom;

    public void bind(ChatRoom aChatRoom, AUIVoiceRoom roomController) {
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
            public void onUserOnline(UserInfo userInfo) {
                chatRoom.setMemberNum(chatRoom.getMemberNum() + 1);
                updateRoomMember();
            }

            @Override
            public void onUserOffline(UserInfo userInfo) {
                chatRoom.setMemberNum(chatRoom.getMemberNum() - 1);
                updateRoomMember();
            }

            @Override
            public void onUserNetworkState(UserInfo user) {
                //如果收到自己的网络变化通知，则更新状态
                if(user.userId.equals(ChatHeaderViewModel.this.chatRoom.getSelf().getId())) {
                    updateNetworkStatus(user.networkState);
                }
            }
        };
        this.roomController.addRoomCallback(this.roomCallback);

    }

    public void unBind() {
        this.roomController.removeRoomCallback(this.roomCallback);
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
        //暂时屏蔽
        return;

//        Context context = view.getContext();
//
//        VoicechatDialogChatMemberListBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.voicechat_dialog_chat_member_list, null, false);
//        binding.setLifecycleOwner((LifecycleOwner) context);
//        CustomViewHolder viewHolder = new CustomViewHolder(binding.getRoot());
//        DefaultCardViewFactory factory = new DefaultCardViewFactory();
//        factory.registerCardView(CardTypeDef.CHAT_MEMBER_CARD, ChatMemberListCard.class);
//        CardListAdapter cardListAdapter = new CardListAdapter(factory);
//
//        binding.rvChatMemberList.setLayoutManager(new LinearLayoutManager(context));
//        binding.rvChatMemberList.addItemDecoration(new ChatItemDecoration(0, 0));
//        binding.rvChatMemberList.setAdapter(cardListAdapter);
//
//        ContentViewModel contentViewModel = new ContentViewModel.Builder()
//                .setContentModel(new ChatMemberListContentModel())
//                .setLoadMoreEnable(true)
//                .setLoadingView(R.layout.voicechat_loading_view)
//                .setErrorView(R.layout.voicechat_layout_error_view, R.id.btn_retry)
//                .build();
//        contentViewModel.bindView(cardListAdapter);
//
//
//        DialogPlus dialog = DialogPlus.newDialog(context)
//                .setContentHolder(viewHolder)
//                .setGravity(Gravity.BOTTOM)
//                .setExpanded(false)
//                .setPadding(0, 0 ,0 , DisplayUtil.getNavigationBarHeight(context))
//                .setOverlayBackgroundResource(android.R.color.transparent)
//                .create();
//        dialog.show();
    }

    public void onRoomCloseClick(View view) {
        DialogHelper.showCloseDialog(view.getContext() , this.roomController, this.isCompere);
    }
}
