package com.github.tvbox.osc.bean;

import android.util.Base64;

import com.github.tvbox.osc.util.DefaultConfig;

/**
 * @author pj567
 * @date :2021/3/8
 * @description:
 */
public class ParseBean {

    private String name;
    private String url;
    private String ext;
    private int type;   // 0 普通嗅探 1 json 2 Json扩展 3 聚合

    private boolean isDefault = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return DefaultConfig.checkReplaceProxy(url);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean b) {
        isDefault = b;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String mixUrl() {
        if (!ext.isEmpty()) {
            int idx = url.indexOf("?");
            if (idx > 0) {
                return url.substring(0, idx + 1) + "cat_ext=" + Base64.encodeToString(ext.getBytes(), Base64.DEFAULT | Base64.URL_SAFE | Base64.NO_WRAP) + "&" + url.substring(idx + 1);
            }
        }
        return url;
    }
}