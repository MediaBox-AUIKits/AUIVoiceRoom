package com.aliyun.auikits.voicechat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.aliyun.auikits.voicechat.databinding.VoicechatActivityChatBinding;
import com.aliyun.auikits.voicechat.databinding.VoicechatDialogReconnectBinding;
import com.aliyun.auikits.voicechat.adapter.ChatItemDecoration;
import com.aliyun.auikits.voicechat.base.card.CardListAdapter;
import com.aliyun.auikits.voicechat.base.feed.ContentViewModel;
import com.aliyun.auikits.voicechat.model.entity.ChatMember;
import com.aliyun.auikits.voicechat.model.entity.ChatRoom;
import com.aliyun.auikits.voicechat.model.entity.ChatRoomCallback;
import com.aliyun.auikits.voicechat.model.entity.ChatRoomItem;
import com.aliyun.auikits.voicechat.model.content.ChatMicMemberContentModel;
import com.aliyun.auikits.voicechat.model.content.ChatMsgContentModel;
import com.aliyun.auikits.voicechat.service.ChatRoomManager;
import com.aliyun.auikits.voicechat.util.DisplayUtil;
import com.aliyun.auikits.voicechat.util.ToastHelper;
import com.aliyun.auikits.voicechat.vm.ChatViewModel;
import com.aliyun.auikits.voicechat.widget.card.CardTypeDef;
import com.aliyun.auikits.voicechat.widget.card.ChatMicMemberCard;
import com.aliyun.auikits.voicechat.widget.card.ChatMemberEmptyCard;
import com.aliyun.auikits.voicechat.widget.card.ChatMessageCard;
import com.aliyun.auikits.voicechat.widget.card.DefaultCardViewFactory;
import com.aliyun.auikits.voicechat.widget.helper.DialogHelper;
import com.aliyun.auikits.voicechat.widget.list.CustomViewHolder;
import com.aliyun.auikits.voice.ARTCVoiceRoomEngine;
import com.aliyun.auikits.voice.ARTCVoiceRoomEngineDelegate;
import com.jaeger.library.StatusBarUtil;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;

@Route(path = "/voicechat/ChatActivity")
public class ChatActivity extends AppCompatActivity {
    //房间信息
    public static final String KEY_CHAT_ROOM_ENTITY = "CHAT_ROOM_ENTITY";
    //当前个人信息
    public static final String KEY_CHAT_SELF_ENTITY = "CHAT_SELF_ENTITY";
    public static final String KEY_AUTHORIZATION = "AUTHORIZATION";
    public static final String KEY_ROOM_DISMISS = "ROOM_DISMISS";
    public static final String KEY_ROOM_ID = "ROOM_ID";
    private VoicechatActivityChatBinding binding;
    private ChatViewModel chatViewModel;

    private CardListAdapter chatMicMemberListAdapter;
    private ContentViewModel chatMicMemberViewModel;

    private CardListAdapter chatMsgListAdapter;
    private ContentViewModel chatMsgViewModel;
    private ChatMsgContentModel chatMsgContentModel;
    private ChatRoom chatRoom;
    private ARTCVoiceRoomEngine roomController;
    private ARTCVoiceRoomEngineDelegate roomCallback;
    private String authorization;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        StatusBarUtil.setTransparent(this);
        binding = DataBindingUtil.setContentView(this, R.layout.voicechat_activity_chat);
        binding.setLifecycleOwner(this);

        ChatRoomItem chatRoomItem = (ChatRoomItem) getIntent().getSerializableExtra(KEY_CHAT_ROOM_ENTITY);
        ChatMember chatSelfEntity = (ChatMember) getIntent().getSerializableExtra(KEY_CHAT_SELF_ENTITY);
        authorization = getIntent().getStringExtra(KEY_AUTHORIZATION);
        if(chatRoomItem == null || chatSelfEntity == null || authorization == null) {
            Toast.makeText(ChatActivity.this, R.string.voicechat_invalid_param, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        roomController = ChatRoomManager.getInstance().getVoiceRoom(chatRoomItem.getId());
        if(roomController == null) {
            Toast.makeText(ChatActivity.this, R.string.voicechat_invalid_param, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        roomCallback = new ChatRoomCallback() {

            @Override
            public void onDismissRoom(String commander) {
                //非主持人收到房间解散信息
                if(!roomController.isAnchor()) {
                    ToastHelper.showToast(ChatActivity.this, R.string.voicechat_room_dismiss, Toast.LENGTH_SHORT);
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(KEY_ROOM_DISMISS, true);
                    returnIntent.putExtra(ChatActivity.KEY_ROOM_ID, roomController.getRoomInfo().roomId);
                    setResult(Activity.RESULT_OK, returnIntent);
                    ChatActivity.this.finish();
                }
            }

            @Override
            public void onExitGroup(String msg) {
                ToastHelper.showToast(ChatActivity.this, R.string.voicechat_user_kickout, Toast.LENGTH_SHORT);
                Intent returnIntent = new Intent();
                returnIntent.putExtra(ChatActivity.KEY_ROOM_ID, roomController.getRoomInfo().roomId);
                setResult(Activity.RESULT_OK, returnIntent);
                ChatActivity.this.finish();
            }
        };

        ViewModelProvider viewModelProvider = new ViewModelProvider(this);
        chatViewModel = viewModelProvider.get(ChatViewModel.class);

        chatRoom = createRoomModel(chatRoomItem, chatSelfEntity);

        chatViewModel.bind(this, chatRoom, roomController);
        binding.setViewModel(chatViewModel);

        initChatMicMemberList();
        initChatMessageList();

        this.roomController.addObserver(this.roomCallback);
    }

    private ChatRoom createRoomModel(ChatRoomItem chatRoomItem, ChatMember self) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setId(chatRoomItem.getRoomId());
        chatRoom.setTitle(chatRoomItem.getTitle());

        chatRoom.setCompere(chatRoomItem.getCompere());
        chatRoom.setSelf(self);
        //如果是主持人，默认打开麦克风状态
        if(chatRoom.isCompere()) {
            self.setMicrophoneStatus(ChatMember.MICROPHONE_STATUS_ON);
        }
        return chatRoom;
    }

    private void initChatMicMemberList() {
        DefaultCardViewFactory factory = new DefaultCardViewFactory();
        factory.registerCardView(CardTypeDef.CHAT_MEMBER_CARD, ChatMicMemberCard.class);
        factory.registerCardView(CardTypeDef.CHAT_MEMBER_EMPTY_CARD, ChatMemberEmptyCard.class);
        chatMicMemberListAdapter = new CardListAdapter(factory);
        binding.iChatList.rvChatMicMemberList.setLayoutManager(new GridLayoutManager(this,4));
        binding.iChatList.rvChatMicMemberList.addItemDecoration(new ChatItemDecoration((int) DisplayUtil.convertDpToPixel(6, this), (int) DisplayUtil.convertDpToPixel(12, this)));
        binding.iChatList.rvChatMicMemberList.setAdapter(chatMicMemberListAdapter);

        chatMicMemberViewModel = new ContentViewModel.Builder()
                .setContentModel(new ChatMicMemberContentModel(chatRoom.getSelf(), roomController))
                .setLoadMoreEnable(false)
                .build();

        chatMicMemberViewModel.bindView(chatMicMemberListAdapter);
    }

    private void initChatMessageList() {
        DefaultCardViewFactory factory = new DefaultCardViewFactory();
        factory.registerCardView(CardTypeDef.CHAT_MESSAGE_CARD, ChatMessageCard.class);
        chatMsgListAdapter = new CardListAdapter(factory);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        binding.iChatList.rvChatMsgList.setLayoutManager(linearLayoutManager);

        int marginLeft = (int) DisplayUtil.convertDpToPixel(20, this);
        int marginRight = (int) DisplayUtil.convertDpToPixel(110, this);
        int marginTop = (int) DisplayUtil.convertDpToPixel(2, this);
        binding.iChatList.rvChatMsgList.addItemDecoration(new ChatItemDecoration(marginLeft, marginRight, marginTop, marginTop));
        binding.iChatList.rvChatMsgList.setAdapter(chatMsgListAdapter);
        chatMsgContentModel = new ChatMsgContentModel(this, chatRoom, this.roomController);
        chatMsgViewModel = new ContentViewModel.Builder()
                .setContentModel(chatMsgContentModel)
                .setLoadMoreEnable(false)
                .build();

        RecyclerView.AdapterDataObserver observer = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int totalItemCount = linearLayoutManager.getItemCount();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();

                // 如果新数据是添加到底部，并且底部是可见的，则自动滚动到底部
                // 这里的-1是因为position是从0开始的
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (totalItemCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    binding.iChatList.rvChatMsgList.scrollToPosition(positionStart);
                }
            }
        };

        chatMsgListAdapter.registerAdapterDataObserver(observer);

        chatMsgViewModel.bindView(chatMsgListAdapter);
    }

    private void onNetworkDisconnect(boolean isCompere) {
        VoicechatDialogReconnectBinding binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.voicechat_dialog_reconnect, null, false);
        binding.setLifecycleOwner(this);
        CustomViewHolder viewHolder = new CustomViewHolder(binding.getRoot());
        DialogPlus dialog = DialogPlus.newDialog(this)
                .setContentHolder(viewHolder)
                .setGravity(Gravity.CENTER)
                .setExpanded(false)
                .setOverlayBackgroundResource(android.R.color.transparent)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(DialogPlus dialog, View view) {
                        if(view.getId() == R.id.btn_cancel) {
                            dialog.dismiss();
                        } else if(view.getId() == R.id.btn_retry) {
                            //TODO SDK 目前暂不知道重试该做哪些操作
                            dialog.dismiss();
                        }
                    }
                })
                .create();
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(chatMicMemberViewModel != null)
            this.chatMicMemberViewModel.unBind();
        if(chatMsgViewModel != null)
            this.chatMsgViewModel.unBind();
        if(chatViewModel != null)
            this.chatViewModel.unBind();
        if(roomController != null){
            this.roomController.removeObserver(this.roomCallback);
            this.roomController.release();
            this.roomController = null;
        }
    }

    @Override
    public void onBackPressed() {
        DialogHelper.showCloseDialog(this , this.roomController, this.chatRoom.isCompere());
    }

}
