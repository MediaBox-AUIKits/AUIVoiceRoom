package com.aliyun.auikits.voicechat.widget.view;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.aliyun.auikits.voicechat.R;
import com.aliyun.auikits.voicechat.databinding.VoicechatDialogInputMsgBinding;
import com.aliyun.auikits.voicechat.util.ToastHelper;

public class InputTextMsgDialog extends Dialog {
    private static final String TAG = InputTextMsgDialog.class.getSimpleName();

    public interface OnTextSendListener {
        void onTextSend(String msg);
    }

    private VoicechatDialogInputMsgBinding binding;
//    private LinearLayout mConfirmArea;
    private InputMethodManager inputMethodManager;
    private OnTextSendListener mOnTextSendListener;

    public InputTextMsgDialog(@NonNull Context context) {
        super(context, R.style.voicechat_input_dialog);


        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.voicechat_dialog_input_msg, null, false);
        setContentView(binding.getRoot());

        inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        binding.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = binding.etInputMessage.getText().toString().trim();
                if (!TextUtils.isEmpty(msg)) {

                    if(mOnTextSendListener != null) {
                        mOnTextSendListener.onTextSend(msg);
                    }

                    inputMethodManager.showSoftInput(binding.etInputMessage, InputMethodManager.SHOW_FORCED);
                    inputMethodManager.hideSoftInputFromWindow(binding.etInputMessage.getWindowToken(), 0);
                    binding.etInputMessage.setText("");
                    dismiss();
                } else {
                    ToastHelper.showToast(context, R.string.voicechat_warning_not_empty, Toast.LENGTH_LONG);
                }
                binding.etInputMessage.setText(null);
            }
        });

        binding.etInputMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case KeyEvent.KEYCODE_ENDCALL:
                    case KeyEvent.KEYCODE_ENTER:
                        String msg = binding.etInputMessage.getText().toString().trim();
                        if (msg.length() > 0) {
                            if(mOnTextSendListener != null) {
                                mOnTextSendListener.onTextSend(msg);
                            }
                            inputMethodManager.hideSoftInputFromWindow(binding.etInputMessage.getWindowToken(), 0);
                            binding.etInputMessage.setText("");
                            dismiss();
                        } else {
                            ToastHelper.showToast(context, R.string.voicechat_warning_not_empty, Toast.LENGTH_LONG);
                        }
                        return true;
                    case KeyEvent.KEYCODE_BACK:
                        dismiss();
                        return false;
                    default:
                        return false;
                }
            }
        });


        binding.etInputMessage.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                Log.d(TAG, "onKey " + keyEvent.getCharacters());
                return false;
            }
        });

        binding.rlOutsideView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    public void show() {
        super.show();
        binding.etInputMessage.requestFocus();
    }

    public void setOnTextSendListener(OnTextSendListener onTextSendListener) {
        this.mOnTextSendListener = onTextSendListener;
    }
}
