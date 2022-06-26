package com.github.tvbox.osc.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.github.catvod.crawler.Spider;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.bean.AbsJson;
import com.github.tvbox.osc.bean.AbsSortJson;
import com.github.tvbox.osc.bean.AbsSortXml;
import com.github.tvbox.osc.bean.AbsXml;
import com.github.tvbox.osc.bean.Movie;
import com.github.tvbox.osc.bean.SourceBean;
import com.github.tvbox.osc.event.RefreshEvent;
import com.github.tvbox.osc.util.DefaultConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author pj567
 * @date :2020/12/18
 * @description:
 */
public class SourceViewModel extends ViewModel {
    public MutableLiveData<AbsSortXml> sortResult;
    public MutableLiveData<AbsXml> listResult;
    public MutableLiveData<AbsXml> searchResult;
    public MutableLiveData<AbsXml> quickSearchResult;
    public MutableLiveData<AbsXml> detailResult;
    public MutableLiveData<JSONObject> playResult;

    public SourceViewModel() {
        sortResult = new MutableLiveData<>();
        listResult = new MutableLiveData<>();
        searchResult = new MutableLiveData<>();
        quickSearchResult = new MutableLiveData<>();
        detailResult = new MutableLiveData<>();
        playResult = new MutableLiveData<>();
    }

    public static final ExecutorService spThreadPool = Executors.newFixedThreadPool(3);

    public void getSort(String sourceKey) {
        if (sourceKey == null) {
            sortResult.postValue(null);
            return;
        }
        SourceBean sourceBean = ApiConfig.get().getSource(sourceKey);
        int type = sourceBean.getType();
        if (type == 3) {
            spThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    Spider sp = ApiConfig.get().getCSP(sourceBean);
                    sortJson(sortResult, sp.homeContent(false));
                }
            });
        } else if (type == 0 || type == 1) {
            OkGo.<String>get(sourceBean.getApi())
                    .tag(sourceBean.getKey() + "_sort")
                    .execute(new AbsCallback<String>() {
                        @Override
                        public String convertResponse(okhttp3.Response response) throws Throwable {
                            if (response.body() != null) {
                                return response.body().string();
                            } else {
                                throw new IllegalStateException("网络请求错误");
                            }
                        }

                        @Override
                        public void onSuccess(Response<String> response) {
                            if (type == 0) {
                                String xml = response.body();
                                sortXml(sortResult, xml);
                            } else if (type == 1) {
                                String json = response.body();
                                sortJson(sortResult, json);
                            }
                        }

                        @Override
                        public void onError(Response<String> response) {
                            super.onError(response);
                            sortResult.postValue(null);
                        }
                    });
        } else {
            sortResult.postValue(null);
        }
    }

    public void getList(String id, int page) {
        SourceBean homeSourceBean = ApiConfig.get().getHomeSourceBean();
        int type = homeSourceBean.getType();
        if (type == 3) {
            spThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Spider sp = ApiConfig.get().getCSP(homeSourceBean);
                        json(listResult, sp.categoryContent(id, page + "", false, new HashMap<>()), homeSourceBean.getKey());
                    } catch (Throwable th) {
                        th.printStackTrace();
                    }
                }
            });
        } else if (type == 0 || type == 1) {
            OkGo.<String>get(homeSourceBean.getApi())
                    .tag(homeSourceBean.getApi())
                    .params("ac", type == 0 ? "videolist" : "detail")
                    .params("t", id)
                    .params("pg", page)
                    .execute(new AbsCallback<String>() {

                        @Override
                        public String convertResponse(okhttp3.Response response) throws Throwable {
                            if (response.body() != null) {
                                return response.body().string();
                            } else {
                                throw new IllegalStateException("网络请求错误");
                            }
                        }

                        @Override
                        public void onSuccess(Response<String> response) {
                            if (type == 0) {
                                String xml = response.body();
                                xml(listResult, xml, homeSourceBean.getKey());
                            } else {
                                String json = response.body();
                                json(listResult, json, homeSourceBean.getKey());
                            }
                        }

                        @Override
                        public void onError(Response<String> response) {
                            super.onError(response);
                            listResult.postValue(null);
                        }
                    });
        } else {
            listResult.postValue(null);
        }
    }

    public void getDetail(String sourceKey, String id) {
        SourceBean sourceBean = ApiConfig.get().getSource(sourceKey);
        int type = sourceBean.getType();
        if (type == 3) {
            spThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Spider sp = ApiConfig.get().getCSP(sourceBean);
                        List<String> ids = new ArrayList<>();
                        ids.add(id);
                        json(detailResult, sp.detailContent(ids), sourceBean.getKey());
                    } catch (Throwable th) {
                        th.printStackTrace();
                    }
                }
            });
        } else if (type == 0 || type == 1) {
            OkGo.<String>get(sourceBean.getApi())
                    .tag("detail")
                    .params("ac", type == 0 ? "videolist" : "detail")
                    .params("ids", id)
                    .execute(new AbsCallback<String>() {

                        @Override
                        public String convertResponse(okhttp3.Response response) throws Throwable {
                            if (response.body() != null) {
                                return response.body().string();
                            } else {
                                throw new IllegalStateException("网络请求错误");
                            }
                        }

                        @Override
                        public void onSuccess(Response<String> response) {
                            if (type == 0) {
                                String xml = response.body();
                                xml(detailResult, xml, sourceBean.getKey());
                            } else {
                                String json = response.body();
                                json(detailResult, json, sourceBean.getKey());
                            }
                        }

                        @Override
                        public void onError(Response<String> response) {
                            super.onError(response);
                            detailResult.postValue(null);
                        }
                    });
        } else {
            detailResult.postValue(null);
        }
    }

    public void getSearch(String sourceKey, String wd) {
        SourceBean sourceBean = ApiConfig.get().getSource(sourceKey);
        int type = sourceBean.getType();
        if (type == 3) {
            try {
                Spider sp = ApiConfig.get().getCSP(sourceBean);
                json(searchResult, sp.searchContent(wd, false), sourceBean.getKey());
            } catch (Throwable th) {
                th.printStackTrace();
            }
        } else if (type == 0 || type == 1) {
            OkGo.<String>get(sourceBean.getApi())
                    .params("wd", wd)
                    .params(type == 1 ? "ac" : null, type == 1 ? "detail" : null)
                    .tag("search")
                    .execute(new AbsCallback<String>() {
                        @Override
                        public String convertResponse(okhttp3.Response response) throws Throwable {
                            if (response.body() != null) {
                                return response.body().string();
                            } else {
                                throw new IllegalStateException("网络请求错误");
                            }
                        }

                        @Override
                        public void onSuccess(Response<String> response) {
                            if (type == 0) {
                                String xml = response.body();
                                xml(searchResult, xml, sourceBean.getKey());
                            } else {
                                String json = response.body();
                                json(searchResult, json, sourceBean.getKey());
                            }
                        }

                        @Override
                        public void onError(Response<String> response) {
                            super.onError(response);
                            // searchResult.postValue(null);
                            EventBus.getDefault().post(new RefreshEvent(RefreshEvent.TYPE_SEARCH_RESULT, null));
                        }
                    });
        } else {
            searchResult.postValue(null);
        }
    }

    public void getQuickSearch(String sourceKey, String wd) {
        SourceBean sourceBean = ApiConfig.get().getSource(sourceKey);
        int type = sourceBean.getType();
        if (type == 3) {
            try {
                Spider sp = ApiConfig.get().getCSP(sourceBean);
                json(quickSearchResult, sp.searchContent(wd, true), sourceBean.getKey());
            } catch (Throwable th) {
                th.printStackTrace();
            }
        } else if (type == 0 || type == 1) {
            OkGo.<String>get(sourceBean.getApi())
                    .params("wd", wd)
                    .params(type == 1 ? "ac" : null, type == 1 ? "detail" : null)
                    .tag("quick_search")
                    .execute(new AbsCallback<String>() {
                        @Override
                        public String convertResponse(okhttp3.Response response) throws Throwable {
                            if (response.body() != null) {
                                return response.body().string();
                            } else {
                                throw new IllegalStateException("网络请求错误");
                            }
                        }

                        @Override
                        public void onSuccess(Response<String> response) {
                            if (type == 0) {
                                String xml = response.body();
                                xml(quickSearchResult, xml, sourceBean.getKey());
                            } else {
                                String json = response.body();
                                json(quickSearchResult, json, sourceBean.getKey());
                            }
                        }

                        @Override
                        public void onError(Response<String> response) {
                            super.onError(response);
                            // quickSearchResult.postValue(null);
                            EventBus.getDefault().post(new RefreshEvent(RefreshEvent.TYPE_QUICK_SEARCH_RESULT, null));
                        }
                    });
        } else {
            quickSearchResult.postValue(null);
        }
    }

    public void getPlay(String sourceKey, String playFlag, String progressKey, String url) {
        SourceBean sourceBean = ApiConfig.get().getSource(sourceKey);
        int type = sourceBean.getType();
        if (type == 3) {
            spThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    Spider sp = ApiConfig.get().getCSP(sourceBean);
                    String json = sp.playerContent(playFlag, url, ApiConfig.get().getVipParseFlags());
                    try {
                        JSONObject result = new JSONObject(json);
                        result.put("key", url);
                        result.put("proKey", progressKey);
                        if (!result.has("flag"))
                            result.put("flag", playFlag);
                        playResult.postValue(result);
                    } catch (Throwable th) {
                        th.printStackTrace();
                        playResult.postValue(null);
                    }
                }
            });
        } else if (type == 0 || type == 1) {
            JSONObject result = new JSONObject();
            try {
                result.put("key", url);
                String playUrl = sourceBean.getPlayerUrl().trim();
                if (DefaultConfig.isVideoFormat(url)) {
                    result.put("parse", 0);
                    result.put("url", url);
                } else {
                    result.put("parse", 1);
                    result.put("url", url);
                }
                result.put("playUrl", playUrl);
                result.put("flag", playFlag);
                playResult.postValue(result);
            } catch (Throwable th) {
                th.printStackTrace();
                playResult.postValue(null);
            }
        } else {
            quickSearchResult.postValue(null);
        }
    }

    private void sortJson(MutableLiveData<AbsSortXml> result, String json) {
        try {
            AbsSortJson sortJson = new Gson().fromJson(json, new TypeToken<AbsSortJson>() {
            }.getType());
            AbsSortXml data = sortJson.toAbsSortXml();
            result.postValue(data);
        } catch (Exception e) {
            result.postValue(null);
        }
    }

    private void sortXml(MutableLiveData<AbsSortXml> result, String xml) {
        try {
            XStream xstream = new XStream(new DomDriver());//创建Xstram对象
            xstream.autodetectAnnotations(true);
            xstream.processAnnotations(AbsSortXml.class);
            xstream.ignoreUnknownElements();
            AbsSortXml data = (AbsSortXml) xstream.fromXML(xml);
            result.postValue(data);
        } catch (Exception e) {
            result.postValue(null);
        }
    }

    private void absXml(AbsXml data, String sourceKey) {
        if (data.movie != null && data.movie.videoList != null) {
            for (Movie.Video video : data.movie.videoList) {
                if (video.urlBean != null && video.urlBean.infoList != null) {
                    for (Movie.Video.UrlBean.UrlInfo urlInfo : video.urlBean.infoList) {
                        String[] str = null;
                        if (urlInfo.urls.contains("#")) {
                            str = urlInfo.urls.split("#");
                        } else {
                            str = new String[]{urlInfo.urls};
                        }
                        List<Movie.Video.UrlBean.UrlInfo.InfoBean> infoBeanList = new ArrayList<>();
                        for (String s : str) {
                            if (s.contains("$")) {
                                String[] ss = s.split("\\$");
                                if (ss.length >= 2) {
                                    infoBeanList.add(new Movie.Video.UrlBean.UrlInfo.InfoBean(ss[0], ss[1]));
                                }
                                //infoBeanList.add(new Movie.Video.UrlBean.UrlInfo.InfoBean(s.substring(0, s.indexOf("$")), s.substring(s.indexOf("$") + 1)));
                            }
                        }
                        urlInfo.beanList = infoBeanList;
                    }
                }
                video.sourceKey = sourceKey;
            }
        }
    }

    private void xml(MutableLiveData<AbsXml> result, String xml, String sourceKey) {
        try {
            XStream xstream = new XStream(new DomDriver());//创建Xstram对象
            xstream.autodetectAnnotations(true);
            xstream.processAnnotations(AbsXml.class);
            xstream.ignoreUnknownElements();
            if (xml.contains("<year></year>")) {
                xml = xml.replace("<year></year>", "<year>0</year>");
            }
            if (xml.contains("<state></state>")) {
                xml = xml.replace("<state></state>", "<state>0</state>");
            }
            AbsXml data = (AbsXml) xstream.fromXML(xml);
            absXml(data, sourceKey);
            if (searchResult == result) {
                EventBus.getDefault().post(new RefreshEvent(RefreshEvent.TYPE_SEARCH_RESULT, data));
            } else if (quickSearchResult == result) {
                EventBus.getDefault().post(new RefreshEvent(RefreshEvent.TYPE_QUICK_SEARCH_RESULT, data));
            } else {
                result.postValue(data);
            }
        } catch (Exception e) {
            if (searchResult == result) {
                EventBus.getDefault().post(new RefreshEvent(RefreshEvent.TYPE_SEARCH_RESULT, null));
            } else if (quickSearchResult == result) {
                EventBus.getDefault().post(new RefreshEvent(RefreshEvent.TYPE_QUICK_SEARCH_RESULT, null));
            } else {
                result.postValue(null);
            }
        }
    }

    private void json(MutableLiveData<AbsXml> result, String json, String sourceKey) {
        try {
            AbsJson absJson = new Gson().fromJson(json, new TypeToken<AbsJson>() {
            }.getType());
            AbsXml data = absJson.toAbsXml();
            absXml(data, sourceKey);
            if (searchResult == result) {
                EventBus.getDefault().post(new RefreshEvent(RefreshEvent.TYPE_SEARCH_RESULT, data));
            } else if (quickSearchResult == result) {
                EventBus.getDefault().post(new RefreshEvent(RefreshEvent.TYPE_QUICK_SEARCH_RESULT, data));
            } else {
                result.postValue(data);
            }
        } catch (Exception e) {
            if (searchResult == result) {
                EventBus.getDefault().post(new RefreshEvent(RefreshEvent.TYPE_SEARCH_RESULT, null));
            } else if (quickSearchResult == result) {
                EventBus.getDefault().post(new RefreshEvent(RefreshEvent.TYPE_QUICK_SEARCH_RESULT, null));
            } else {
                result.postValue(null);
            }
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}