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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.bean.ChannelGroup;
import com.github.tvbox.osc.bean.LiveChannel;
import com.github.tvbox.osc.player.controller.BoxVideoController;
import com.github.tvbox.osc.ui.adapter.ChannelGroupAdapter;
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

import xyz.doikki.videocontroller.component.GestureView;
import xyz.doikki.videoplayer.player.VideoView;

/**
 * @author pj567
 * @date :2021/1/12
 * @description:
 */
public class LivePlayActivity extends BaseActivity {
    private VideoView mVideoView;
    private TextView tvHint;
    private TextView tvChannel;
    private LinearLayout tvLeftLinearLayout;
    private TvRecyclerView mGroupGridView;
    private TvRecyclerView mChannelGridView;
    private ChannelGroupAdapter groupAdapter;
    private LiveChannelAdapter channelAdapter;
    private Handler mHandler = new Handler();

    private List<ChannelGroup> channelGroupList = new ArrayList<>();
    private int selectedGroupIndex = 0;
    private int focusedGroupIndex = 0;
    private int focusedChannelIndex = 0;
    private int currentGroupIndex = 0;
    private int currentChannelIndex = 0;
    private LiveChannel currentChannel = null;


    @Override
    protected int getLayoutResID() {
        return R.layout.activity_live_play;
    }

    @Override
    protected void init() {
        setLoadSir(findViewById(R.id.live_root));
        mVideoView = findViewById(R.id.mVideoView);
        PlayerHelper.updateCfg(mVideoView);

        tvLeftLinearLayout = findViewById(R.id.tvLeftLinearLayout);
        mGroupGridView = findViewById(R.id.mGroupGridView);
        mChannelGridView = findViewById(R.id.mChannelGridView);
        tvChannel = findViewById(R.id.tvChannel);
        tvHint = findViewById(R.id.tvHint);

        mGroupGridView.setHasFixedSize(true);
        mGroupGridView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));
        mChannelGridView.setHasFixedSize(true);
        mChannelGridView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));
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

        groupAdapter = new ChannelGroupAdapter();
        mGroupGridView.setAdapter(groupAdapter);
        mGroupGridView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
            }
        });

        //电视
        mGroupGridView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                focusChannelGroup(position);
                if (position == selectedGroupIndex) return;
                selectChannelGroup(position);
                channelAdapter.setNewData(channelGroupList.get(position).getLiveChannels());
                if (position == currentGroupIndex)
                    mChannelGridView.scrollToPosition(currentChannelIndex);
                else
                    mChannelGridView.scrollToPosition(0);
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
                channelAdapter.setNewData(channelGroupList.get(position).getLiveChannels());
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
            }
        });

        //手机/模拟器
        groupAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                selectChannelGroup(position);
                channelAdapter.setNewData(channelGroupList.get(position).getLiveChannels());
                if (position == currentGroupIndex)
                    mChannelGridView.scrollToPosition(currentChannelIndex);
                else
                    mChannelGridView.scrollToPosition(0);
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
            }
        });

        channelAdapter = new LiveChannelAdapter();
        mChannelGridView.setAdapter(channelAdapter);
        mChannelGridView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
            }
        });

        //电视
        mChannelGridView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {

            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                if (position < 0) return;
                focusLiveChannel(position);
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
                if (selectedGroupIndex == currentGroupIndex && position == currentChannelIndex) return;
                if (playChannel(position, false)) {
                    mHandler.post(mHideChannelListRun);
                }
            }
        });

        //手机/模拟器
        channelAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                if (selectedGroupIndex == currentGroupIndex && position == currentChannelIndex) return;
                if (playChannel(position, false)) {
                    mHandler.post(mHideChannelListRun);
                }
            }
        });

        initChannelListView();
    }

    @Override
    public void onBackPressed() {
        if (tvLeftLinearLayout.getVisibility() == View.VISIBLE) {
            mHandler.post(mHideChannelListRun);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int keyCode = event.getKeyCode();
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && tvLeftLinearLayout.getVisibility() == View.INVISIBLE) {
                playNext();
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP && tvLeftLinearLayout.getVisibility() == View.INVISIBLE) {
                playPrevious();
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && tvLeftLinearLayout.getVisibility() == View.INVISIBLE) {
                preSourceUrl();
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && tvLeftLinearLayout.getVisibility() == View.INVISIBLE) {
                nextSourceUrl();
            } else if (((Hawk.get(HawkConfig.DEBUG_OPEN, false) && keyCode == KeyEvent.KEYCODE_0) || keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE/* || keyCode == KeyEvent.KEYCODE_0*/) && tvLeftLinearLayout.getVisibility() == View.INVISIBLE) {
                showChannelList();
            } else if (tvLeftLinearLayout.getVisibility() == View.INVISIBLE) {
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
            if (tvLeftLinearLayout.getVisibility() == View.VISIBLE) {
//                mHandler.postDelayed(mHideChannelListRun, 5000);
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
        if (tvLeftLinearLayout.getVisibility() == View.VISIBLE) {
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

    private void initChannelListView() {
        List<ChannelGroup> list = ApiConfig.get().getChannelGroupList();
        if (list.isEmpty())
            return;

        if (list.size() == 1 && list.get(0).getGroupName().startsWith("http://127.0.0.1")) {
            showLoading();
            loadProxyLives(list.get(0).getGroupName());
        }
        else {
            channelGroupList.clear();
            channelGroupList.addAll(list);
            showSuccess();
            initLiveState();
        }
    }

    public void loadProxyLives(String url) {
        OkGo.<String>get(url).execute(new AbsCallback<String>() {

            @Override
            public String convertResponse(okhttp3.Response response) throws Throwable {
                return response.body().string();
            }

            @Override
            public void onSuccess(Response<String> response) {
                List<LiveChannel> list = new ArrayList<>();
                JsonArray livesArray = new Gson().fromJson(response.body(), JsonArray.class);
                loadLives(livesArray);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        LivePlayActivity.this.showSuccess();
                        initLiveState();
                    }
                });
            }
        });
    }

    public void loadLives(JsonArray livesArray) {
        int groupNum = 1;
        int channelNum = 1;
        for (JsonElement groupElement : livesArray) {
            ChannelGroup channelGroup = new ChannelGroup();
            channelGroup.setLiveChannels(new ArrayList<LiveChannel>());
            channelGroup.setGroupNum(groupNum++);
            channelGroup.setGroupName(((JsonObject) groupElement).get("group").getAsString().trim());
            for (JsonElement channelElement : ((JsonObject) groupElement).get("channels").getAsJsonArray()) {
                JsonObject obj = (JsonObject) channelElement;
                LiveChannel liveChannel = new LiveChannel();
                liveChannel.setChannelName(obj.get("name").getAsString().trim());
                liveChannel.setChannelNum(channelNum++);
                ArrayList<String> urls = DefaultConfig.safeJsonStringList(obj, "urls");
                liveChannel.setChannelUrls(urls);
                channelGroup.getLiveChannels().add(liveChannel);
            }
            channelGroupList.add(channelGroup);
        }
        ApiConfig.get().setChannelGroupList(channelGroupList);
    }

    private void initLiveState() {
        String lastChannelName = Hawk.get(HawkConfig.LIVE_CHANNEL, "");

        int groupIndex = 0;
        int channelIndex = 0;
        boolean bFound = false;
        for (ChannelGroup channelGroup : channelGroupList) {
            for (LiveChannel liveChannel : channelGroup.getLiveChannels()) {
                if (liveChannel.getChannelName().equals(lastChannelName)) {
                    bFound = true;
                    break;
                }
                channelIndex++;
            }
            if (bFound) {
                selectedGroupIndex = groupIndex;
                currentGroupIndex = groupIndex;
                currentChannelIndex = channelIndex;
                focusedGroupIndex = groupIndex;
                focusedChannelIndex = channelIndex;
                break;
            }
            groupIndex++;
            channelIndex = 0;
        }

        tvLeftLinearLayout.setVisibility(View.INVISIBLE);
        tvHint.setVisibility(View.INVISIBLE);
//        tvUrl.setVisibility(Hawk.get(HawkConfig.DEBUG_OPEN, false) ? View.VISIBLE : View.INVISIBLE);

        groupAdapter.setNewData(channelGroupList);
        channelAdapter.setNewData(channelGroupList.get(currentGroupIndex).getLiveChannels());
        mGroupGridView.scrollToPosition(currentGroupIndex);
        mChannelGridView.scrollToPosition(currentChannelIndex);
        selectChannelGroup(currentGroupIndex);

        playChannel(currentChannelIndex, false);
    }

    private void refreshTextInfo() {
        tvChannel.setText(String.format("%d", currentChannel.getChannelNum()));
    }

    private Runnable mHideChannelListRun = new Runnable() {
        @Override
        public void run() {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tvLeftLinearLayout.getLayoutParams();
            if (tvLeftLinearLayout.getVisibility() == View.VISIBLE) {
                ViewObj viewObj = new ViewObj(tvLeftLinearLayout, params);
                ObjectAnimator animator = ObjectAnimator.ofObject(viewObj, "marginLeft", new IntEvaluator(), 0, -tvLeftLinearLayout.getLayoutParams().width);
                animator.setDuration(200);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (channelGroupList.size() > 0) {                   //修复未配置接口，进入直播会导致崩溃
                            deselectChannelGroup(selectedGroupIndex);
                            if (focusedGroupIndex > -1) {
                                defocusChannelGroup(focusedGroupIndex);
                                focusedGroupIndex = -1;
                            }
                            if (focusedChannelIndex > -1) {
                                defocusLiveChannel(focusedChannelIndex);
                                focusedChannelIndex = -1;
                            }
                        }
                        tvLeftLinearLayout.setVisibility(View.INVISIBLE);
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

    private Runnable mFocusCurrentChannelAndShowChannelList = new Runnable() {
        @Override
        public void run() {
            if (mGroupGridView.isScrolling() || mChannelGridView.isScrolling()) {
                mHandler.postDelayed(this, 100);
            } else {
                RecyclerView.ViewHolder holder = mChannelGridView.findViewHolderForAdapterPosition(currentChannelIndex);
                if (holder != null)
                    holder.itemView.requestFocus();
                ViewObj viewObj = new ViewObj(tvLeftLinearLayout, (ViewGroup.MarginLayoutParams) tvLeftLinearLayout.getLayoutParams());
                ObjectAnimator animator = ObjectAnimator.ofObject(viewObj, "marginLeft", new IntEvaluator(), -tvLeftLinearLayout.getLayoutParams().width, 0);
                animator.setDuration(200);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                });
                animator.start();
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
            }
        }
    };

    private void showChannelList() {
        if (tvLeftLinearLayout.getVisibility() == View.INVISIBLE) {
            tvHint.setVisibility(View.VISIBLE);
            tvLeftLinearLayout.setVisibility(View.VISIBLE);

            //重新载入上一次状态
            channelAdapter.setNewData(channelGroupList.get(currentGroupIndex).getLiveChannels());
            mGroupGridView.scrollToPosition(currentGroupIndex);
            mChannelGridView.scrollToPosition(currentChannelIndex);
            mGroupGridView.setSelection(currentGroupIndex);
            mChannelGridView.setSelection(currentChannelIndex);
            selectChannelGroup(currentGroupIndex);
            selectLiveChannel(currentChannelIndex);
            mHandler.postDelayed(mFocusCurrentChannelAndShowChannelList, 200);
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

    private boolean playChannel(int channelIndex, boolean changeSource) {
        if (!changeSource) {
            selectLiveChannel(channelIndex);
            currentChannel = channelGroupList.get(currentGroupIndex).getLiveChannels().get(currentChannelIndex);
            showChannelNum();
            Hawk.put(HawkConfig.LIVE_CHANNEL, currentChannel.getChannelName());
        }
        mVideoView.release();
        mVideoView.setUrl(currentChannel.getUrl());
        mVideoView.start();
        return true;
    }

    private boolean playChannelByNum(int channelNum) {
        int groupIndex = 0;
        int channelIndex = 0;
        boolean bFound = false;
        for (ChannelGroup channelGroup : channelGroupList) {
            for (LiveChannel liveChannel : channelGroup.getLiveChannels()) {
                if (liveChannel.getChannelNum() == channelNum) {
                    if (groupIndex == currentGroupIndex && channelIndex == currentChannelIndex)
                        return true;
                    else {
                        selectChannelGroup(groupIndex);
                        return playChannel(channelIndex, false);
                    }
                }
                channelIndex++;
            }
            groupIndex++;
            channelIndex = 0;
        }
        return false;
    }

    private void playNext() {
        int newChannelIndex = currentChannelIndex;
        newChannelIndex++;
        if (newChannelIndex >= channelGroupList.get(currentGroupIndex).getLiveChannels().size())
            newChannelIndex = 0;

        playChannel(newChannelIndex, false);
    }

    private void playPrevious() {
        int newChannelIndex = currentChannelIndex;
        newChannelIndex--;
        if (newChannelIndex < 0)
            newChannelIndex = channelGroupList.get(currentGroupIndex).getLiveChannels().size() - 1;

        playChannel(newChannelIndex, false);
    }

    public void preSourceUrl() {
        currentChannel.preSource();
        playChannel(currentChannelIndex, true);
    }

    public void nextSourceUrl() {
        currentChannel.nextSource();
        playChannel(currentChannelIndex, true);
    }

    public void selectChannelGroup(int groupIndex) {
        channelGroupList.get(selectedGroupIndex).setSelected(false);
        groupAdapter.notifyItemChanged(selectedGroupIndex);
        selectedGroupIndex = groupIndex;
        channelGroupList.get(selectedGroupIndex).setSelected(true);
        groupAdapter.notifyItemChanged(selectedGroupIndex);
    }

    public void deselectChannelGroup(int groupIndex) {
        channelGroupList.get(groupIndex).setSelected(false);
        groupAdapter.notifyItemChanged(groupIndex);
    }

    public void selectLiveChannel(int channelIndex) {
        channelGroupList.get(currentGroupIndex).getLiveChannels().get(currentChannelIndex).setSelected(false);
        channelAdapter.notifyItemChanged(currentChannelIndex);
        currentChannelIndex = channelIndex;
        currentGroupIndex = selectedGroupIndex;
        channelGroupList.get(currentGroupIndex).getLiveChannels().get(currentChannelIndex).setSelected(true);
        channelAdapter.notifyItemChanged(currentChannelIndex);
    }

    public void focusChannelGroup(int groupIndex) {
        if (focusedChannelIndex > -1) {
            defocusLiveChannel(focusedChannelIndex);
            focusedChannelIndex = -1;
        }
        if (focusedGroupIndex > -1) {
            channelGroupList.get(focusedGroupIndex).setFocused(false);
            groupAdapter.notifyItemChanged(focusedGroupIndex);
        }
        focusedGroupIndex = groupIndex;
        channelGroupList.get(focusedGroupIndex).setFocused(true);
        groupAdapter.notifyItemChanged(focusedGroupIndex);
    }

    public void defocusChannelGroup(int groupIndex) {
        channelGroupList.get(groupIndex).setFocused(false);
        groupAdapter.notifyItemChanged(groupIndex);
    }

    public void focusLiveChannel(int channelIndex) {
        if (focusedGroupIndex > -1) {
            defocusChannelGroup(focusedGroupIndex);
            focusedGroupIndex = -1;
        }
        if (focusedChannelIndex > -1) {
            channelGroupList.get(selectedGroupIndex).getLiveChannels().get(focusedChannelIndex).setFocused(false);
            channelAdapter.notifyItemChanged(focusedChannelIndex);
        }
        focusedChannelIndex = channelIndex;
        channelGroupList.get(selectedGroupIndex).getLiveChannels().get(focusedChannelIndex).setFocused(true);
        channelAdapter.notifyItemChanged(focusedChannelIndex);
    }

    public void defocusLiveChannel(int channelIndex) {
        channelGroupList.get(selectedGroupIndex).getLiveChannels().get(channelIndex).setFocused(false);
        channelAdapter.notifyItemChanged(channelIndex);
    }
}