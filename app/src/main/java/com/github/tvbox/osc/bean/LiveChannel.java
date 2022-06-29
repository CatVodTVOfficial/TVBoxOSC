package com.github.tvbox.osc.bean;

import java.util.ArrayList;

/**
 * @author pj567
 * @date :2021/1/12
 * @description:
 */
public class LiveChannel {
    /**
     * channelNum : 1
     * channelName : CCTV-1 综合
     * channelUrl : http://117.148.187.37/PLTV/88888888/224/3221226154/index.m3u8
     * channelLogo : https://upload.wikimedia.org/wikipedia/zh/6/65/CCTV-1_Logo.png
     */

    private int channelNum;
    private String channelName;
    private ArrayList<String> channelUrls;
    private boolean isSelected = false;
    private boolean isFocused = false;
    public int sourceIndex = 0;
    public int sourceNum = 0;

    public void setChannelNum(int channelNum) {
        this.channelNum = channelNum;
    }

    public int getChannelNum() {
        return channelNum;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelName() {
        return channelName;
    }

    public ArrayList<String> getChannelUrls() { return channelUrls; }

    public void setChannelUrls(ArrayList<String> channelUrls) {
        this.channelUrls = channelUrls;
        sourceNum = channelUrls.size();
    }
    public void preSource() {
        sourceIndex--;
        if (sourceIndex < 0) sourceIndex = sourceNum - 1;
    }
    public void nextSource() {
        sourceIndex++;
        if (sourceIndex == sourceNum) sourceIndex = 0;
    }

    public int getSourceIndex() {
        return sourceIndex;
    }

    public String getUrl() {
        return channelUrls.get(sourceIndex);
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean b) {
        isSelected = b;
    }

    public int getSourceNum() { return sourceNum; }

    public boolean isFocused() {
        return isFocused;
    }

    public void setFocused(boolean focused) {
        isFocused = focused;
    }
}