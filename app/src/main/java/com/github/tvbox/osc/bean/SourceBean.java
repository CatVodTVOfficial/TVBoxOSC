package com.github.tvbox.osc.bean;

import com.github.tvbox.osc.cache.RoomDataManger;
import com.github.tvbox.osc.cache.SourceState;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;

/**
 * @author pj567
 * @date :2020/12/18
 * @description:
 */
public class SourceBean {
    /**
     * name : 最大资源网
     * api : http://www.zdziyuan.com/inc/api.php
     * download : http://www.zdziyuan.com/inc/apidown.php
     */
    private String key;
    private String name;
    private String api;
    private int type;   // 0 xml 1 json 3 Spider
    private int searchable; // 是否可搜索
    private int quickSearch; // 是否可以快速搜索
    private int filterable; // 可筛选?
    private String playerUrl; // 站点解析Url
    private String ext; // 扩展数据
    private SourceState state;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public void setPlayerUrl(String playerUrl) {
        this.playerUrl = playerUrl;
    }

    public String getPlayerUrl() {
        return playerUrl;
    }

    public void setState(SourceState state) {
        this.state = state;
    }

    public SourceState getState() {
        if (state == null) {
            state = new SourceState();
            state.sourceKey = getKey();
            state.active = true;
            state.home = false;
        }
        return state;
    }

    public boolean isHome() {
        return getState().home;
    }

    public void setHome(boolean home) {
        getState().home = home;
        RoomDataManger.addSourceState(getState());
    }

    public boolean isActive() {
        return getState().active;
    }

    public void setActive(boolean act) {
        getState().active = act;
        RoomDataManger.addSourceState(getState());
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setTidSort(HashMap<String, Integer> tidSort) {
        getState().tidSort = new Gson().toJson(tidSort);
        RoomDataManger.addSourceState(getState());
    }

    public HashMap<String, Integer> getTidSort() {
        return new Gson().fromJson(state.tidSort, new TypeToken<HashMap<String, Integer>>() {
        }.getType());
    }

    public int getSearchable() {
        return searchable;
    }

    public void setSearchable(int searchable) {
        this.searchable = searchable;
    }

    public int getQuickSearch() {
        return quickSearch;
    }

    public void setQuickSearch(int quickSearch) {
        this.quickSearch = quickSearch;
    }

    public int getFilterable() {
        return filterable;
    }

    public void setFilterable(int filterable) {
        this.filterable = filterable;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }
}