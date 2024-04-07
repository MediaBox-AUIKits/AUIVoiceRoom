package com.aliyun.auikits.voicechat.widget.helper;

import android.view.View;

public class DebouncedOnClickListener implements View.OnClickListener {
    private View.OnClickListener onClickListener;

    private static final long minimumInterval = 500L;

    private long lastClickTimestamp = 0L;

    public DebouncedOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public void onClick(View view) {
        long currentTimestamp = System.currentTimeMillis();
        if(lastClickTimestamp == 0L  || currentTimestamp - lastClickTimestamp > minimumInterval) {
            lastClickTimestamp = currentTimestamp;
            performClick(view);
        }
        lastClickTimestamp = currentTimestamp;
    }

    void performClick(View view) {
        if(onClickListener != null) {
            onClickListener.onClick(view);
        }
    }
}
