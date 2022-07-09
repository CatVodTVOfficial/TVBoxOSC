package com.github.tvbox.osc.ui.adapter;

import android.graphics.Color;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.LiveSettingItem;

import java.util.ArrayList;


/**
 * @author pj567
 * @date :2021/1/12
 * @description:
 */
public class LiveSettingItemAdapter extends BaseQuickAdapter<LiveSettingItem, BaseViewHolder> {
    private int focusedItemIndex = -1;

    public LiveSettingItemAdapter() {
        super(R.layout.item_live_setting, new ArrayList<>());
    }

    @Override
    protected void convert(BaseViewHolder holder, LiveSettingItem item) {
        TextView tvItemName = holder.getView(R.id.tvSettingItemName);
        tvItemName.setText(item.getItemName());
        int itemIndex = item.getItemIndex();
        if (item.isItemSelected() && itemIndex != focusedItemIndex) {
            tvItemName.setTextColor(mContext.getResources().getColor(R.color.color_1890FF));
        } else {
            tvItemName.setTextColor(Color.WHITE);
        }
    }

    public void selectItem(int selectedItemIndex, boolean select, boolean unselectPreItemIndex) {
        if (unselectPreItemIndex) {
            int preSelectedItemIndex = getSelectedItemIndex();
            if (preSelectedItemIndex != -1) {
                getData().get(preSelectedItemIndex).setItemSelected(false);
                notifyItemChanged(preSelectedItemIndex);
            }
        }
        if (selectedItemIndex != -1) {
            getData().get(selectedItemIndex).setItemSelected(select);
            notifyItemChanged(selectedItemIndex);
        }
    }

    public void setFocusedItemIndex(int focusedItemIndex) {
        int preFocusItemIndex = this.focusedItemIndex;
        this.focusedItemIndex = focusedItemIndex;
        if (preFocusItemIndex != -1)
            notifyItemChanged(preFocusItemIndex);
        if (this.focusedItemIndex != -1)
            notifyItemChanged(this.focusedItemIndex);
    }

    public int getSelectedItemIndex() {
        for (LiveSettingItem item : getData()) {
            if (item.isItemSelected())
                return item.getItemIndex();
        }
        return -1;
    }
}