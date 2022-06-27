package com.github.tvbox.osc.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IdRes;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.server.ControlManager;
import com.github.tvbox.osc.ui.tv.QRCodeGen;

/**
 * 描述
 *
 * @author pj567
 * @since 2020/12/27
 */
public class RemoteDialog {
    private View rootView;
    private Dialog mDialog;
    private Context mContext;
    private ImageView ivQRCode;
    private TextView tvAddress;

    public RemoteDialog() {

    }

    public RemoteDialog build(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.dialog_remote, null);
        mDialog = new Dialog(context, R.style.CustomDialogStyle);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setContentView(rootView);
        mContext = context;
        init();
        return this;
    }

    @SuppressWarnings("unchecked")
    private <T extends View> T findViewById(@IdRes int viewId) {
        View view = null;
        if (rootView != null) {
            view = rootView.findViewById(viewId);
        }
        return (T) view;
    }

    private void init() {
        ivQRCode = findViewById(R.id.ivQRCode);
        tvAddress = findViewById(R.id.tvAddress);
        refreshQRCode();
    }

    private void refreshQRCode() {
        String address = ControlManager.get().getAddress(false);
        tvAddress.setText(String.format("手机/电脑扫描左边二维码或者直接浏览器访问地址\n%s", address));
        ivQRCode.setImageBitmap(QRCodeGen.generateBitmap(address, 200, 200));
    }

    public void dismiss() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    public void show() {
        if (mDialog != null && !mDialog.isShowing()) {
            mDialog.show();
        }
    }

    public boolean isShowing() {
        return mDialog != null && mDialog.isShowing();
    }
}
