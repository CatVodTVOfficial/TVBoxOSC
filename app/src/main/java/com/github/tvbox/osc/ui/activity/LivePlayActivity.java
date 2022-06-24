package com.github.tvbox.osc.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.dueeeke.videocontroller.component.GestureView;
import com.dueeeke.videoplayer.player.VideoView;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.bean.LiveChannel;
import com.github.tvbox.osc.player.controller.BoxVideoController;
import com.github.tvbox.osc.ui.adapter.LiveChannelAdapter;
import com.github.tvbox.osc.ui.tv.widget.ViewObj;
import com.github.tvbox.osc.util.DefaultConfig;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.PlayerHelper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;
import com.orhanobut.hawk.Hawk;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pj567
 * @date :2021/1/12
 * @description:
 */
public class LivePlayActivity extends BaseActivity {
    private VideoView mVideoView;
    private TextView tvHint;
    private TextView tvUrl;
    private TextView tvChannel;
    private TvRecyclerView mGridView;
    private LiveChannelAdapter channelAdapter;
    private Handler mHandler = new Handler();

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_live_play;
    }

    @Override
    protected void init() {
        setLoadSir(findViewById(R.id.live_root));
        mVideoView = findViewById(R.id.mVideoView);
        PlayerHelper.updateCfg(mVideoView);
//        ViewGroup.LayoutParams layoutParams = mVideoView.getLayoutParams();
//        layoutParams.width = 100;
//        layoutParams.height = 50;
//        mVideoView.setLayoutParams(layoutParams);
        mGridView = findViewById(R.id.mGridView);
        tvChannel = findViewById(R.id.tvChannel);
        tvHint = findViewById(R.id.tvHint);
        tvUrl = findViewById(R.id.tvUrl);

        mGridView.setHasFixedSize(true);
        mGridView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));
        BoxVideoController controller = new BoxVideoController(this);
        controller.setScreenTapListener(new BoxVideoController.OnScreenTapListener() {
            @Override
            public void tap() {
                showChannelList();
            }
        });
        controller.addControlComponent(new GestureView(this));
        controller.setCanChangePosition(false);
        controller.setEnableInNormal(true);
        controller.setGestureEnabled(true);
        mVideoView.setVideoController(controller);
        mVideoView.setProgressManager(null);

        initChannelList();
    }

    @Override
    public void onBackPressed() {
        if (mGridView.getVisibility() == View.VISIBLE) {
            mHandler.post(mHideChannelListRun);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int keyCode = event.getKeyCode();
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && mGridView.getVisibility() == View.INVISIBLE) {
                playNext();
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP && mGridView.getVisibility() == View.INVISIBLE) {
                playPrevious();
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && mGridView.getVisibility() == View.INVISIBLE) {
                preSourceUrl();
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && mGridView.getVisibility() == View.INVISIBLE) {
                nextSourceUrl();
            } else if (((Hawk.get(HawkConfig.DEBUG_OPEN, false) && keyCode == KeyEvent.KEYCODE_0) || keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE/* || keyCode == KeyEvent.KEYCODE_0*/) && mGridView.getVisibility() == View.INVISIBLE) {
                showChannelList();
            } else if (mGridView.getVisibility() == View.INVISIBLE) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_0:
                        inputChannelNum("0");
                        break;
                    case KeyEvent.KEYCODE_1:
                        inputChannelNum("1");
                        break;
                    case KeyEvent.KEYCODE_2:
                        inputChannelNum("2");
                        break;
                    case KeyEvent.KEYCODE_3:
                        inputChannelNum("3");
                        break;
                    case KeyEvent.KEYCODE_4:
                        inputChannelNum("4");
                        break;
                    case KeyEvent.KEYCODE_5:
                        inputChannelNum("5");
                        break;
                    case KeyEvent.KEYCODE_6:
                        inputChannelNum("6");
                        break;
                    case KeyEvent.KEYCODE_7:
                        inputChannelNum("7");
                        break;
                    case KeyEvent.KEYCODE_8:
                        inputChannelNum("8");
                        break;
                    case KeyEvent.KEYCODE_9:
                        inputChannelNum("9");
                        break;
                }
            }
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            if (mGridView.getVisibility() == View.VISIBLE) {
                mHandler.postDelayed(mHideChannelListRun, 5000);
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mVideoView != null) {
            mVideoView.resume();
        }
        if (mGridView.getVisibility() == View.VISIBLE) {
            mHandler.postDelayed(mHideChannelListRun, 5000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoView != null) {
            mVideoView.pause();
        }
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoView != null) {
            mVideoView.release();
        }
    }

    private List<LiveChannel> channelList = new ArrayList<>();
    private LiveChannel currentChannel = null;

    private void initChannelList() {
        channelAdapter = new LiveChannelAdapter();
        mGridView.setAdapter(channelAdapter);
        mGridView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
            }
        });
        channelAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                if (playChannel(channelList.get(position), false)) {
                    mHandler.post(mHideChannelListRun);
                }
            }
        });
        List<LiveChannel> list = ApiConfig.get().getChannelList();
        if (list.isEmpty())
            return;
        if (list.size() > 0 && list.get(0).getUrls().startsWith("proxy://")) {
            showLoading();
            String url = DefaultConfig.checkReplaceProxy(list.get(0).getUrls());
            OkGo.<String>get(url).execute(new AbsCallback<String>() {

                @Override
                public String convertResponse(okhttp3.Response response) throws Throwable {
                    return response.body().string();
                }

                @Override
                public void onSuccess(Response<String> response) {
                    List<LiveChannel> list = new ArrayList<>();
                    JsonArray lives = new Gson().fromJson(response.body(), JsonArray.class);
                    int lcIdx = 0;
                    for (JsonElement opt : lives) {
                        for (JsonElement optChl : ((JsonObject) opt).get("channels").getAsJsonArray()) {
                            JsonObject obj = (JsonObject) optChl;
                            LiveChannel lc = new LiveChannel();
                            lc.setName(obj.get("name").getAsString().trim());
                            lc.setUrls(DefaultConfig.safeJsonStringList(obj, "urls"));
                            // 暂时不考虑分组问题
                            lc.setChannelNum(lcIdx++);
                            list.add(lc);
                        }
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            LivePlayActivity.this.showSuccess();
                            initList(list);
                        }
                    });
                }
            });
        } else {
            showSuccess();
            initList(list);
        }
    }

    private void initList(List<LiveChannel> list) {
        LiveChannel lastChannel = null;
        String lastChannelName = Hawk.get(HawkConfig.LIVE_CHANNEL, "");
        channelList.clear();
        channelList.addAll(list);
        for (LiveChannel lc : channelList) {
            if (lc.getName().equals(lastChannelName)) {
                lastChannel = lc;
                break;
            }
        }
        if (lastChannel == null)
            lastChannel = channelList.get(0);

        mGridView.setVisibility(View.INVISIBLE);
        tvHint.setVisibility(View.INVISIBLE);
        tvUrl.setVisibility(Hawk.get(HawkConfig.DEBUG_OPEN, false) ? View.VISIBLE : View.INVISIBLE);

        channelAdapter.setNewData(channelList);
        playChannel(lastChannel, false);
    }

    private void refreshTextInfo() {
        tvUrl.setText(currentChannel.getUrls());
        tvChannel.setText(String.format("%d", currentChannel.getChannelNum()));
    }

    private Runnable mHideChannelListRun = new Runnable() {
        @Override
        public void run() {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mGridView.getLayoutParams();
            if (mGridView.getVisibility() == View.VISIBLE) {
                ViewObj viewObj = new ViewObj(mGridView, params);
                ObjectAnimator animator = ObjectAnimator.ofObject(viewObj, "marginLeft", new IntEvaluator(), 0, -mGridView.getLayoutParams().width);
                animator.setDuration(200);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mGridView.setVisibility(View.INVISIBLE);
                        tvHint.setVisibility(View.INVISIBLE);
                    }
                });
                animator.start();
            }
        }
    };

    private Runnable mPlayUserInputChannelRun = new Runnable() {
        @Override
        public void run() {
            if (!TextUtils.isEmpty(userInputChannelNum)) {
                playChannelByNum(Integer.parseInt(userInputChannelNum));
                userInputChannelNum = "";
            }
            mHandler.postDelayed(mHideChannelNumRun, 4000);
        }
    };

    private Runnable mHideChannelNumRun = new Runnable() {
        @Override
        public void run() {
            tvChannel.setVisibility(View.INVISIBLE);
            refreshTextInfo();
        }
    };

    private Runnable showListAfterScrollOk = new Runnable() {
        @Override
        public void run() {
            if (mGridView.isScrolling()) {
                mHandler.postDelayed(this, 100);
            } else {
                ViewObj viewObj = new ViewObj(mGridView, (ViewGroup.MarginLayoutParams) mGridView.getLayoutParams());
                ObjectAnimator animator = ObjectAnimator.ofObject(viewObj, "marginLeft", new IntEvaluator(), -mGridView.getLayoutParams().width, 0);
                animator.setDuration(200);
                animator.start();
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                });
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
            }
        }
    };

    private void showChannelList() {
        if (mGridView.getVisibility() == View.INVISIBLE) {
            tvHint.setVisibility(View.VISIBLE);
            mGridView.setVisibility(View.VISIBLE);
            mGridView.setSelection(channelList.indexOf(currentChannel));
            mHandler.postDelayed(showListAfterScrollOk, 100);
        }
    }

    private String userInputChannelNum = "";

    private void inputChannelNum(String add) {
        if (userInputChannelNum.length() < 4) {
            mHandler.removeCallbacks(mPlayUserInputChannelRun);
            mHandler.removeCallbacks(mHideChannelNumRun);
            tvChannel.setVisibility(View.VISIBLE);
            userInputChannelNum = String.format("%s%s", userInputChannelNum, add);
            tvChannel.setText(userInputChannelNum);
            mHandler.postDelayed(mPlayUserInputChannelRun, 1000);
        }
    }

    private void showChannelNum() {
        refreshTextInfo();
        tvChannel.setVisibility(View.VISIBLE);
        mHandler.postDelayed(mHideChannelNumRun, 4000);
    }

    private boolean playChannel(LiveChannel channel, boolean changeSource) {
        if ((channel == currentChannel && !changeSource) || channel == null)
            return false;
        if (currentChannel != null)
            currentChannel.setDefault(false);
        channelAdapter.notifyItemChanged(channelList.indexOf(currentChannel));
        currentChannel = channel;
        currentChannel.setDefault(true);
        channelAdapter.notifyItemChanged(channelList.indexOf(currentChannel));
        showChannelNum();
        Hawk.put(HawkConfig.LIVE_CHANNEL, channel.getName());
        mVideoView.release();
        mVideoView.setUrl(channel.getUrls());
        mVideoView.start();
        return true;
    }

    private boolean playChannelByNum(int channelNum) {
        LiveChannel tempChannel = null;
        for (int i = 0; i < channelList.size(); i++) {
            LiveChannel channel = channelList.get(i);
            if (channel.getChannelNum() == channelNum) {
                tempChannel = channel;
                break;
            }
        }
        if (tempChannel == null)
            return false;
        return playChannel(tempChannel, false);
    }

    private void playNext() {
        int playIndex = channelList.indexOf(currentChannel);
        playIndex++;
        if (playIndex >= channelList.size()) {
            playIndex = 0;
        }
        playChannel(channelList.get(playIndex), false);
    }

    private void playPrevious() {
        int playIndex = channelList.indexOf(currentChannel);
        playIndex--;
        if (playIndex < 0) {
            playIndex = channelList.size() - 1;
        }
        playChannel(channelList.get(playIndex), false);
    }

    public void preSourceUrl() {
        currentChannel.sourceIdx--;
        playChannel(currentChannel, true);
    }

    public void nextSourceUrl() {
        currentChannel.sourceIdx++;
        playChannel(currentChannel, true);
    }
}