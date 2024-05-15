package com.aliyun.auikits.voicechat.vm;


import android.view.View;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

import com.aliyun.auikits.voicechat.model.entity.ChatMusicItem;

public class ChatMusicCardViewModel extends ViewModel {

    public ObservableField<String> title = new ObservableField<String>("");
    public ObservableField<String> author = new ObservableField<String>("0");
    public ObservableBoolean playing = new ObservableBoolean(false);
    public ObservableBoolean applying = new ObservableBoolean(false);

    public void bind(ChatMusicItem musicItem) {
        this.title.set(musicItem.getTitle());
        this.author.set(musicItem.getAuthor());
        this.playing.set(musicItem.isPlaying());
        this.applying.set(musicItem.isApplying());
    }


    public void onPlayOrStopMusic(View view) {
        this.playing.set(!this.playing.get());
    }
}
