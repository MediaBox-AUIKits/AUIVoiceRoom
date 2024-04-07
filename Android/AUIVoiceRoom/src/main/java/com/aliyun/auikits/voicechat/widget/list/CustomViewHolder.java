package com.aliyun.auikits.voicechat.widget.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orhanobut.dialogplus.ViewHolder;

public class CustomViewHolder extends ViewHolder {

    public interface OnViewInflatedListener {
        void onContentInflated(View content);
        void onHeaderInflated(View header);
        void onFooterInflated(View footer);
    }

    private OnViewInflatedListener onViewInflatedListener;

    public CustomViewHolder(int viewResourceId) {
        super(viewResourceId);
    }

    public CustomViewHolder(View contentView) {
        super(contentView);
    }


    public void setOnViewInflatedListener(OnViewInflatedListener listener) {
        this.onViewInflatedListener = listener;
    }

    @Override
    public View getView(LayoutInflater inflater, ViewGroup parent) {
        View view = super.getView(inflater, parent);
        if(onViewInflatedListener != null) {
            onViewInflatedListener.onContentInflated(getInflatedView());
        }
        return view;
    }

    @Override
    public void addHeader(View view) {
        super.addHeader(view);
        if(onViewInflatedListener != null) {
            onViewInflatedListener.onHeaderInflated(view);
        }
    }

    @Override
    public void addFooter(View view) {
        super.addFooter(view);
        if(onViewInflatedListener != null) {
            onViewInflatedListener.onFooterInflated(view);
        }
    }

}
