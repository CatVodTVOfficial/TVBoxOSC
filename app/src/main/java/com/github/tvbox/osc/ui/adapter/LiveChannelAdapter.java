package com.github.tvbox.osc.ui.adapter;

import android.graphics.Color;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.LiveChannel;

import java.util.ArrayList;

/**
 * @author pj567
 * @date :2021/1/12
 * @description:
 */
public class LiveChannelAdapter extends BaseQuickAdapter<LiveChannel, BaseViewHolder> {
    public LiveChannelAdapter() {
        super(R.layout.item_live_channel, new ArrayList<>());
    }

    @Override
    protected void convert(BaseViewHolder helper, LiveChannel item) {
        TextView tvChannelNum = helper.getView(R.id.tvChannelNum);
        TextView tvChannel = helper.getView(R.id.tvChannel);
        tvChannelNum.setText(String.format("%s", item.getChannelNum()));
        tvChannel.setText(item.getChannelName());
        if (item.isDefault()) {
            tvChannelNum.setTextColor(mContext.getResources().getColor(R.color.color_1890FF));
            tvChannel.setTextColor(mContext.getResources().getColor(R.color.color_1890FF));
        } else {
            tvChannelNum.setTextColor(Color.WHITE);
            tvChannel.setTextColor(Color.WHITE);
        }
    }
}