package com.github.tvbox.osc.ui.tv.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.bean.VodInfo;
import com.github.tvbox.osc.cache.RoomDataManger;
import com.github.tvbox.osc.event.RefreshEvent;
import com.github.tvbox.osc.ui.activity.DetailActivity;
import com.github.tvbox.osc.ui.activity.SearchActivity;
import com.github.tvbox.osc.ui.adapter.HomeHotVodAdapter;
import com.github.tvbox.osc.util.HawkConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;
import com.orhanobut.hawk.Hawk;
import com.owen.tvrecyclerview.widget.TvRecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class HotVodList extends FrameLayout {
    private final String DOUBAN_HOT_MOVIE_DATA = "douban_hot_movie_data";
    private final String DOUBAN_HOT_MOVIE_UPDATE_DATE = "douban_hot_movie_update_date";
    private final String DOUBAN_HOT_SHOW_DATA = "douboan_hot_show_data";
    private final String DOUBAN_HOT_SHOW_UPDATE_DATE = "douban_hot_show_update_date";

    private Context mContext;
    private Activity mActivity;

    private TvRecyclerView mHotVodRecycler;
    private HomeHotVodAdapter mHotVodAdapter;

    public HotVodList(@NonNull Context context) {
        super(context);
        mContext = context;

        init();
    }

    public HotVodList(@NonNull Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mContext = context;

        init();
    }

    private void init() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_hot_vod_list, this);

        mHotVodRecycler = view.findViewById(R.id.tvHotRecycler);
        mHotVodRecycler.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
                itemView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).setInterpolator(new BounceInterpolator()).start();
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                itemView.animate().scaleX(1.05f).scaleY(1.05f).setDuration(300).setInterpolator(new BounceInterpolator()).start();
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {

            }
        });
    }

    public void initView(Activity activity) {
        mActivity = activity;

        mHotVodAdapter = new HomeHotVodAdapter();
        mHotVodAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (ApiConfig.get().getSourceBeanList().isEmpty())
                    return;

                HomeHotVodAdapter.HotVod hotVod = (HomeHotVodAdapter.HotVod) adapter.getItem(position);
                if (hotVod.getId() == null) {
                    String title = ((HomeHotVodAdapter.HotVod) adapter.getItem(position)).getName();
                    Intent newIntent = new Intent(mContext, SearchActivity.class);
                    newIntent.putExtra("title", title);
                    newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mActivity.startActivity(newIntent);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString("id", hotVod.getId());
                    bundle.putString("sourceKey", hotVod.getSourceKey());

                    Intent intent = new Intent(mContext, DetailActivity.class);
                    intent.putExtras(bundle);
                    mActivity.startActivity(intent);
                }
            }
        });
        mHotVodRecycler.setAdapter(mHotVodAdapter);

        int source = Hawk.get(HawkConfig.HOT_VOD_SOURCE, 0);
        if (source == 1) {
            loadDoubanHotMovie();
        } else if (source == 2) {
            loadDoubanHotShow();
        } else {
            loadHistory();
        }
    }

    public void refreshView() {
        int source = Hawk.get(HawkConfig.HOT_VOD_SOURCE, 0);
        switch (source) {
            case 1:
                loadDoubanHotMovie();
                break;
            case 2:
                loadDoubanHotShow();
                break;
            default:
                loadHistory();
        }
    }

    private void loadHistory() {
        List<VodInfo> allVodRecord = RoomDataManger.getAllVodRecord();
        if (allVodRecord.size() == 0) {
            loadDoubanHotMovie();
            return;
        }

        ArrayList<HomeHotVodAdapter.HotVod> vodInfoList = new ArrayList<>();
        for (VodInfo vodInfo : allVodRecord) {
            HomeHotVodAdapter.HotVod hotVod =new HomeHotVodAdapter.HotVod(
                    vodInfo.id, vodInfo.sourceKey, vodInfo.name, vodInfo.type, vodInfo.pic);
            vodInfoList.add(hotVod);
        }
        mHotVodAdapter.setNewData(vodInfoList);
    }

    private void loadDoubanHotMovie() {
        try {
            long time = Hawk.get(DOUBAN_HOT_MOVIE_UPDATE_DATE, 0L);
            if (System.currentTimeMillis() - time < 6 * 60 * 60 * 1000) {
                String json = Hawk.get(DOUBAN_HOT_MOVIE_DATA, "");
                if (!json.isEmpty()) {
                    ArrayList<HomeHotVodAdapter.HotVod> data = parseDoubanData(json);
                    mHotVodAdapter.setNewData(data);
                    return;
                }
            }

            String url = "https://movie.douban.com/j/search_subjects?type=movie&tag=%E7%83%AD%E9%97%A8&page_limit=50&page_start=0";
            OkGo.<String>get(url).execute(new AbsCallback<String>() {
                @Override
                public void onSuccess(Response<String> response) {
                    String netJson = response.body();
                    Hawk.put(DOUBAN_HOT_MOVIE_UPDATE_DATE, System.currentTimeMillis());
                    Hawk.put(DOUBAN_HOT_MOVIE_DATA, netJson);
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayList<HomeHotVodAdapter.HotVod> data = parseDoubanData(netJson);
                            mHotVodAdapter.setNewData(data);
                        }
                    });
                }

                @Override
                public String convertResponse(okhttp3.Response response) throws Throwable {
                    return response.body().string();
                }
            });
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private void loadDoubanHotShow() {
        try {
            long time = Hawk.get(DOUBAN_HOT_SHOW_UPDATE_DATE, 0L);
            if (System.currentTimeMillis() - time < 6 * 60 * 60 * 1000) {
                String json = Hawk.get(DOUBAN_HOT_SHOW_DATA, "");
                if (!json.isEmpty()) {
                    ArrayList<HomeHotVodAdapter.HotVod> data = parseDoubanData(json);
                    mHotVodAdapter.setNewData(data);
                    return;
                }
            }

            String url = "https://movie.douban.com/j/search_subjects?type=tv&tag=%E7%83%AD%E9%97%A8&page_limit=50&page_start=0";
            OkGo.<String>get(url).execute(new AbsCallback<String>() {
                @Override
                public void onSuccess(Response<String> response) {
                    String netJson = response.body();
                    Hawk.put(DOUBAN_HOT_SHOW_UPDATE_DATE, System.currentTimeMillis());
                    Hawk.put(DOUBAN_HOT_SHOW_DATA, netJson);
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayList<HomeHotVodAdapter.HotVod> data = parseDoubanData(netJson);
                            mHotVodAdapter.setNewData(data);
                        }
                    });
                }

                @Override
                public String convertResponse(okhttp3.Response response) throws Throwable {
                    return response.body().string();
                }
            });
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private ArrayList<HomeHotVodAdapter.HotVod> parseDoubanData(String json) {
        ArrayList<HomeHotVodAdapter.HotVod> result = new ArrayList<>();
        try {
            JsonObject infoJson = new Gson().fromJson(json, JsonObject.class);
            JsonArray array = infoJson.getAsJsonArray("subjects");
            for (JsonElement ele : array) {
                JsonObject obj = (JsonObject) ele;
                result.add(new HomeHotVodAdapter.HotVod(
                        obj.get("title").getAsString(),
                        obj.get("rate").getAsString(),
                        obj.get("cover").getAsString()
                ));
            }
        } catch (Throwable th) {

        }
        return result;
    }
}
