package com.github.tvbox.osc.bean;

import java.util.ArrayList;

public class LiveSettingGroup {
    private int groupIndex;
    private String groupName;
    private ArrayList<LiveSettingItem> liveSettingItems;

    public int getGroupIndex() {
        return groupIndex;
    }

    public void setGroupIndex(int groupIndex) {
        this.groupIndex = groupIndex;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public ArrayList<LiveSettingItem> getLiveSettingItems() {
        return liveSettingItems;
    }

    public void setLiveSettingItems(ArrayList<LiveSettingItem> liveSettingItems) {
        this.liveSettingItems = liveSettingItems;
    }
}
