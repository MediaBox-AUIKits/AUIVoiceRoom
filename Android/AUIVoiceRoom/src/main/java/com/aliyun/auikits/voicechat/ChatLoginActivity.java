package com.aliyun.auikits.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alivc.auimessage.model.token.IMNewToken;
import com.aliyun.auikits.voicechat.R;
import com.aliyun.auikits.voicechat.databinding.VoicechatActivityLoginBinding;
import com.aliyun.auikits.voicechat.base.network.RetrofitManager;
import com.aliyun.auikits.voicechat.model.api.ChatRoomApi;
import com.aliyun.auikits.voicechat.model.entity.network.ImTokenRequest;
import com.aliyun.auikits.voicechat.model.entity.network.ImTokenResponse;
import com.aliyun.auikits.voicechat.model.entity.network.LoginRequest;
import com.aliyun.auikits.voicechat.model.entity.network.LoginResponse;
import com.aliyun.auikits.voicechat.service.ChatRoomManager;
import com.aliyun.auikits.voicechat.util.PermissionUtils;
import com.aliyun.auikits.voicechat.util.SettingFlags;
import com.aliyun.auikits.voicechat.util.SettingFlagsKeyDef;
import com.aliyun.auikits.voicechat.util.ToastHelper;
import com.aliyun.auikits.voiceroom.bean.UserInfo;
import com.jaeger.library.StatusBarUtil;
import com.orhanobut.hawk.Hawk;
import com.permissionx.guolindev.PermissionX;

import java.util.regex.Pattern;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;

@Route(path = "/voicechat/ChatLoginActivity")
public class ChatLoginActivity extends AppCompatActivity{
    private static final String TAG = "ChatLoginTag";
    private static final int MAX_ID_LEN = 15;
    private VoicechatActivityLoginBinding binding;

    private String authorization;

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Hawk.init(getApplicationContext())
                .build();

        StatusBarUtil.setTransparent(this);
        RxJavaPlugins.setErrorHandler(Functions.<Throwable>emptyConsumer());

        binding = DataBindingUtil.setContentView(this, R.layout.voicechat_activity_login);
        binding.setLifecycleOwner(this);
        binding.userInputClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.userIdInput.setText("");
            }
        });

        InputFilter englishNumberUnderscoreFilter = (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                if (!Pattern.matches("[a-zA-Z0-9_]+", Character.toString(source.charAt(i)))) {
                    return "";
                }
            }
            return null;
        };

        binding.userIdInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_ID_LEN), englishNumberUnderscoreFilter});
        binding.userIdInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged( CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged( CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged( Editable s) {
                binding.userInputStat.setText((s == null ? 0 : s.length()) + "/15");
                if (s != null && s.length() > 0) {
                    binding.userInputClear.setVisibility(View.VISIBLE);
                }else{
                    binding.userInputClear.setVisibility(View.GONE);
                }
            }
        });

        binding.userIdInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case KeyEvent.KEYCODE_ENDCALL:
                    case KeyEvent.KEYCODE_ENTER:
                        onLoginClick(binding.loginBtn);
                        return true;
                    default:
                        return false;
                }
            }
        });

        Intent intent = getIntent();
        // 检查 Intent 的 action 和 category
        boolean isLaunchedFromHome = Intent.ACTION_MAIN.equals(intent.getAction()) &&
                intent.hasCategory(Intent.CATEGORY_LAUNCHER);

        if(isLaunchedFromHome) {
            findViewById(R.id.back_btn).setVisibility(View.GONE);
        } else {
            findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        }

        PermissionX.init(this)
                .permissions(PermissionUtils.getPermissions())
                .request((allGranted, grantedList, deniedList) -> {
                    if(!allGranted) {
                        ToastHelper.showToast(ChatLoginActivity.this, "请开通权限后重试", Toast.LENGTH_SHORT);
                        finish();
                    } else {
                        onAllPermissionGranted();
                    }
                });

        initData();

    }

    private boolean isValidUserId(String userId) {
        // 正则表达式：只允许英文、数字和下划线
        String regex = "^[a-zA-Z0-9_]+$";
        return userId.matches(regex);
    }

    private void initData() {
        String userId = SettingFlags.getFlag(SettingFlagsKeyDef.FLAG_PROFILE_ID, "");
        if(!TextUtils.isEmpty(userId)) {
            binding.userIdInput.setText(userId);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void onAllPermissionGranted() {
        onEnvironmentReady();
    }



    public void onLoginClick(View v) {
        String currentId = binding.userIdInput.getText().toString();
        if(TextUtils.isEmpty(currentId)){
            return;
        }

        if(!isValidUserId(currentId)) {
            ToastHelper.showToast(this, "请输入字母、数字、下划线", Toast.LENGTH_SHORT);
            return;
        }

        String userId = SettingFlags.getFlag(SettingFlagsKeyDef.FLAG_PROFILE_ID, "");


        if(!userId.equals(currentId)){
            SettingFlags.setFlag(SettingFlagsKeyDef.FLAG_PROFILE_ID, currentId);
        }
        binding.loadingMask.setVisibility(View.VISIBLE);

        UserInfo userInfo = new UserInfo(currentId, currentId);

        LoginRequest appSignRequest = new LoginRequest(userInfo.userId, userInfo.userId);
        RetrofitManager.getRetrofit(ChatRoomApi.HOST).create(ChatRoomApi.class).login(appSignRequest)
                .flatMap(new Function<LoginResponse, ObservableSource<ImTokenResponse>>() {
                    @Override
                    public ObservableSource<ImTokenResponse> apply(LoginResponse response) throws Throwable {
                        if(response.isSuccess()) {
                            ChatLoginActivity.this.authorization = "Bearer " + response.token;
                            ImTokenRequest imTokenRequest = new ImTokenRequest(userInfo.userId, userInfo.deviceId, "admin");
                            Log.v(TAG, "LoginSuccess");
                            return RetrofitManager.getRetrofit(ChatRoomApi.HOST).create(ChatRoomApi.class)
                                    .getImToken(authorization, imTokenRequest);

                        }
                        Log.v(TAG, "LoginFailed :" + response.getCode());
                        return Observable.error(new RuntimeException("获取Token错误"));
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ImTokenResponse>() {
                    @Override
                    public void accept(ImTokenResponse imTokenResponse) throws Throwable {
                        if(imTokenResponse.isSuccess()) {
                            Log.v(TAG, "getImToken Success ");
                            onLoginEnd(true, imTokenResponse.aliyun_im);
                        } else {
                            Log.v(TAG, "getImToken Failure:" + imTokenResponse.getCode());
                            onLoginEnd(false, null);
                        }


                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {
                        throwable.printStackTrace();
                        onLoginEnd(false, null);
                    }
                });
    }

    private void onEnvironmentReady() {


    }

    private void onLoginEnd(boolean success, IMNewToken aliyun_im) {
        binding.loadingMask.setVisibility(View.GONE);
        if(success) {
            String userId = SettingFlags.getFlag(SettingFlagsKeyDef.FLAG_PROFILE_ID, "");
            Intent intent = new Intent(this, ChatEntryActivity.class);
            intent.putExtra(ChatEntryActivity.KEY_AUTHORIZATION, ChatLoginActivity.this.authorization);
            intent.putExtra(ChatEntryActivity.KEY_IM_TAG, aliyun_im);
            intent.putExtra(ChatEntryActivity.KEY_USER_ID, userId);

            startActivity(intent);
        } else {
            ToastHelper.showToast(ChatLoginActivity.this, R.string.voicechat_login_failed, Toast.LENGTH_SHORT);
        }
    }

}
