package com.github.tvbox.osc.bean;

import java.util.ArrayList;

public class ChannelGroup {
    /**
     * num : 1
     * name : 央视频道
     * password : 频道密码
     */
    private int groupNum;
    private String groupName;
    private String groupPassword;
    private ArrayList<LiveChannel> liveChannels;
    private boolean isSelected = false;
    private boolean isFocused = false;


    public int getGroupNum() {
        return groupNum;
    }

    public void setGroupNum(int groupNum) {
        this.groupNum = groupNum;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public ArrayList<LiveChannel> getLiveChannels() {
        return liveChannels;
    }

    public void setLiveChannels(ArrayList<LiveChannel> liveChannels) {
        this.liveChannels = liveChannels;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isFocused() {
        return isFocused;
    }

    public void setFocused(boolean focused) {
        isFocused = focused;
    }
}
