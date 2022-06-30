package com.github.tvbox.osc.bean;

import com.github.tvbox.osc.api.ApiConfig;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * @author pj567
 * @date :2020/12/22
 * @description:
 */
public class VodInfo implements Serializable {
    public String last;//时间
    //内容id
    public String id;
    //父级id
    public int tid;
    //影片名称 <![CDATA[老爸当家]]>
    public String name;
    //类型名称
    public String type;
    //视频分类zuidam3u8,zuidall
    public String dt;
    //图片
    public String pic;
    //语言
    public String lang;
    //地区
    public String area;
    //年份
    public int year;
    public String state;
    //描述集数或者影片信息<![CDATA[共40集]]>
    public String note;
    //演员<![CDATA[张国立,蒋欣,高鑫,曹艳艳,王维维,韩丹彤,孟秀,王新]]>
    public String actor;
    //导演<![CDATA[陈国星]]>
    public String director;
    public ArrayList<VodSeriesFlag> seriesFlags;
    public LinkedHashMap<String, List<VodSeries>> seriesMap;
    public String des;// <![CDATA[权来]
    public String playFlag = null;
    public int playIndex = 0;
    public String sourceKey;
    public String playerCfg = "";
    public boolean reverseSort = false;

    public void setVideo(Movie.Video video) {
        last = video.last;
        id = video.id;
        tid = video.tid;
        name = video.name;
        type = video.type;
        // dt = video.dt;
        pic = video.pic;
        lang = video.lang;
        area = video.area;
        year = video.year;
        state = video.state;
        note = video.note;
        actor = video.actor;
        director = video.director;
        des = video.des;
        if (video.urlBean != null && video.urlBean.infoList != null && video.urlBean.infoList.size() > 0) {
            LinkedHashMap<String, List<VodSeries>> tempSeriesMap = new LinkedHashMap<>();
            seriesFlags = new ArrayList<>();
            for (Movie.Video.UrlBean.UrlInfo urlInfo : video.urlBean.infoList) {
                if (urlInfo.beanList != null && urlInfo.beanList.size() > 0) {
                    List<VodSeries> seriesList = new ArrayList<>();
                    for (Movie.Video.UrlBean.UrlInfo.InfoBean infoBean : urlInfo.beanList) {
                        seriesList.add(new VodSeries(infoBean.name, infoBean.url));
                    }
                    tempSeriesMap.put(urlInfo.flag, seriesList);
                    seriesFlags.add(new VodSeriesFlag(urlInfo.flag));
                }
            }
            SourceBean sb = ApiConfig.get().getSource(video.sourceKey);
            if (sb != null) { // ssp 不排序
                // 优先展示m3u8
                Collections.sort(seriesFlags, new Comparator<VodSeriesFlag>() {
                    final String PREFIX = "m3u8";

                    @Override
                    public int compare(VodSeriesFlag a, VodSeriesFlag b) {
                        if (a.name.contains(PREFIX) && b.name.contains(PREFIX))
                            return a.name.compareTo(b.name);
                        if (a.name.contains(PREFIX) && !b.name.contains(PREFIX)) return -1;
                        if (!a.name.contains(PREFIX) && b.name.contains(PREFIX)) return 1;
                        return 0;
                    }
                });
            }
            seriesMap = new LinkedHashMap<>();
            for (VodSeriesFlag flag : seriesFlags) {
                seriesMap.put(flag.name, tempSeriesMap.get(flag.name));
            }
        }
    }

    public void reverse() {
        Set<String> flags = seriesMap.keySet();
        for (String flag : flags) {
            Collections.reverse(seriesMap.get(flag));
        }
    }

    public static class VodSeriesFlag implements Serializable {

        public String name;
        public boolean selected;

        public VodSeriesFlag() {

        }

        public VodSeriesFlag(String name) {
            this.name = name;
        }
    }

    public static class VodSeries implements Serializable {

        public String name;
        public String url;
        public boolean selected;

        public VodSeries() {
        }

        public VodSeries(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }
}