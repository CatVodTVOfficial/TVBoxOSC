package com.github.tvbox.osc.ui.dialog;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;

import com.github.tvbox.osc.R;

public class BaseDialog extends Dialog {
    public BaseDialog(@NonNull Context context) {
        super(context, R.style.CustomDialogStyle);
    }

    @Override
    public void show() {
        super.show();
    }
}
