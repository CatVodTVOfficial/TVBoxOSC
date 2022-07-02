package com.github.tvbox.osc.bean;

/**
 * @author pj567
 * @date :2021/1/12
 * @description:
 */
public class LiveSettingItem {
    private int itemIndex;
    private String itemName;
    private boolean itemSelected = false;

    public int getItemIndex() {
        return itemIndex;
    }

    public void setItemIndex(int itemIndex) {
        this.itemIndex = itemIndex;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public boolean isItemSelected() {
        return itemSelected;
    }

    public void setItemSelected(boolean itemSelected) {
        this.itemSelected = itemSelected;
    }
}