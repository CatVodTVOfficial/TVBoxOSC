package com.github.tvbox.osc.api;

import android.app.Activity;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;

import com.github.catvod.crawler.JarLoader;
import com.github.catvod.crawler.Spider;
import com.github.tvbox.osc.bean.IJKCode;
import com.github.tvbox.osc.bean.LiveChannel;
import com.github.tvbox.osc.bean.ParseBean;
import com.github.tvbox.osc.bean.SourceBean;
import com.github.tvbox.osc.cache.RoomDataManger;
import com.github.tvbox.osc.cache.SourceState;
import com.github.tvbox.osc.server.ControlManager;
import com.github.tvbox.osc.util.AdBlocker;
import com.github.tvbox.osc.util.DefaultConfig;
import com.github.tvbox.osc.util.HawkConfig;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;
import com.orhanobut.hawk.Hawk;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author pj567
 * @date :2020/12/18
 * @description:
 */
public class ApiConfig {
    private static ApiConfig instance;
    private List<SourceBean> sourceBeanList;
    private SourceBean mHomeSource;
    private ParseBean mDefaultParse;
    private List<LiveChannel> channelList;
    private List<ParseBean> parseBeanList;
    private List<String> vipParseFlags;
    private List<IJKCode> ijkCodes;
    private String spider = null;

    private SourceBean emptyHome = new SourceBean();

    private JarLoader jarLoader = new JarLoader();


    private ApiConfig() {
        sourceBeanList = new ArrayList<>();
        channelList = new ArrayList<>();
        parseBeanList = new ArrayList<>();
    }

    public static ApiConfig get() {
        if (instance == null) {
            synchronized (ApiConfig.class) {
                if (instance == null) {
                    instance = new ApiConfig();
                }
            }
        }
        return instance;
    }

    public void loadConfig(LoadConfigCallback callback, Activity activity) {
        /*boolean isSourceModeLocal = Hawk.get(HawkConfig.SOURCE_MODE_LOCAL, false);
        if (isSourceModeLocal) {
            loadConfigLocal(callback, activity);
        } else {
            loadConfigServer(callback, activity);
        }*/
        loadConfigServer(callback, activity);
    }


    public void loadJar(String spider, LoadConfigCallback callback) {
        OkGo.<byte[]>get(spider).execute(new AbsCallback<byte[]>() {
            @Override
            public byte[] convertResponse(okhttp3.Response response) {
                try {
                    return response.body().bytes();
                } catch (Throwable th) {
                    return null;
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
                callback.success();
            }

            @Override
            public void onSuccess(Response<byte[]> response) {
                if (response != null && response.body() != null) {
                    jarLoader.load(response.body());
                }
            }
        });
    }

    private void loadConfigServer(LoadConfigCallback callback, Activity activity) {
        String apiUrl = Hawk.get(HawkConfig.API_URL, "");
        String apiFix = apiUrl;
        if (apiUrl.startsWith("clan://")) {
            apiFix = clanToAddress(apiUrl);
        }
        OkGo.<String>get(apiFix)
                .execute(new AbsCallback<String>() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            parseJson(apiUrl, response.body());
                            callback.success();
                        } catch (Throwable th) {
                            th.printStackTrace();
                            callback.error("解析配置失败");
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        callback.error("拉取配置失败");
                    }

                    public String convertResponse(okhttp3.Response response) throws Throwable {
                        String result = "";
                        if (response.body() == null) {
                            result = "";
                        } else {
                            result = response.body().string();
                        }
                        if (apiUrl.startsWith("clan")) {
                            result = clanContentFix(clanToAddress(apiUrl), result);
                        }
                        return result;
                    }
                });
    }

    private void parseJson(String apiUrl, String jsonStr) {
        JsonObject infoJson = new Gson().fromJson(jsonStr, JsonObject.class);
        // spider
        spider = DefaultConfig.safeJsonString(infoJson, "spider", "");
        spider = spider.split(";md5;")[0];
        // 远端站点源
        for (JsonElement opt : infoJson.get("sites").getAsJsonArray()) {
            JsonObject obj = (JsonObject) opt;
            SourceBean sb = new SourceBean();
            sb.setKey(obj.get("key").getAsString().trim());
            sb.setName(obj.get("name").getAsString().trim());
            sb.setType(obj.get("type").getAsInt());
            sb.setApi(obj.get("api").getAsString().trim());
            sb.setSearchable(DefaultConfig.safeJsonInt(obj, "searchable", 1));
            sb.setSearchable(DefaultConfig.safeJsonInt(obj, "quickSearch", 1));
            sb.setFilterable(DefaultConfig.safeJsonInt(obj, "filterable", 1));
            sb.setPlayerUrl(DefaultConfig.safeJsonString(obj, "playUrl", ""));
            sb.setExt(DefaultConfig.safeJsonString(obj, "ext", ""));
            sourceBeanList.add(sb);
        }
        if (sourceBeanList != null && sourceBeanList.size() > 0) {
            // 获取启用状态
            HashMap<String, SourceState> sourceStates = RoomDataManger.getAllSourceState();
            for (SourceBean sb : sourceBeanList) {
                if (sourceStates.containsKey(sb.getKey()))
                    sb.setState(sourceStates.get(sb.getKey()));
                if (sb.isHome())
                    setSourceBean(sb);
            }
            // 如果没有home source 使用第一个
            if (mHomeSource == null)
                setSourceBean(sourceBeanList.get(0));
        }
        // 需要使用vip解析的flag
        vipParseFlags = DefaultConfig.safeJsonStringList(infoJson, "flags");
        // 解析地址
        for (JsonElement opt : infoJson.get("parses").getAsJsonArray()) {
            JsonObject obj = (JsonObject) opt;
            ParseBean pb = new ParseBean();
            pb.setName(obj.get("name").getAsString().trim());
            pb.setUrl(obj.get("url").getAsString().trim());
            String ext = obj.has("ext") ? obj.get("ext").getAsJsonObject().toString() : "";
            pb.setExt(ext);
            pb.setType(DefaultConfig.safeJsonInt(obj, "type", 0));
            parseBeanList.add(pb);
        }
        // 获取默认解析
        if (parseBeanList != null && parseBeanList.size() > 0) {
            String defaultParse = Hawk.get(HawkConfig.DEFAULT_PARSE, "");
            if (!TextUtils.isEmpty(defaultParse))
                for (ParseBean pb : parseBeanList) {
                    if (pb.getName().equals(defaultParse))
                        setDefaultParse(pb);
                }
            if (mDefaultParse == null)
                setDefaultParse(parseBeanList.get(0));
        }
        // 直播源
        try {
            int lcIdx = 0;
            for (JsonElement opt : infoJson.get("lives").getAsJsonArray()) {
                for (JsonElement optChl : ((JsonObject) opt).get("channels").getAsJsonArray()) {
                    JsonObject obj = (JsonObject) optChl;
                    LiveChannel lc = new LiveChannel();
                    lc.setName(obj.get("name").getAsString().trim());
                    ArrayList<String> urls = DefaultConfig.safeJsonStringList(obj, "urls");
                    if (urls.size() > 0) {
                        String url = urls.get(0);
                        if (url.startsWith("proxy://")) {
                            String fix = url.replace("proxy://", "http://0.0.0.0/?");
                            String extUrl = Uri.parse(fix).getQueryParameter("ext");
                            if (extUrl != null && !extUrl.isEmpty()) {
                                String extUrlFix = new String(Base64.decode(extUrl, Base64.DEFAULT | Base64.URL_SAFE | Base64.NO_WRAP), "UTF-8");
                                if (extUrlFix.startsWith("clan://")) {
                                    extUrlFix = clanContentFix(clanToAddress(apiUrl), extUrlFix);
                                    extUrlFix = Base64.encodeToString(extUrlFix.getBytes("UTF-8"), Base64.DEFAULT | Base64.URL_SAFE | Base64.NO_WRAP);
                                    fix = url.replace(extUrl, extUrlFix);
                                    urls.set(0, fix);
                                }
                            }
                        }
                    }
                    lc.setUrls(urls);
                    // 暂时不考虑分组问题
                    lc.setChannelNum(lcIdx++);
                    channelList.add(lc);
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        // 广告地址
        for (JsonElement host : infoJson.getAsJsonArray("ads")) {
            AdBlocker.addAdHost(host.getAsString());
        }
        // IJK解码配置
        boolean foundOldSelect = false;
        String ijkCodec = Hawk.get(HawkConfig.IJK_CODEC, "");
        ijkCodes = new ArrayList<>();
        for (JsonElement opt : infoJson.get("ijk").getAsJsonArray()) {
            JsonObject obj = (JsonObject) opt;
            String name = obj.get("group").getAsString();
            LinkedHashMap<String, String> baseOpt = new LinkedHashMap<>();
            for (JsonElement cfg : obj.get("options").getAsJsonArray()) {
                JsonObject cObj = (JsonObject) cfg;
                String key = cObj.get("category").getAsString() + "|" + cObj.get("name").getAsString();
                String val = cObj.get("value").getAsString();
                baseOpt.put(key, val);
            }
            IJKCode codec = new IJKCode();
            codec.setName(name);
            codec.setOption(baseOpt);
            if (name.equals(ijkCodec) || TextUtils.isEmpty(ijkCodec)) {
                codec.selected(true);
                ijkCodec = name;
                foundOldSelect = true;
            } else {
                codec.selected(false);
            }
            ijkCodes.add(codec);
        }
        if (!foundOldSelect && ijkCodes.size() > 0) {
            ijkCodes.get(0).selected(true);
        }
    }

    public String getSpider() {
        return spider;
    }

    public Spider getCSP(SourceBean sourceBean) {
        return jarLoader.getSpider(sourceBean.getApi(), sourceBean.getExt());
    }

    public Object[] proxyLocal(Map param) {
        return jarLoader.proxyInvoke(param);
    }

    public JSONObject jsonExt(String key, LinkedHashMap<String, String> jxs, String url) {
        return jarLoader.jsonExt(key, jxs, url);
    }

    public JSONObject jsonExtMix(String flag, String key, String name, LinkedHashMap<String, HashMap<String, String>> jxs, String url) {
        return jarLoader.jsonExtMix(flag, key, name, jxs, url);
    }

    public interface LoadConfigCallback {
        void success();

        void retry();

        void error(String msg);
    }

    public interface FastParseCallback {
        void success(boolean parse, String url, Map<String, String> header);

        void fail(int code, String msg);
    }

    public SourceBean getSource(String key) {
        for (SourceBean bean : sourceBeanList) {
            if (bean.getKey().equals(key))
                return bean;
        }
        return null;
    }

    public void setSourceBean(SourceBean sourceBean) {
        if (this.mHomeSource != null)
            this.mHomeSource.setHome(false);
        this.mHomeSource = sourceBean;
        sourceBean.setHome(true);
    }

    public void setDefaultParse(ParseBean parseBean) {
        if (this.mDefaultParse != null)
            this.mDefaultParse.setDefault(false);
        this.mDefaultParse = parseBean;
        Hawk.put(HawkConfig.DEFAULT_PARSE, parseBean.getName());
        parseBean.setDefault(true);
    }

    public ParseBean getDefaultParse() {
        return mDefaultParse;
    }

    public List<SourceBean> getSourceBeanList() {
        return sourceBeanList;
    }

    public List<ParseBean> getParseBeanList() {
        return parseBeanList;
    }

    public List<String> getVipParseFlags() {
        return vipParseFlags;
    }

    public SourceBean getHomeSourceBean() {
        return mHomeSource == null ? emptyHome : mHomeSource;
    }

    public List<LiveChannel> getChannelList() {
        return channelList;
    }

    public List<IJKCode> getIjkCodes() {
        return ijkCodes;
    }

    public IJKCode getCurrentIJKCode() {
        String codeName = Hawk.get(HawkConfig.IJK_CODEC, "");
        for (IJKCode code : ijkCodes) {
            if (code.getName().equals(codeName))
                return code;
        }
        return ijkCodes.get(0);
    }

    String clanToAddress(String lanLink) {
        if (lanLink.startsWith("clan://localhost/")) {
            return lanLink.replace("clan://localhost/", ControlManager.get().getAddress(true) + "file/");
        } else {
            String link = lanLink.substring(7);
            int end = link.indexOf('/');
            return "http://" + link.substring(0, end) + "/file/" + link.substring(end + 1);
        }
    }

    String clanContentFix(String lanLink, String content) {
        String fix = lanLink.substring(0, lanLink.indexOf("/file/") + 6);
        return content.replace("clan://", fix);
    }
}