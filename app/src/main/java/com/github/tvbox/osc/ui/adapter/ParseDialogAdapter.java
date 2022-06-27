package com.github.tvbox.osc.ui.adapter;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.ParseBean;

import java.util.ArrayList;

/**
 * @author pj567
 * @date :2021/3/9
 * @description:
 */
public class ParseDialogAdapter extends BaseQuickAdapter<ParseBean, BaseViewHolder> {
    public ParseDialogAdapter() {
        super(R.layout.item_dialog_parse, new ArrayList<>());
    }

    @Override
    protected void convert(BaseViewHolder helper, ParseBean item) {
        TextView tvParse = helper.getView(R.id.tvParse);
        tvParse.setVisibility(View.VISIBLE);
        if (item.isDefault()) {
            tvParse.setTextColor(mContext.getResources().getColor(R.color.color_02F8E1));
        } else {
            tvParse.setTextColor(Color.WHITE);
        }
        tvParse.setText(item.getName());
    }
}