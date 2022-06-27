package com.github.tvbox.osc.ui.adapter;

import android.graphics.Color;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.ChannelGroup;

import java.util.ArrayList;


/**
 * @author pj567
 * @date :2021/1/12
 * @description:
 */
public class ChannelGroupAdapter extends BaseQuickAdapter<ChannelGroup, BaseViewHolder> {
    public ChannelGroupAdapter() {
        super(R.layout.item_channel_group, new ArrayList<>());
    }

    @Override
    protected void convert(BaseViewHolder helper, ChannelGroup item) {
        TextView tvGroupName = helper.getView(R.id.tvGroupName);
        tvGroupName.setText(item.getGroupName());
        if (item.isDefault()) {
            tvGroupName.setTextColor(mContext.getResources().getColor(R.color.color_1890FF));
        } else {
            tvGroupName.setTextColor(Color.WHITE);
        }
    }
}