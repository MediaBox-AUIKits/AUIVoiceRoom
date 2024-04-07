package com.aliyun.auikits.voicechat.model.entity.network;

public class BaseResponse{
    public static final int CODE_SUCCESS = 200;

    protected int code;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isSuccess() {
        return code == CODE_SUCCESS;
    }
}
