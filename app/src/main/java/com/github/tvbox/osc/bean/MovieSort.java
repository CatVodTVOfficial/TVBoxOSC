package com.github.tvbox.osc.bean;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author pj567
 * @date :2020/12/18
 * @description:
 */
@XStreamAlias("class")
public class MovieSort implements Serializable {
    @XStreamImplicit(itemFieldName = "ty")
    public List<SortData> sortList;

    @XStreamAlias("ty")
    @XStreamConverter(value = ToAttributedValueConverter.class, strings = {"name"})
    public static class SortData implements Serializable, Comparable<SortData> {
        @XStreamAsAttribute
        public String id;
        public String name;
        public int sort = -1;
        public boolean select = false;
        public ArrayList<SortFilter> filters = new ArrayList<>();
        public HashMap<String, String> filterSelect = new HashMap<>();

        public SortData() {
        }

        public SortData(String id, String name) {
            this.id = id;
            this.name = name;
        }


        @Override
        public int compareTo(SortData o) {
            return this.sort - o.sort;
        }
    }

    public static class SortFilter {
        public String key;
        public String name;
        public LinkedHashMap<String, String> values;
    }

}