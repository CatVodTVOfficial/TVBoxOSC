package com.github.tvbox.osc.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.github.tvbox.osc.R;

import org.jetbrains.annotations.NotNull;



/**
 * 描述
 *
 * @author pj567
 * @since 2020/12/27
 */
public class LivePasswordDialog extends BaseDialog {
    private EditText inputPassword;

    public LivePasswordDialog(@NonNull @NotNull Context context) {
        super(context);
        setOwnerActivity((Activity) context);
        setContentView(R.layout.dialog_live_password);
        inputPassword = findViewById(R.id.input);
        findViewById(R.id.inputSubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = inputPassword.getText().toString().trim();
                if (!password.isEmpty()) {
                    listener.onChange(password);
                    dismiss();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        listener.onCancel();
        dismiss();
    }

    public void setOnListener(OnListener listener) {
        this.listener = listener;
    }

    OnListener listener = null;

    public interface OnListener {
        void onChange(String password);
        void onCancel();
    }
}
