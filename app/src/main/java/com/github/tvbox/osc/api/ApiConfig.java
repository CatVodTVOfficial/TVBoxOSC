package com.github.tvbox.osc.api;

import android.app.Activity;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;

import com.github.catvod.crawler.JarLoader;
import com.github.catvod.crawler.Spider;
import com.github.tvbox.osc.base.App;
import com.github.tvbox.osc.bean.ChannelGroup;
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
import com.github.tvbox.osc.util.MD5;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;
import com.orhanobut.hawk.Hawk;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
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
    private List<ChannelGroup> channelGroupList;
    private List<ParseBean> parseBeanList;
    private List<String> vipParseFlags;
    private List<IJKCode> ijkCodes;
    private String spider = null;

    private SourceBean emptyHome = new SourceBean();

    private JarLoader jarLoader = new JarLoader();


    private ApiConfig() {
        sourceBeanList = new ArrayList<>();
        channelGroupList = new ArrayList<>();
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

    public void loadConfig(boolean useCache, LoadConfigCallback callback, Activity activity) {
        String apiUrl = Hawk.get(HawkConfig.API_URL, "https://raw.nite07.com/t/peizhi.json");
        if (apiUrl.isEmpty()) {
            callback.error("-1");
            return;
        }
        File cache = new File(App.getInstance().getFilesDir().getAbsolutePath() + "/" + MD5.encode(apiUrl));
        if (useCache && cache.exists()) {
            try {
                parseJson(apiUrl, cache);
                callback.success();
                return;
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
        String apiFix = apiUrl;
        if (apiUrl.startsWith("clan://")) {
            apiFix = clanToAddress(apiUrl);
        }
        OkGo.<String>get(apiFix)
                .execute(new AbsCallback<String>() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            String json = response.body();
                            parseJson(apiUrl, response.body());
                            try {
                                File cacheDir = cache.getParentFile();
                                if (!cacheDir.exists())
                                    cacheDir.mkdirs();
                                if (cache.exists())
                                    cache.delete();
                                FileOutputStream fos = new FileOutputStream(cache);
                                fos.write(json.getBytes("UTF-8"));
                                fos.flush();
                                fos.close();
                            } catch (Throwable th) {
                                th.printStackTrace();
                            }
                            callback.success();
                        } catch (Throwable th) {
                            th.printStackTrace();
                            callback.error("解析配置失败");
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        if (cache.exists()) {
                            try {
                                parseJson(apiUrl, cache);
                                callback.success();
                                return;
                            } catch (Throwable th) {
                                th.printStackTrace();
                            }
                        }
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


    public void loadJar(boolean useCache, String spider, LoadConfigCallback callback) {
        String[] urls = spider.split(";md5;");
        String jarUrl = urls[0];
        String md5 = urls.length > 1 ? urls[1].trim() : "";
        File cache = new File(App.getInstance().getFilesDir().getAbsolutePath() + "/csp.jar");

        if (!md5.isEmpty() || useCache) {
            if (cache.exists() && (useCache || MD5.getFileMd5(cache).equalsIgnoreCase(md5))) {
                if (jarLoader.load(cache.getAbsolutePath())) {
                    callback.success();
                } else {
                    callback.error("");
                }
                return;
            }
        }

        OkGo.<File>get(jarUrl).execute(new AbsCallback<File>() {

            @Override
            public File convertResponse(okhttp3.Response response) throws Throwable {
                File cacheDir = cache.getParentFile();
                if (!cacheDir.exists())
                    cacheDir.mkdirs();
                if (cache.exists())
                    cache.delete();
                FileOutputStream fos = new FileOutputStream(cache);
                fos.write(response.body().bytes());
                fos.flush();
                fos.close();
                return cache;
            }

            @Override
            public void onSuccess(Response<File> response) {
                if (response.body().exists()) {
                    if (jarLoader.load(response.body().getAbsolutePath())) {
                        callback.success();
                    } else {
                        callback.error("");
                    }
                } else {
                    callback.error("");
                }
            }
        });
    }

    private void parseJson(String apiUrl, File f) throws Throwable {
        System.out.println("从本地缓存加载" + f.getAbsolutePath());
        BufferedReader bReader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String s = "";
        while ((s = bReader.readLine()) != null) {
            sb.append(s + "\n");
        }
        bReader.close();
        parseJson(apiUrl, sb.toString());
    }

    private void parseJson(String apiUrl, String jsonStr) {
        JsonObject infoJson = new Gson().fromJson(jsonStr, JsonObject.class);
        // spider
        spider = DefaultConfig.safeJsonString(infoJson, "spider", "");
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
            String lives = infoJson.get("lives").getAsJsonArray().toString();
            int index = lives.indexOf("proxy://");
            if (index != -1) {
                int endIndex = lives.lastIndexOf("\"");
                String url = lives.substring(index, endIndex);
                url = DefaultConfig.checkReplaceProxy(url);

                //clan
                String extUrl = Uri.parse(url).getQueryParameter("ext");
                if (extUrl != null && !extUrl.isEmpty()) {
                    String extUrlFix = new String(Base64.decode(extUrl, Base64.DEFAULT | Base64.URL_SAFE | Base64.NO_WRAP), "UTF-8");
                    if (extUrlFix.startsWith("clan://")) {
                        extUrlFix = clanContentFix(clanToAddress(apiUrl), extUrlFix);
                        extUrlFix = Base64.encodeToString(extUrlFix.getBytes("UTF-8"), Base64.DEFAULT | Base64.URL_SAFE | Base64.NO_WRAP);
                        url = url.replace(extUrl, extUrlFix);
                    }
                }
                ChannelGroup channelGroup = new ChannelGroup();
                channelGroup.setGroupName(url);
                channelGroupList.add(channelGroup);
            } else {
                loadLives(infoJson.get("lives").getAsJsonArray());
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

    public void loadLives(JsonArray livesArray) {
        int groupIndex = 0;
        int channelIndex = 0;
        for (JsonElement groupElement : livesArray) {
            ChannelGroup channelGroup = new ChannelGroup();
            channelGroup.setGroupNum(groupIndex++);
            channelGroup.setGroupName(((JsonObject) groupElement).get("Group").getAsString().trim());
            for (JsonElement channelElement : ((JsonObject) groupElement).get("channels").getAsJsonArray()) {
                JsonObject obj = (JsonObject) channelElement;
                LiveChannel liveChannel = new LiveChannel();
                liveChannel.setChannelName(obj.get("name").getAsString().trim());
                liveChannel.setChannelNum(channelIndex++);
                ArrayList<String> urls = DefaultConfig.safeJsonStringList(obj, "urls");
                liveChannel.setUrls(urls);
                channelGroup.getLiveChannels().add(liveChannel);
            }
            channelGroupList.add(channelGroup);
        }
    }

    public String getSpider() {
        return spider;
    }

    public Spider getCSP(SourceBean sourceBean) {
        return jarLoader.getSpider(sourceBean.getKey(), sourceBean.getApi(), sourceBean.getExt());
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

    public List<ChannelGroup> getChannelGroupList() {
        return channelGroupList;
    }

    public void setChannelGroupList(List<ChannelGroup> list) {
        channelGroupList.clear();
        channelGroupList.addAll(list);
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
