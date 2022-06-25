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
    private ArrayList<String> urls;
    private boolean isDefault;
    public int sourceIdx = 0;

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

    public String getUrls() {
        if (sourceIdx <= 0 || sourceIdx >= urls.size())
            sourceIdx = 0;
        return urls.get(sourceIdx);
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean b) {
        isDefault = b;
    }

    public void setUrls(ArrayList<String> urls) {
        this.urls = urls;
    }
}