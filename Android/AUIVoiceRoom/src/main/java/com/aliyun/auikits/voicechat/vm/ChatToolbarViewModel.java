package com.aliyun.auikits.voicechat.vm;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aliyun.auikits.common.util.CommonUtil;
import com.aliyun.auikits.rtc.MixSoundType;
import com.aliyun.auikits.rtc.VoiceChangeType;
import com.aliyun.auikits.voice.AudioOutputType;
import com.aliyun.auikits.voicechat.R;
import com.aliyun.auikits.voicechat.base.card.CardEntity;
import com.aliyun.auikits.voicechat.databinding.VoicechatDialogMusicBinding;
import com.aliyun.auikits.voicechat.databinding.VoicechatDialogSettingBinding;
import com.aliyun.auikits.voicechat.databinding.VoicechatDialogSoundEffectBinding;
import com.aliyun.auikits.voicechat.model.entity.ChatMusicItem;
import com.aliyun.auikits.voicechat.model.entity.ChatRoomCallback;
import com.aliyun.auikits.voicechat.model.entity.ChatSoundMix;
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
import com.aliyun.auikits.voice.ARTCVoiceRoomEngine;
import com.aliyun.auikits.voiceroom.bean.AccompanyPlayState;
import com.aliyun.auikits.voiceroom.bean.AudioEffect;
import com.aliyun.auikits.voiceroom.bean.MixSound;
import com.aliyun.auikits.voiceroom.bean.Music;
import com.aliyun.auikits.voiceroom.bean.UserInfo;
import com.aliyun.auikits.voiceroom.bean.VoiceChange;
import com.aliyun.auikits.voiceroom.callback.ActionCallback;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.orhanobut.dialogplus.DialogPlus;

import java.lang.ref.WeakReference;
import java.util.Map;


public class ChatToolbarViewModel extends ViewModel {

    private ARTCVoiceRoomEngine roomController;
    private ChatMember chatMember;
    public ObservableBoolean volumeSwitch = new ObservableBoolean(true);
    public ObservableInt musicVisibility = new ObservableInt(View.GONE);
    public ObservableBoolean soundEffectEnable = new ObservableBoolean(false);
    public ObservableBoolean settingEnable = new ObservableBoolean(false);
    public ObservableInt microphoneIconRes = new ObservableInt();
    private boolean connectMic = false;
    private ChatRoomCallback roomCallback;
    private ContentViewModel mSoundEffectViewModel;
    private ChatSoundEffectContentModel mSoundEffectContentModel;
    private DialogPlus mSoundEffectDialog;
    private ContentViewModel mBackgroundMusicContentModel;
    private ChatMusicContentModel mMusicReverbContentModel;
    private DialogPlus mBackgroundMusicDialog;
    private ContentViewModel mAudioSettingViewModel;
    private DialogPlus mAudioSettingDialog;
    private ObservableBoolean mInputPanelShown = null;
    private boolean isHost = false;

    public void bind(boolean isHost, ChatMember chatMember, ARTCVoiceRoomEngine roomController) {
        this.isHost = isHost;
        this.chatMember = chatMember;
        this.roomController = roomController;
        updateMicrophoneIconRes(chatMember.getMicrophoneStatus());
        this.roomCallback = new ChatRoomCallback() {
            @Override
            public void onJoinedMic(UserInfo user) {
                //非主持人模式下
                if(!roomController.isAnchor() && user.equals(roomController.getCurrentUser())) {
                    connectMic = true;
                    onChatConnectChanged(connectMic);
                }

                onJoinMic(user);
            }

            @Override
            public void onLeavedMic(UserInfo user) {
                if(!roomController.isAnchor() && user.equals(roomController.getCurrentUser())) {
                    connectMic = false;
                    onChatConnectChanged(connectMic);
                }

                onLeaveMic(user);
            }

            @Override
            public void onMicUserMicrophoneChanged(UserInfo user, boolean open) {
                if(open){
                    //当前用户的mic位变化,能收到这个，说明已经是连麦状态
                    if(connectMic && user.equals(roomController.getCurrentUser())) {
                        ChatToolbarViewModel.this.chatMember.setMicrophoneStatus(ChatMember.MICROPHONE_STATUS_ON);
                        updateMicrophoneIconRes(ChatToolbarViewModel.this.chatMember.getMicrophoneStatus());
                    }
                }else{
                    //当前用户的mic位变化,能收到这个，说明已经是连麦状态
                    if(connectMic && user.equals(roomController.getCurrentUser())) {
                        ChatToolbarViewModel.this.chatMember.setMicrophoneStatus(ChatMember.MICROPHONE_STATUS_OFF);
                        updateMicrophoneIconRes(ChatToolbarViewModel.this.chatMember.getMicrophoneStatus());
                    }
                }
            }

            @Override
            public void onAccompanyStateChanged(AccompanyPlayState state) {
                if(mMusicReverbContentModel != null){
                    if(state == AccompanyPlayState.STARTED){
                        mMusicReverbContentModel.onUpdatePlayState(true);
                    }else{
                        mMusicReverbContentModel.onUpdatePlayState(false);
                    }
                    mMusicReverbContentModel.notifyContentUpdate();
                }
            }
        };
        this.roomController.addObserver(roomCallback);
        if(roomController.isAnchor()){
            musicVisibility.set(View.VISIBLE);
            soundEffectEnable.set(true);
            settingEnable.set(true);
        }
    }

    public void setMicEntryShowObservable(ObservableBoolean observable){
        this.mInputPanelShown = observable;
    }

    public void unBind() {
        if(mSoundEffectViewModel != null){
            mSoundEffectViewModel.unBind();
        }
        if(mBackgroundMusicContentModel != null){
            mBackgroundMusicContentModel.unBind();
        }
        this.roomController.removeObserver(roomCallback);
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
        mInputTextMsgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(mInputPanelShown != null && !isHost){
                    mInputPanelShown.set(true);
                }
            }
        });
        if(mInputPanelShown != null){
            mInputPanelShown.set(false);
        }
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

        this.roomController.setAudioOutputType(this.volumeSwitch.get() ? AudioOutputType.LOUDSPEAKER : AudioOutputType.HEADSET);
        ToastHelper.showToast(context, this.volumeSwitch.get() ? R.string.voicechat_chat_volume_on : R.string.voicechat_chat_volume_off, Toast.LENGTH_SHORT);
    }

    public void onMusicClick(View view) {
        if(mBackgroundMusicDialog != null){
            mBackgroundMusicDialog.show();
            return;
        }
        Context context = view.getContext();
        VoicechatDialogMusicBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.voicechat_dialog_music, null, false);
        binding.setLifecycleOwner((LifecycleOwner) context);

        DefaultCardViewFactory factory = new DefaultCardViewFactory();
        factory.registerCardView(CardTypeDef.CHAT_MUSIC_CARD, ChatMusicCard.class);
        CardListAdapter musicCardListAdapter = new CardListAdapter(factory);

        binding.rvChatDataList.setLayoutManager(new LinearLayoutManager(context));
        binding.rvChatDataList.addItemDecoration(new ChatItemDecoration(0, 0));
        binding.rvChatDataList.setAdapter(musicCardListAdapter);

        mMusicReverbContentModel = new ChatMusicContentModel(context);
        mBackgroundMusicContentModel = new ContentViewModel.Builder()
                .setContentModel(mMusicReverbContentModel)
                .setLoadMoreEnable(false)
                .build();
        mBackgroundMusicContentModel.bindView(musicCardListAdapter);

        musicCardListAdapter.addChildClickViewIds(R.id.btn_play, R.id.btn_apply);

        ChatMusicViewModel musicVm = new ChatMusicViewModel();
        musicVm.bind(roomController);
        binding.setViewModel(musicVm);
        CustomViewHolder viewHolder = new CustomViewHolder(binding.getRoot());
        mBackgroundMusicDialog = DialogPlus.newDialog(context)
                .setContentHolder(viewHolder)
                .setGravity(Gravity.BOTTOM)
                .setExpanded(false)
                .setOverlayBackgroundResource(android.R.color.transparent)
                .create();


        musicCardListAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {

            @Override
            public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                CardEntity entity = mMusicReverbContentModel.getTargetData(position);
                ChatMusicItem musicItem = (ChatMusicItem) entity.bizData;
                Music music = new Music(musicItem.getUrl());
                music.volume = musicVm.getMusicVolumeInt();
                mMusicReverbContentModel.onUpdatePlaySource(musicItem.getUrl());
                if(view.getId() == R.id.btn_play) { //试听
                    mMusicReverbContentModel.onUpdateApplying(false);
                    music.justForTest = true;
                    if(roomController != null){
                        musicItem.setPlaying(!musicItem.isPlaying());
                        if(musicItem.isPlaying()){
                            roomController.setBackgroundMusic(music); //试听
                            mMusicReverbContentModel.onUpdatePlayState(true);
                        }else{
                            roomController.setBackgroundMusic(null); //停止试听
                            mMusicReverbContentModel.onUpdatePlayState(false);
                        }
                    }
                } else if(view.getId() == R.id.btn_apply) {
                    mMusicReverbContentModel.onUpdateApplying(true);
                    music.justForTest = false;
                    if(roomController != null){
                        musicItem.setApplying(!musicItem.isApplying());
                        if(musicItem.isApplying()){
                            roomController.setBackgroundMusic(music); //应用
                            mMusicReverbContentModel.onUpdatePlayState(true);
                        }else{
                            roomController.setBackgroundMusic(null); //停止应用
                            mMusicReverbContentModel.onUpdatePlayState(false);
                        }
                    }
//                    dialog.dismiss();
                }
                mMusicReverbContentModel.notifyContentUpdate();
            }
        });
        mBackgroundMusicDialog.show();
    }

    public void onSoundEffectClick(View view) {
        if(mSoundEffectDialog != null){
            mSoundEffectDialog.show();
            return;
        }

        Context context = view.getContext();
        VoicechatDialogSoundEffectBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.voicechat_dialog_sound_effect, null, false);
        binding.setLifecycleOwner((LifecycleOwner) context);

        DefaultCardViewFactory factory = new DefaultCardViewFactory();
        factory.registerCardView(CardTypeDef.CHAT_SOUND_EFFECT_CARD, ChatSoundEffectCard.class);
        CardListAdapter soundEffectCardListAdapter = new CardListAdapter(factory);
        soundEffectCardListAdapter.bindEngine(roomController);

        binding.rvChatDataList.setLayoutManager(new LinearLayoutManager(context));
        binding.rvChatDataList.addItemDecoration(new ChatItemDecoration(0, 0));
        binding.rvChatDataList.setAdapter(soundEffectCardListAdapter);

        mSoundEffectContentModel = new ChatSoundEffectContentModel(context);
        mSoundEffectViewModel = new ContentViewModel.Builder()
                .setContentModel(mSoundEffectContentModel)
                .setLoadMoreEnable(false)
                .build();
        mSoundEffectViewModel.bindView(soundEffectCardListAdapter);

        soundEffectCardListAdapter.addChildClickViewIds(R.id.btn_play, R.id.btn_apply);

        CustomViewHolder viewHolder = new CustomViewHolder(binding.getRoot());
        mSoundEffectDialog = DialogPlus.newDialog(context)
                .setContentHolder(viewHolder)
                .setGravity(Gravity.BOTTOM)
                .setExpanded(false)
                .setOverlayBackgroundResource(android.R.color.transparent)
                .create();

        soundEffectCardListAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                CardEntity entity = mSoundEffectContentModel.getTargetData(position);
                if(entity == null)
                    return;
                ChatMusicItem item = (ChatMusicItem) entity.bizData;
                mSoundEffectContentModel.onUpdatePlaySource(item.getUrl());
                AudioEffect eff = new AudioEffect(item.getUrl());
                eff.volume = item.getVolume();
                if(view.getId() == R.id.btn_play) {
                    eff.justForTest = true;
                    mSoundEffectContentModel.onUpdateApplyState(false);
                } else if(view.getId() == R.id.btn_apply) {
                    eff.justForTest = false;
                    mSoundEffectContentModel.onUpdateApplyState(true);
//                    dialog.dismiss();
                }
                if(roomController != null){
                    item.setSoundId(roomController.playAudioEffect(eff)); //应用特效
                }
                mSoundEffectContentModel.notifyContentUpdate();
            }
        });
        mSoundEffectDialog.show();
    }

    public void onSettingClick(View view) {
        if(mAudioSettingDialog != null){
            mAudioSettingDialog.show();
            return;
        }
        Context context = view.getContext();
        VoicechatDialogSettingBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.voicechat_dialog_setting, null, false);
        binding.setLifecycleOwner((LifecycleOwner) context);
        ChatSettingViewModel settingViewModel = new ChatSettingViewModel();
        settingViewModel.bind(roomController);
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
                CardEntity entity = chatReverbContentModel.getSelectedItem();
                if(entity != null){
                    ChatSoundMix soundMix = (ChatSoundMix) entity.bizData;
                    if(roomController != null){
                        roomController.setAudioMixSound(new MixSound(MixSoundType.fromInt(Integer.parseInt(soundMix.getId()))));
                    }
                }
            }
        });

        CardListAdapter voiceCardListAdapter = new CardListAdapter(factory);
        binding.rvVoiceList.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        binding.rvVoiceList.addItemDecoration(new ChatItemDecoration(DisplayUtil.dip2px(8), 0));
        binding.rvVoiceList.setAdapter(voiceCardListAdapter);

        //TODO SDK 读取当前选中状态
        int lastVoiceSelectPos = 0;
        ChatVoiceContentModel chatVoiceContentModel = new ChatVoiceContentModel(lastVoiceSelectPos);
        mAudioSettingViewModel = new ContentViewModel.Builder()
                .setContentModel(chatVoiceContentModel)
                .setLoadMoreEnable(false)
                .build();
        mAudioSettingViewModel.bindView(voiceCardListAdapter);
        voiceCardListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                chatVoiceContentModel.selectItem(position);
                CardEntity entity = chatVoiceContentModel.getSelectedItem();
                if(entity != null){
                    ChatSoundMix soundMix = (ChatSoundMix) entity.bizData;
                    if(roomController != null){
                        roomController.setVoiceChange(new VoiceChange(VoiceChangeType.fromInt(Integer.parseInt(soundMix.getId()))));
                    }
                }
            }
        });

        mAudioSettingDialog = DialogPlus.newDialog(context)
                .setContentHolder(viewHolder)
                .setGravity(Gravity.BOTTOM)
                .setExpanded(false)
                .setOverlayBackgroundResource(android.R.color.transparent)
                .create();
        mAudioSettingDialog.show();
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
            this.roomController.switchMicrophone(microphoneStatus == ChatMember.MICROPHONE_STATUS_ON);
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

    public void onLeaveMic(UserInfo user){
        if(roomController != null && roomController.isAnchor()){
            musicVisibility.set(View.VISIBLE);
        }else{
            musicVisibility.set(View.GONE);
        }
        if(mBackgroundMusicContentModel != null)
            mBackgroundMusicContentModel.unBind();
        if(mBackgroundMusicDialog != null && mBackgroundMusicDialog.isShowing())
            mBackgroundMusicDialog.dismiss();
        if(mSoundEffectViewModel != null)
            mSoundEffectViewModel.unBind();
        if(mSoundEffectDialog != null && mSoundEffectDialog.isShowing())
            mSoundEffectDialog.dismiss();
        mBackgroundMusicDialog = null;
        mBackgroundMusicContentModel = null;
        mSoundEffectViewModel = null;
        mSoundEffectDialog = null;
    }

    public void onJoinMic(UserInfo user){
        if(roomController != null && roomController.isAnchor()){
            musicVisibility.set(View.VISIBLE);
        }else{
            musicVisibility.set(View.GONE);
        }
    }
}
