package com.aliyun.auikits.single.server;

import com.aliyun.auikits.single.Single;

public class Server implements Single {
    private String mAuthorizeToken;

    public void setAuthorizeToken(String auth){
        this.mAuthorizeToken = auth;
    }

    public String getAuthorizeToken(){
        return this.mAuthorizeToken;
    }
}
