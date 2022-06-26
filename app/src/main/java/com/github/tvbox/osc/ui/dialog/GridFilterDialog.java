package com.github.tvbox.osc.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.IdRes;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.MovieSort;
import com.github.tvbox.osc.ui.adapter.GridFilterKVAdapter;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;

import java.util.ArrayList;

public class GridFilterDialog {
    private Context mContext;
    private View rootView;
    private Dialog mDialog;
    private LinearLayout filterRoot;

    public interface Callback {
        void change();
    }

    public GridFilterDialog build(Context context) {
        mContext = context;
        rootView = LayoutInflater.from(context).inflate(R.layout.dialog_grid_filter, null);
        filterRoot = rootView.findViewById(R.id.filterRoot);
        mDialog = new Dialog(context, R.style.CustomDialogStyle);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(true);
        mDialog.setContentView(rootView);
        return this;
    }

    public void setOnDismiss(Callback callback) {
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (selectChange) {
                    callback.change();
                }
            }
        });
    }

    public void setData(MovieSort.SortData sortData) {
        ArrayList<MovieSort.SortFilter> filters = sortData.filters;
        for (MovieSort.SortFilter filter : filters) {
            View line = LayoutInflater.from(mContext).inflate(R.layout.item_grid_filter, null);
            ((TextView) line.findViewById(R.id.filterName)).setText(filter.name);
            TvRecyclerView gridView = line.findViewById(R.id.mFilterKv);
            gridView.setHasFixedSize(true);
            gridView.setLayoutManager(new V7LinearLayoutManager(mContext, 0, false));
            GridFilterKVAdapter filterKVAdapter = new GridFilterKVAdapter();
            gridView.setAdapter(filterKVAdapter);
            String key = filter.key;
            ArrayList<String> values = new ArrayList<>(filter.values.keySet());
            ArrayList<String> keys = new ArrayList<>(filter.values.values());
            filterKVAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                View pre = null;

                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    if (sortData.filterSelect.get(key) == null || !sortData.filterSelect.get(key).equals(values.get(position))) {
                        sortData.filterSelect.put(key, keys.get(position));
                        selectChange = true;
                        if (pre != null) {
                            ((TextView) pre.findViewById(R.id.filterValue)).setTextColor(mContext.getResources().getColor(R.color.color_FFFFFF));
                        }
                        ((TextView) view.findViewById(R.id.filterValue)).setTextColor(mContext.getResources().getColor(R.color.color_03DAC5));
                        pre = view;
                    }
                }
            });
            filterKVAdapter.setNewData(values);
            filterRoot.addView(line);
        }
    }

    private boolean selectChange = false;

    public void show() {
        selectChange = false;
        if (mDialog != null && !mDialog.isShowing()) {
            mDialog.show();
            WindowManager.LayoutParams layoutParams = mDialog.getWindow().getAttributes();
            layoutParams.gravity = Gravity.BOTTOM;
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutParams.dimAmount = 0f;
            mDialog.getWindow().getDecorView().setPadding(0, 0, 0, 0);
            mDialog.getWindow().setAttributes(layoutParams);
        }
    }

    public void dismiss() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends View> T findViewById(@IdRes int viewId) {
        View view = null;
        if (rootView != null) {
            view = rootView.findViewById(viewId);
        }
        return (T) view;
    }
}