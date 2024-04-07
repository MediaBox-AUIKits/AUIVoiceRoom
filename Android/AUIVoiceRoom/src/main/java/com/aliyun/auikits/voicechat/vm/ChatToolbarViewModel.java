package com.aliyun.auikits.voicechat.vm;

import android.app.Activity;
import android.content.Context;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aliyun.auikits.voicechat.R;
import com.aliyun.auikits.voicechat.databinding.VoicechatDialogMusicBinding;
import com.aliyun.auikits.voicechat.databinding.VoicechatDialogSettingBinding;
import com.aliyun.auikits.voicechat.databinding.VoicechatDialogSoundEffectBinding;
import com.aliyun.auikits.voicechat.model.entity.ChatRoomCallback;
import com.aliyun.auikits.voicechat.service.ChatRoomManager;
import com.aliyun.auikits.voicechat.adapter.ChatItemDecoration;
import com.aliyun.auikits.voicechat.base.card.CardListAdapter;
import com.aliyun.auikits.voicechat.base.feed.ContentViewModel;
import com.aliyun.auikits.voicechat.model.content.ChatSoundEffectContentModel;
import com.aliyun.auikits.voicechat.model.entity.ChatMember;
import com.aliyun.auikits.voicechat.model.content.ChatMusicContentModel;
import com.aliyun.auikits.voicechat.model.content.ChatReverbContentModel;
import com.aliyun.auikits.voicechat.model.content.ChatVoiceContentModel;
import com.aliyun.auikits.voicechat.util.DisplayUtil;
import com.aliyun.auikits.voicechat.util.ToastHelper;
import com.aliyun.auikits.voicechat.widget.card.CardTypeDef;
import com.aliyun.auikits.voicechat.widget.card.ChatMusicCard;
import com.aliyun.auikits.voicechat.widget.card.ChatSoundEffectCard;
import com.aliyun.auikits.voicechat.widget.card.ChatSoundMixCard;
import com.aliyun.auikits.voicechat.widget.card.DefaultCardViewFactory;
import com.aliyun.auikits.voicechat.widget.list.CustomViewHolder;
import com.aliyun.auikits.voicechat.widget.view.InputTextMsgDialog;
import com.aliyun.auikits.voiceroom.AUIVoiceRoom;
import com.aliyun.auikits.voiceroom.bean.UserInfo;
import com.aliyun.auikits.voiceroom.callback.ActionCallback;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.orhanobut.dialogplus.DialogPlus;

import java.lang.ref.WeakReference;
import java.util.Map;


public class ChatToolbarViewModel extends ViewModel {

    private AUIVoiceRoom roomController;
    private ChatMember chatMember;
    public ObservableBoolean volumeSwitch = new ObservableBoolean(true);
    public ObservableBoolean musicEnable = new ObservableBoolean(false);
    public ObservableBoolean soundEffectEnable = new ObservableBoolean(false);
    public ObservableBoolean settingEnable = new ObservableBoolean(false);
    public ObservableInt microphoneIconRes = new ObservableInt();
    private boolean connectMic = false;
    private ChatRoomCallback roomCallback;

    public void bind(ChatMember chatMember, AUIVoiceRoom roomController) {
        this.chatMember = chatMember;
        this.roomController = roomController;
        updateMicrophoneIconRes(chatMember.getMicrophoneStatus());
        this.roomCallback = new ChatRoomCallback() {
            @Override
            public void onUserJoinMic(UserInfo user) {
                //非主持人模式下
                if(!roomController.isHost() && user.equals(roomController.getCurrentUser())) {
                    connectMic = true;
                    onChatConnectChanged(connectMic);
                }
            }

            @Override
            public void onUserLeaveMic(UserInfo user) {
                if(!roomController.isHost() && user.equals(roomController.getCurrentUser())) {
                    connectMic = false;
                    onChatConnectChanged(connectMic);
                }
            }

            @Override
            public void onUserMicOn(UserInfo user) {
                //当前用户的mic位变化,能收到这个，说明已经是连麦状态
                if(connectMic && user.equals(roomController.getCurrentUser())) {
                    ChatToolbarViewModel.this.chatMember.setMicrophoneStatus(ChatMember.MICROPHONE_STATUS_ON);
                    updateMicrophoneIconRes(ChatToolbarViewModel.this.chatMember.getMicrophoneStatus());
                }
            }

            @Override
            public void onUserMicOff(UserInfo user) {
                //当前用户的mic位变化,能收到这个，说明已经是连麦状态
                if(connectMic && user.equals(roomController.getCurrentUser())) {
                    ChatToolbarViewModel.this.chatMember.setMicrophoneStatus(ChatMember.MICROPHONE_STATUS_OFF);
                    updateMicrophoneIconRes(ChatToolbarViewModel.this.chatMember.getMicrophoneStatus());
                }
            }
        };
        this.roomController.addRoomCallback(roomCallback);
    }

    public void unBind() {
        this.roomController.removeRoomCallback(roomCallback);
    }

    public void onInputMsgClick(View view) {

        Context context = view.getContext();
        Activity activity = (Activity) context;
        WindowManager windowManager = activity.getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        InputTextMsgDialog mInputTextMsgDialog = new InputTextMsgDialog(context);
        WindowManager.LayoutParams lp = mInputTextMsgDialog.getWindow().getAttributes();
        lp.width = display.getWidth();
        mInputTextMsgDialog.getWindow().setAttributes(lp);
        mInputTextMsgDialog.setCancelable(true);
        mInputTextMsgDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mInputTextMsgDialog.setOnTextSendListener(new InputTextMsgDialog.OnTextSendListener() {
            @Override
            public void onTextSend(String msg) {
                onSendMessage(new WeakReference<>(context), msg);
            }
        });
        mInputTextMsgDialog.show();
    }

    private void onSendMessage(WeakReference<Context> contextRef, String msg) {
        this.roomController.sendTextMessage(msg, new ActionCallback() {
            @Override
            public void onResult(int code, String msg, Map<String, Object> params) {
                if(code != ChatRoomManager.CODE_SUCCESS) {
                    Context context = contextRef.get();
                    if(context != null) {
                        ToastHelper.showToast(context, R.string.voicechat_send_message_failed, Toast.LENGTH_SHORT);
                    }
                }
            }
        });
    }

    public void onVolumeSwitchChange(View view) {
        Context context = view.getContext();
        this.volumeSwitch.set(!this.volumeSwitch.get());

        this.roomController.openLoudSpeaker(this.volumeSwitch.get());
        ToastHelper.showToast(context, this.volumeSwitch.get() ? R.string.voicechat_chat_volume_on : R.string.voicechat_chat_volume_off, Toast.LENGTH_SHORT);
    }

    public void onMusicClick(View view) {
        Context context = view.getContext();
        VoicechatDialogMusicBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.voicechat_dialog_music, null, false);
        binding.setLifecycleOwner((LifecycleOwner) context);

        DefaultCardViewFactory factory = new DefaultCardViewFactory();
        factory.registerCardView(CardTypeDef.CHAT_MUSIC_CARD, ChatMusicCard.class);
        CardListAdapter musicCardListAdapter = new CardListAdapter(factory);

        binding.rvChatDataList.setLayoutManager(new LinearLayoutManager(context));
        binding.rvChatDataList.addItemDecoration(new ChatItemDecoration(0, 0));
        binding.rvChatDataList.setAdapter(musicCardListAdapter);

        ChatMusicContentModel musicReverbContentModel = new ChatMusicContentModel();
        ContentViewModel musicContentViewModel = new ContentViewModel.Builder()
                .setContentModel(musicReverbContentModel)
                .setLoadMoreEnable(false)
                .build();
        musicContentViewModel.bindView(musicCardListAdapter);

        musicCardListAdapter.addChildClickViewIds(R.id.btn_play, R.id.btn_apply);

        ChatMusicViewModel vm = new ChatMusicViewModel();
        //TODO APP 绑定音乐当前数据
        binding.setViewModel(vm);
        CustomViewHolder viewHolder = new CustomViewHolder(binding.getRoot());
        DialogPlus dialog = DialogPlus.newDialog(context)
                .setContentHolder(viewHolder)
                .setGravity(Gravity.BOTTOM)
                .setExpanded(false)
                .setOverlayBackgroundResource(android.R.color.transparent)
                .create();


        musicCardListAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {

            @Override
            public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                if(view.getId() == R.id.btn_play) {
                    //TODO SDK 对接播放和revert逻辑
                    musicReverbContentModel.playOrStopItem(position);
                } else if(view.getId() == R.id.btn_apply) {
                    //TODO SDK 对接应用背景音乐逻辑
                    dialog.dismiss();
                }
            }
        });


        dialog.show();

    }

    public void onSoundEffectClick(View view) {
        Context context = view.getContext();
        VoicechatDialogSoundEffectBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.voicechat_dialog_sound_effect, null, false);
        binding.setLifecycleOwner((LifecycleOwner) context);

        DefaultCardViewFactory factory = new DefaultCardViewFactory();
        factory.registerCardView(CardTypeDef.CHAT_SOUND_EFFECT_CARD, ChatSoundEffectCard.class);
        CardListAdapter soundEffectCardListAdapter = new CardListAdapter(factory);

        binding.rvChatDataList.setLayoutManager(new LinearLayoutManager(context));
        binding.rvChatDataList.addItemDecoration(new ChatItemDecoration(0, 0));
        binding.rvChatDataList.setAdapter(soundEffectCardListAdapter);

        ChatSoundEffectContentModel soundEffectContentModel = new ChatSoundEffectContentModel(context);
        ContentViewModel soundEffectContentViewModel = new ContentViewModel.Builder()
                .setContentModel(soundEffectContentModel)
                .setLoadMoreEnable(false)
                .build();
        soundEffectContentViewModel.bindView(soundEffectCardListAdapter);

        soundEffectCardListAdapter.addChildClickViewIds(R.id.btn_play, R.id.btn_apply);

        CustomViewHolder viewHolder = new CustomViewHolder(binding.getRoot());
        DialogPlus dialog = DialogPlus.newDialog(context)
                .setContentHolder(viewHolder)
                .setGravity(Gravity.BOTTOM)
                .setExpanded(false)
                .setOverlayBackgroundResource(android.R.color.transparent)
                .create();

        soundEffectCardListAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                if(view.getId() == R.id.btn_play) {
                    //TODO SDK 对接播放和revert逻辑
                    soundEffectContentModel.playOrStopItem(position);
                } else if(view.getId() == R.id.btn_apply) {
                    //TODO SDK 对接应用背景音乐逻辑
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    public void onSettingClick(View view) {

        Context context = view.getContext();
        VoicechatDialogSettingBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.voicechat_dialog_setting, null, false);
        binding.setLifecycleOwner((LifecycleOwner) context);
        ChatSettingViewModel settingViewModel = new ChatSettingViewModel();
        //TODO SDK 读取当前耳返
        binding.setViewModel(settingViewModel);
        CustomViewHolder viewHolder = new CustomViewHolder(binding.getRoot());

        DefaultCardViewFactory factory = new DefaultCardViewFactory();
        factory.registerCardView(CardTypeDef.CHAT_SOUND_MIX_CARD, ChatSoundMixCard.class);
        CardListAdapter reverbCardListAdapter = new CardListAdapter(factory);

        binding.rvReverbList.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false));
        binding.rvReverbList.addItemDecoration(new ChatItemDecoration(DisplayUtil.dip2px(8), 0));
        binding.rvReverbList.setAdapter(reverbCardListAdapter);

        //TODO SDK 读取当前选中状态
        int lastReverbSelectPos = 0;
        ChatReverbContentModel chatReverbContentModel = new ChatReverbContentModel(lastReverbSelectPos);
        ContentViewModel reverbContentViewModel = new ContentViewModel.Builder()
                .setContentModel(chatReverbContentModel)
                .setLoadMoreEnable(false)
                .build();
        reverbContentViewModel.bindView(reverbCardListAdapter);
        reverbCardListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                chatReverbContentModel.selectItem(position);
            }
        });

        CardListAdapter voiceCardListAdapter = new CardListAdapter(factory);
        binding.rvVoiceList.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        binding.rvVoiceList.addItemDecoration(new ChatItemDecoration(DisplayUtil.dip2px(8), 0));
        binding.rvVoiceList.setAdapter(voiceCardListAdapter);

        //TODO SDK 读取当前选中状态
        int lastVoiceSelectPos = 0;
        ChatVoiceContentModel chatVoiceContentModel = new ChatVoiceContentModel(lastVoiceSelectPos);
        ContentViewModel voiceContentViewModel = new ContentViewModel.Builder()
                .setContentModel(chatVoiceContentModel)
                .setLoadMoreEnable(false)
                .build();
        voiceContentViewModel.bindView(voiceCardListAdapter);
        voiceCardListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                chatVoiceContentModel.selectItem(position);
            }
        });

        DialogPlus dialog = DialogPlus.newDialog(context)
                .setContentHolder(viewHolder)
                .setGravity(Gravity.BOTTOM)
                .setExpanded(false)
                .setOverlayBackgroundResource(android.R.color.transparent)
                .create();
        dialog.show();
    }

    public void onMicrophoneChange(View view) {
        int microphoneStatus = chatMember.getMicrophoneStatus();
        if(microphoneStatus != ChatMember.MICROPHONE_STATUS_DISABLE) {
            if(microphoneStatus == ChatMember.MICROPHONE_STATUS_OFF) {
                microphoneStatus = ChatMember.MICROPHONE_STATUS_ON;
            } else {
                microphoneStatus = ChatMember.MICROPHONE_STATUS_OFF;
            }
            ToastHelper.showToast(view.getContext(), microphoneStatus == ChatMember.MICROPHONE_STATUS_OFF ? R.string.voicechat_chat_microphone_off : R.string.voicechat_chat_microphone_on, Toast.LENGTH_SHORT);
            chatMember.setMicrophoneStatus(microphoneStatus);
            this.roomController.openMic(microphoneStatus == ChatMember.MICROPHONE_STATUS_ON);
            updateMicrophoneIconRes(microphoneStatus);
        }
    }

    private void updateMicrophoneIconRes(int microphoneStatus) {
        if(microphoneStatus == ChatMember.MICROPHONE_STATUS_ON) {
            this.microphoneIconRes.set(R.drawable.voicechat_ic_microphone_on);
        } else if(microphoneStatus == ChatMember.MICROPHONE_STATUS_OFF) {
            this.microphoneIconRes.set(R.drawable.voicechat_ic_microphone_off);
        } else {
            this.microphoneIconRes.set(R.drawable.voicechat_ic_microphone_disabled);
        }

    }

    public void onChatConnectChanged(boolean connect) {
        this.settingEnable.set(connect);
        this.soundEffectEnable.set(connect);
        if(connect) {
            if(this.chatMember.getMicrophoneStatus() == ChatMember.MICROPHONE_STATUS_DISABLE) {
                this.chatMember.setMicrophoneStatus(ChatMember.MICROPHONE_STATUS_ON);
            }
        } else {
            this.chatMember.setMicrophoneStatus(ChatMember.MICROPHONE_STATUS_DISABLE);
        }
        updateMicrophoneIconRes(this.chatMember.getMicrophoneStatus());
    }
}
