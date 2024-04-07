package com.aliyun.auikits.voicechat.adapter;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class ChatItemDecoration extends RecyclerView.ItemDecoration {
    private int marginLeft;
    private int marginRight;
    private int marginTop;
    private int marginBottom;

    public ChatItemDecoration(int marginHorizontal, int marginVertical) {
        this(marginHorizontal, marginHorizontal, marginVertical, marginVertical);
    }

    public ChatItemDecoration(int marginLeft, int marginRight, int marginTop, int marginBottom) {
        this.marginLeft = marginLeft;
        this.marginRight = marginRight;
        this.marginTop = marginTop;
        this.marginBottom = marginBottom;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.bottom = this.marginBottom;
        outRect.top = this.marginTop;
        outRect.left = this.marginLeft;
        outRect.right = this.marginRight;

    }
}
