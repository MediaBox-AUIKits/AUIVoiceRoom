package com.aliyun.auikits.rtc;

import android.view.View;

import java.lang.ref.WeakReference;

public final class PreviewInfo {
    private boolean isTop;
    private boolean mirror;
    private String userId;
    private WeakReference<View> view;

    public PreviewInfo(String userId_, View view_, boolean isTop_, boolean mirror_) {
        this.userId = userId_;
        this.view = new WeakReference<>(view_);
        this.isTop = isTop_;
        this.mirror = mirror_;
    }

    public final String getUserId() {
        return this.userId;
    }

    public final void setUserId(String str) {
        this.userId = str;
    }

    public final WeakReference<View> getView() {
        return this.view;
    }

    public final void setView(WeakReference<View> weakReference) {
        this.view = weakReference;
    }

    public final boolean isTop() {
        return this.isTop;
    }

    public final void setTop(boolean z) {
        this.isTop = z;
    }

    public final boolean getMirror() {
        return this.mirror;
    }

    public final void setMirror(boolean z) {
        this.mirror = z;
    }
}
