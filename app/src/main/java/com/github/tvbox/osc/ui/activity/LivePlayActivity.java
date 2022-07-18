package com.github.tvbox.osc.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.App;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.bean.LiveChannelGroup;
import com.github.tvbox.osc.bean.LiveChannelItem;
import com.github.tvbox.osc.bean.LivePlayerManager;
import com.github.tvbox.osc.bean.LiveSettingGroup;
import com.github.tvbox.osc.bean.LiveSettingItem;
import com.github.tvbox.osc.player.controller.LiveController;
import com.github.tvbox.osc.ui.adapter.LiveChannelGroupAdapter;
import com.github.tvbox.osc.ui.adapter.LiveChannelItemAdapter;
import com.github.tvbox.osc.ui.adapter.LiveSettingGroupAdapter;
import com.github.tvbox.osc.ui.adapter.LiveSettingItemAdapter;
import com.github.tvbox.osc.ui.dialog.LivePasswordDialog;
import com.github.tvbox.osc.ui.tv.widget.ViewObj;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.HawkConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;
import com.orhanobut.hawk.Hawk;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import xyz.doikki.videoplayer.player.VideoView;

/**
 * @author pj567
 * @date :2021/1/12
 * @description:
 */
public class LivePlayActivity extends BaseActivity {
    private VideoView mVideoView;
    private TextView tvChannelInfo;
    private TextView tvTime;
    private TextView tvNetSpeed;
    private LinearLayout tvLeftChannelListLayout;
    private TvRecyclerView mChannelGroupView;
    private TvRecyclerView mLiveChannelView;
    private LiveChannelGroupAdapter liveChannelGroupAdapter;
    private LiveChannelItemAdapter liveChannelItemAdapter;

    private LinearLayout tvRightSettingLayout;
    private TvRecyclerView mSettingGroupView;
    private TvRecyclerView mSettingItemView;
    private LiveSettingGroupAdapter liveSettingGroupAdapter;
    private LiveSettingItemAdapter liveSettingItemAdapter;
    private List<LiveSettingGroup> liveSettingGroupList = new ArrayList<>();

    private Handler mHandler = new Handler();

    private List<LiveChannelGroup> liveChannelGroupList = new ArrayList<>();
    private int currentChannelGroupIndex = 0;
    private int currentLiveChannelIndex = -1;
    private int currentLiveChangeSourceTimes = 0;
    private LiveChannelItem currentLiveChannelItem = null;
    private LivePlayerManager livePlayerManager = new LivePlayerManager();
    private ArrayList<Integer> channelGroupPasswordConfirmed = new ArrayList<>();

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_live_play;
    }

    @Override
    protected void init() {
        setLoadSir(findViewById(R.id.live_root));
        mVideoView = findViewById(R.id.mVideoView);

        tvLeftChannelListLayout = findViewById(R.id.tvLeftChannnelListLayout);
        mChannelGroupView = findViewById(R.id.mGroupGridView);
        mLiveChannelView = findViewById(R.id.mChannelGridView);
        tvRightSettingLayout = findViewById(R.id.tvRightSettingLayout);
        mSettingGroupView = findViewById(R.id.mSettingGroupView);
        mSettingItemView = findViewById(R.id.mSettingItemView);
        tvChannelInfo = findViewById(R.id.tvChannel);
        tvTime = findViewById(R.id.tvTime);
        tvNetSpeed = findViewById(R.id.tvNetSpeed);

        initVideoView();
        initChannelGroupView();
        initLiveChannelView();
        initSettingGroupView();
        initSettingItemView();
        initLiveChannelList();
        initLiveSettingGroupList();
    }

    @Override
    public void onBackPressed() {
        if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideChannelListRun);
            mHandler.post(mHideChannelListRun);
        }
        else if (tvRightSettingLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideSettingLayoutRun);
            mHandler.post(mHideSettingLayoutRun);
        }
        else {
            mHandler.removeCallbacks(mConnectTimeoutChangeSourceRun);
            mHandler.removeCallbacks(mUpdateNetSpeedRun);
            super.onBackPressed();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int keyCode = event.getKeyCode();
            if (keyCode == KeyEvent.KEYCODE_MENU) {
                showSettingGroup();
            } else if (!isListOrSettingLayoutVisible()) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_UP:
                        if (Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, false))
                            playNext();
                        else
                            playPrevious();
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        if (Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, false))
                            playPrevious();
                        else
                            playNext();
                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        playPreSource();
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        playNextSource();
                        break;
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                    case KeyEvent.KEYCODE_ENTER:
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        showChannelList();
                        break;
                }
            }
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mVideoView != null) {
            mVideoView.resume();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoView != null) {
            mVideoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoView != null) {
            mVideoView.release();
            mVideoView = null;
        }
    }

    private void showChannelList() {
        if (tvRightSettingLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideSettingLayoutRun);
            mHandler.post(mHideSettingLayoutRun);
        }
        if (tvLeftChannelListLayout.getVisibility() == View.INVISIBLE) {
            //重新载入上一次状态
            liveChannelItemAdapter.setNewData(getLiveChannels(currentChannelGroupIndex));
            if (currentLiveChannelIndex > -1)
                mLiveChannelView.scrollToPosition(currentLiveChannelIndex);
            mLiveChannelView.setSelection(currentLiveChannelIndex);
            mChannelGroupView.scrollToPosition(currentChannelGroupIndex);
            mChannelGroupView.setSelection(currentChannelGroupIndex);
            mHandler.postDelayed(mFocusCurrentChannelAndShowChannelList, 200);
        } else {
            mHandler.removeCallbacks(mHideChannelListRun);
            mHandler.post(mHideChannelListRun);
        }
    }

    private Runnable mFocusCurrentChannelAndShowChannelList = new Runnable() {
        @Override
        public void run() {
            if (mChannelGroupView.isScrolling() || mLiveChannelView.isScrolling() || mChannelGroupView.isComputingLayout() || mLiveChannelView.isComputingLayout()) {
                mHandler.postDelayed(this, 100);
            } else {
                liveChannelGroupAdapter.setSelectedGroupIndex(currentChannelGroupIndex);
                liveChannelItemAdapter.setSelectedChannelIndex(currentLiveChannelIndex);
                RecyclerView.ViewHolder holder = mLiveChannelView.findViewHolderForAdapterPosition(currentLiveChannelIndex);
                if (holder != null)
                    holder.itemView.requestFocus();
                tvLeftChannelListLayout.setVisibility(View.VISIBLE);
                ViewObj viewObj = new ViewObj(tvLeftChannelListLayout, (ViewGroup.MarginLayoutParams) tvLeftChannelListLayout.getLayoutParams());
                ObjectAnimator animator = ObjectAnimator.ofObject(viewObj, "marginLeft", new IntEvaluator(), -tvLeftChannelListLayout.getLayoutParams().width, 0);
                animator.setDuration(200);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mHandler.removeCallbacks(mHideChannelListRun);
                        mHandler.postDelayed(mHideChannelListRun, 5000);
                    }
                });
                animator.start();
            }
        }
    };

    private Runnable mHideChannelListRun = new Runnable() {
        @Override
        public void run() {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tvLeftChannelListLayout.getLayoutParams();
            if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE) {
                ViewObj viewObj = new ViewObj(tvLeftChannelListLayout, params);
                ObjectAnimator animator = ObjectAnimator.ofObject(viewObj, "marginLeft", new IntEvaluator(), 0, -tvLeftChannelListLayout.getLayoutParams().width);
                animator.setDuration(200);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        tvLeftChannelListLayout.setVisibility(View.INVISIBLE);
                    }
                });
                animator.start();
            }
        }
    };

    private void showChannelInfo() {
        tvChannelInfo.setText(String.format(Locale.getDefault(), "%d %s %s(%d/%d)", currentLiveChannelItem.getChannelNum(),
                currentLiveChannelItem.getChannelName(), currentLiveChannelItem.getSourceName(),
                currentLiveChannelItem.getSourceIndex() + 1, currentLiveChannelItem.getSourceNum()));

        FrameLayout.LayoutParams lParams = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (tvRightSettingLayout.getVisibility() == View.VISIBLE) {
            lParams.gravity = Gravity.LEFT;
            lParams.leftMargin = 60;
            lParams.topMargin = 30;
        } else {
            lParams.gravity = Gravity.RIGHT;
            lParams.rightMargin = 60;
            lParams.topMargin = 30;
        }
        tvChannelInfo.setLayoutParams(lParams);

        tvChannelInfo.setVisibility(View.VISIBLE);
        mHandler.removeCallbacks(mHideChannelInfoRun);
        mHandler.postDelayed(mHideChannelInfoRun, 3000);
    }

    private Runnable mHideChannelInfoRun = new Runnable() {
        @Override
        public void run() {
            tvChannelInfo.setVisibility(View.INVISIBLE);
        }
    };

    private boolean playChannel(int channelGroupIndex, int liveChannelIndex, boolean changeSource) {
        if ((channelGroupIndex == currentChannelGroupIndex && liveChannelIndex == currentLiveChannelIndex && !changeSource)
                || (changeSource && currentLiveChannelItem.getSourceNum() == 1)) {
            showChannelInfo();
            return true;
        }
        mVideoView.release();
        if (!changeSource) {
            currentChannelGroupIndex = channelGroupIndex;
            currentLiveChannelIndex = liveChannelIndex;
            currentLiveChannelItem = getLiveChannels(currentChannelGroupIndex).get(currentLiveChannelIndex);
            Hawk.put(HawkConfig.LIVE_CHANNEL, currentLiveChannelItem.getChannelName());
            livePlayerManager.getLiveChannelPlayer(mVideoView, currentLiveChannelItem.getChannelName());
        }
        mVideoView.setUrl(currentLiveChannelItem.getUrl());
        showChannelInfo();
        mVideoView.start();
        return true;
    }

    private void playNext() {
        if (!isCurrentLiveChannelValid()) return;
        Integer[] groupChannelIndex = getNextChannel(1);
        playChannel(groupChannelIndex[0], groupChannelIndex[1], false);
    }

    private void playPrevious() {
        if (!isCurrentLiveChannelValid()) return;
        Integer[] groupChannelIndex = getNextChannel(-1);
        playChannel(groupChannelIndex[0], groupChannelIndex[1], false);
    }

    public void playPreSource() {
        if (!isCurrentLiveChannelValid()) return;
        currentLiveChannelItem.preSource();
        playChannel(currentChannelGroupIndex, currentLiveChannelIndex, true);
    }

    public void playNextSource() {
        if (!isCurrentLiveChannelValid()) return;
        currentLiveChannelItem.nextSource();
        playChannel(currentChannelGroupIndex, currentLiveChannelIndex, true);
    }

    //显示设置列表
    private void showSettingGroup() {
        if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideChannelListRun);
            mHandler.post(mHideChannelListRun);
        }
        if (tvRightSettingLayout.getVisibility() == View.INVISIBLE) {
            if (!isCurrentLiveChannelValid()) return;
            //重新载入默认状态
            loadCurrentSourceList();
            liveSettingGroupAdapter.setNewData(liveSettingGroupList);
            selectSettingGroup(0, false);
            mSettingGroupView.scrollToPosition(0);
            mSettingItemView.scrollToPosition(currentLiveChannelItem.getSourceIndex());
            mHandler.postDelayed(mFocusAndShowSettingGroup, 200);
        } else {
            mHandler.removeCallbacks(mHideSettingLayoutRun);
            mHandler.post(mHideSettingLayoutRun);
        }
    }

    private Runnable mFocusAndShowSettingGroup = new Runnable() {
        @Override
        public void run() {
            if (mSettingGroupView.isScrolling() || mSettingItemView.isScrolling() || mSettingGroupView.isComputingLayout() || mSettingItemView.isComputingLayout()) {
                mHandler.postDelayed(this, 100);
            } else {
                RecyclerView.ViewHolder holder = mSettingGroupView.findViewHolderForAdapterPosition(0);
                if (holder != null)
                    holder.itemView.requestFocus();
                tvRightSettingLayout.setVisibility(View.VISIBLE);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tvRightSettingLayout.getLayoutParams();
                if (tvRightSettingLayout.getVisibility() == View.VISIBLE) {
                    ViewObj viewObj = new ViewObj(tvRightSettingLayout, params);
                    ObjectAnimator animator = ObjectAnimator.ofObject(viewObj, "marginRight", new IntEvaluator(), -tvRightSettingLayout.getLayoutParams().width, 0);
                    animator.setDuration(200);
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mHandler.postDelayed(mHideSettingLayoutRun, 5000);
                        }
                    });
                    animator.start();
                }
            }
        }
    };

    private Runnable mHideSettingLayoutRun = new Runnable() {
        @Override
        public void run() {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tvRightSettingLayout.getLayoutParams();
            if (tvRightSettingLayout.getVisibility() == View.VISIBLE) {
                ViewObj viewObj = new ViewObj(tvRightSettingLayout, params);
                ObjectAnimator animator = ObjectAnimator.ofObject(viewObj, "marginRight", new IntEvaluator(), 0, -tvRightSettingLayout.getLayoutParams().width);
                animator.setDuration(200);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        tvRightSettingLayout.setVisibility(View.INVISIBLE);
                        liveSettingGroupAdapter.setSelectedGroupIndex(-1);
                    }
                });
                animator.start();
            }
        }
    };

    private void initVideoView() {
        LiveController controller = new LiveController(this);
        controller.setListener(new LiveController.LiveControlListener() {
            @Override
            public boolean singleTap() {
                showChannelList();
                return true;
            }

            @Override
            public void longPress() {
                showSettingGroup();
            }

            @Override
            public void playStateChanged(int playState) {
                switch (playState) {
                    case VideoView.STATE_IDLE:
                    case VideoView.STATE_PAUSED:
                        break;
                    case VideoView.STATE_PREPARED:
                    case VideoView.STATE_BUFFERED:
                    case VideoView.STATE_PLAYING:
                        currentLiveChangeSourceTimes = 0;
                        mHandler.removeCallbacks(mConnectTimeoutChangeSourceRun);
                        break;
                    case VideoView.STATE_ERROR:
                    case VideoView.STATE_PLAYBACK_COMPLETED:
                        mHandler.removeCallbacks(mConnectTimeoutChangeSourceRun);
                        mHandler.post(mConnectTimeoutChangeSourceRun);
                        break;
                    case VideoView.STATE_PREPARING:
                    case VideoView.STATE_BUFFERING:
                        mHandler.removeCallbacks(mConnectTimeoutChangeSourceRun);
                        mHandler.postDelayed(mConnectTimeoutChangeSourceRun, (Hawk.get(HawkConfig.LIVE_CONNECT_TIMEOUT, 1) + 1) * 5000);
                        break;
                }
            }

            @Override
            public void changeSource(int direction) {
                if (direction > 0)
                    playNextSource();
                else
                    playPreSource();
            }
        });
        controller.setCanChangePosition(false);
        controller.setEnableInNormal(true);
        controller.setGestureEnabled(true);
        controller.setDoubleTapTogglePlayEnabled(false);
        mVideoView.setVideoController(controller);
        mVideoView.setProgressManager(null);
    }

    private Runnable mConnectTimeoutChangeSourceRun = new Runnable() {
        @Override
        public void run() {
            currentLiveChangeSourceTimes++;
            if (currentLiveChannelItem.getSourceNum() == currentLiveChangeSourceTimes) {
                currentLiveChangeSourceTimes = 0;
                Integer[] groupChannelIndex = getNextChannel(Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, false) ? -1 : 1);
                playChannel(groupChannelIndex[0], groupChannelIndex[1], false);
            } else {
                playNextSource();
            }
        }
    };

    private void initChannelGroupView() {
        mChannelGroupView.setHasFixedSize(true);
        mChannelGroupView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));

        liveChannelGroupAdapter = new LiveChannelGroupAdapter();
        mChannelGroupView.setAdapter(liveChannelGroupAdapter);
        mChannelGroupView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
            }
        });

        //电视
        mChannelGroupView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                selectChannelGroup(position, true, -1);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
                if (isNeedInputPassword(position)) {
                    showPasswordDialog(position, -1);
                }
            }
        });

        //手机/模拟器
        liveChannelGroupAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                selectChannelGroup(position, false, -1);
            }
        });
    }

    private void selectChannelGroup(int groupIndex, boolean focus, int liveChannelIndex) {
        if (focus) {
            liveChannelGroupAdapter.setFocusedGroupIndex(groupIndex);
            liveChannelItemAdapter.setFocusedChannelIndex(-1);
        }
        if ((groupIndex > -1 && groupIndex != liveChannelGroupAdapter.getSelectedGroupIndex()) || isNeedInputPassword(groupIndex)) {
            liveChannelGroupAdapter.setSelectedGroupIndex(groupIndex);
            if (isNeedInputPassword(groupIndex)) {
                showPasswordDialog(groupIndex, liveChannelIndex);
                return;
            }
            loadChannelGroupDataAndPlay(groupIndex, liveChannelIndex);
        }
        if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideChannelListRun);
            mHandler.postDelayed(mHideChannelListRun, 5000);
        }
    }

    private void initLiveChannelView() {
        mLiveChannelView.setHasFixedSize(true);
        mLiveChannelView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));

        liveChannelItemAdapter = new LiveChannelItemAdapter();
        mLiveChannelView.setAdapter(liveChannelItemAdapter);
        mLiveChannelView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
            }
        });

        //电视
        mLiveChannelView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                if (position < 0) return;
                liveChannelGroupAdapter.setFocusedGroupIndex(-1);
                liveChannelItemAdapter.setFocusedChannelIndex(position);
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
                clickLiveChannel(position);
            }
        });

        //手机/模拟器
        liveChannelItemAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                clickLiveChannel(position);
            }
        });
    }

    private void clickLiveChannel(int position) {
        liveChannelItemAdapter.setSelectedChannelIndex(position);
        playChannel(liveChannelGroupAdapter.getSelectedGroupIndex(), position, false);
        if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideChannelListRun);
            mHandler.postDelayed(mHideChannelListRun, 5000);
        }
    }

    private void initSettingGroupView() {
        mSettingGroupView.setHasFixedSize(true);
        mSettingGroupView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));

        liveSettingGroupAdapter = new LiveSettingGroupAdapter();
        mSettingGroupView.setAdapter(liveSettingGroupAdapter);
        mSettingGroupView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mHandler.removeCallbacks(mHideSettingLayoutRun);
                mHandler.postDelayed(mHideSettingLayoutRun, 5000);
            }
        });

        //电视
        mSettingGroupView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                selectSettingGroup(position, true);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
            }
        });

        //手机/模拟器
        liveSettingGroupAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                selectSettingGroup(position, false);
            }
        });
    }

    private void selectSettingGroup(int position, boolean focus) {
        if (!isCurrentLiveChannelValid()) return;
        if (focus) {
            liveSettingGroupAdapter.setFocusedGroupIndex(position);
            liveSettingItemAdapter.setFocusedItemIndex(-1);
        }
        if (position == liveSettingGroupAdapter.getSelectedGroupIndex() || position < -1)
            return;

        liveSettingGroupAdapter.setSelectedGroupIndex(position);
        liveSettingItemAdapter.setNewData(liveSettingGroupList.get(position).getLiveSettingItems());

        switch (position) {
            case 0:
                liveSettingItemAdapter.selectItem(currentLiveChannelItem.getSourceIndex(), true, false);
                break;
            case 1:
                liveSettingItemAdapter.selectItem(livePlayerManager.getLivePlayerScale(), true, true);
                break;
            case 2:
                liveSettingItemAdapter.selectItem(livePlayerManager.getLivePlayerType(), true, true);
                break;
        }
        int scrollToPosition = liveSettingItemAdapter.getSelectedItemIndex();
        if (scrollToPosition < 0) scrollToPosition = 0;
        mSettingItemView.scrollToPosition(scrollToPosition);
        mHandler.removeCallbacks(mHideSettingLayoutRun);
        mHandler.postDelayed(mHideSettingLayoutRun, 5000);
    }

    private void initSettingItemView() {
        mSettingItemView.setHasFixedSize(true);
        mSettingItemView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));

        liveSettingItemAdapter = new LiveSettingItemAdapter();
        mSettingItemView.setAdapter(liveSettingItemAdapter);
        mSettingItemView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mHandler.removeCallbacks(mHideSettingLayoutRun);
                mHandler.postDelayed(mHideSettingLayoutRun, 5000);
            }
        });

        //电视
        mSettingItemView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                if (position < 0) return;
                liveSettingGroupAdapter.setFocusedGroupIndex(-1);
                liveSettingItemAdapter.setFocusedItemIndex(position);
                mHandler.removeCallbacks(mHideSettingLayoutRun);
                mHandler.postDelayed(mHideSettingLayoutRun, 5000);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
                clickSettingItem(position);
            }
        });

        //手机/模拟器
        liveSettingItemAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                clickSettingItem(position);
            }
        });
    }

    private void clickSettingItem(int position) {
        int settingGroupIndex = liveSettingGroupAdapter.getSelectedGroupIndex();
        if (settingGroupIndex < 4) {
            if (position == liveSettingItemAdapter.getSelectedItemIndex())
                return;
            liveSettingItemAdapter.selectItem(position, true, true);
        }
        switch (settingGroupIndex) {
            case 0://线路切换
                currentLiveChannelItem.setSourceIndex(position);
                playChannel(currentChannelGroupIndex, currentLiveChannelIndex,true);
                break;
            case 1://画面比例
                livePlayerManager.changeLivePlayerScale(mVideoView, position, currentLiveChannelItem.getChannelName());
                break;
            case 2://播放解码
                mVideoView.release();
                livePlayerManager.changeLivePlayerType(mVideoView, position, currentLiveChannelItem.getChannelName());
                mVideoView.setUrl(currentLiveChannelItem.getUrl());
                mVideoView.start();
                break;
            case 3://超时换源
                Hawk.put(HawkConfig.LIVE_CONNECT_TIMEOUT, position);
                break;
            case 4://超时换源
                boolean select = false;
                switch (position) {
                    case 0:
                        select = !Hawk.get(HawkConfig.LIVE_SHOW_TIME, false);
                        Hawk.put(HawkConfig.LIVE_SHOW_TIME, select);
                        showTime();
                        break;
                    case 1:
                        select = !Hawk.get(HawkConfig.LIVE_SHOW_NET_SPEED, false);
                        Hawk.put(HawkConfig.LIVE_SHOW_NET_SPEED, select);
                        showNetSpeed();
                        break;
                    case 2:
                        select = !Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, false);
                        Hawk.put(HawkConfig.LIVE_CHANNEL_REVERSE, select);
                        break;
                    case 3:
                        select = !Hawk.get(HawkConfig.LIVE_CROSS_GROUP, false);
                        Hawk.put(HawkConfig.LIVE_CROSS_GROUP, select);
                        break;
                }
                liveSettingItemAdapter.selectItem(position, select, false);
                break;
        }
        mHandler.removeCallbacks(mHideSettingLayoutRun);
        mHandler.postDelayed(mHideSettingLayoutRun, 5000);
    }

    private void initLiveChannelList() {
        List<LiveChannelGroup> list = ApiConfig.get().getChannelGroupList();
        if (list.isEmpty()) {
            Toast.makeText(App.getInstance(), "频道列表为空", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (list.size() == 1 && list.get(0).getGroupName().startsWith("http://127.0.0.1")) {
            showLoading();
            loadProxyLives(list.get(0).getGroupName());
        }
        else {
            liveChannelGroupList.clear();
            liveChannelGroupList.addAll(list);
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
                JsonArray livesArray = new Gson().fromJson(response.body(), JsonArray.class);
                ApiConfig.get().loadLives(livesArray);
                List<LiveChannelGroup> list = ApiConfig.get().getChannelGroupList();
                if (list.isEmpty()) {
                    Toast.makeText(App.getInstance(), "频道列表为空", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                liveChannelGroupList.clear();
                liveChannelGroupList.addAll(list);

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

    private void initLiveState() {
        String lastChannelName = Hawk.get(HawkConfig.LIVE_CHANNEL, "");

        int lastChannelGroupIndex = -1;
        int lastLiveChannelIndex = -1;
        for (LiveChannelGroup liveChannelGroup : liveChannelGroupList) {
            for (LiveChannelItem liveChannelItem : liveChannelGroup.getLiveChannels()) {
                if (liveChannelItem.getChannelName().equals(lastChannelName)) {
                    lastChannelGroupIndex = liveChannelGroup.getGroupIndex();
                    lastLiveChannelIndex = liveChannelItem.getChannelIndex();
                    break;
                }
            }
            if (lastChannelGroupIndex != -1) break;
        }
        if (lastChannelGroupIndex == -1) {
            lastChannelGroupIndex = getFirstNoPasswordChannelGroup();
            if (lastChannelGroupIndex == -1)
                lastChannelGroupIndex = 0;
            lastLiveChannelIndex = 0;
        }

        livePlayerManager.init(mVideoView);
        showTime();
        showNetSpeed();
        tvLeftChannelListLayout.setVisibility(View.INVISIBLE);
        tvRightSettingLayout.setVisibility(View.INVISIBLE);

        liveChannelGroupAdapter.setNewData(liveChannelGroupList);
        selectChannelGroup(lastChannelGroupIndex, false, lastLiveChannelIndex);
    }

    private boolean isListOrSettingLayoutVisible() {
        return tvLeftChannelListLayout.getVisibility() == View.VISIBLE || tvRightSettingLayout.getVisibility() == View.VISIBLE;
    }

    private void initLiveSettingGroupList() {
        ArrayList<String> groupNames = new ArrayList<>(Arrays.asList("线路选择", "画面比例", "播放解码", "超时换源", "偏好设置"));
        ArrayList<ArrayList<String>> itemsArrayList = new ArrayList<>();
        ArrayList<String> sourceItems = new ArrayList<>();
        ArrayList<String> scaleItems = new ArrayList<>(Arrays.asList("默认", "16:9", "4:3", "填充", "原始", "裁剪"));
        ArrayList<String> playerDecoderItems = new ArrayList<>(Arrays.asList("系统", "ijk硬解", "ijk软解", "exo"));
        ArrayList<String> timeoutItems = new ArrayList<>(Arrays.asList("5s", "10s", "15s", "20s", "25s", "30s"));
        ArrayList<String> personalSettingItems = new ArrayList<>(Arrays.asList("显示时间", "显示网速", "换台反转", "跨选分类"));
        itemsArrayList.add(sourceItems);
        itemsArrayList.add(scaleItems);
        itemsArrayList.add(playerDecoderItems);
        itemsArrayList.add(timeoutItems);
        itemsArrayList.add(personalSettingItems);

        liveSettingGroupList.clear();
        for (int i = 0; i < groupNames.size(); i++) {
            LiveSettingGroup liveSettingGroup = new LiveSettingGroup();
            ArrayList<LiveSettingItem> liveSettingItemList = new ArrayList<>();
            liveSettingGroup.setGroupIndex(i);
            liveSettingGroup.setGroupName(groupNames.get(i));
            for (int j = 0; j < itemsArrayList.get(i).size(); j++) {
                LiveSettingItem liveSettingItem = new LiveSettingItem();
                liveSettingItem.setItemIndex(j);
                liveSettingItem.setItemName(itemsArrayList.get(i).get(j));
                liveSettingItemList.add(liveSettingItem);
            }
            liveSettingGroup.setLiveSettingItems(liveSettingItemList);
            liveSettingGroupList.add(liveSettingGroup);
        }
        liveSettingGroupList.get(3).getLiveSettingItems().get(Hawk.get(HawkConfig.LIVE_CONNECT_TIMEOUT, 1)).setItemSelected(true);
        liveSettingGroupList.get(4).getLiveSettingItems().get(0).setItemSelected(Hawk.get(HawkConfig.LIVE_SHOW_TIME, false));
        liveSettingGroupList.get(4).getLiveSettingItems().get(1).setItemSelected(Hawk.get(HawkConfig.LIVE_SHOW_NET_SPEED, false));
        liveSettingGroupList.get(4).getLiveSettingItems().get(2).setItemSelected(Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, false));
        liveSettingGroupList.get(4).getLiveSettingItems().get(3).setItemSelected(Hawk.get(HawkConfig.LIVE_CROSS_GROUP, false));
    }

    private void loadCurrentSourceList() {
        ArrayList<String> currentSourceNames = currentLiveChannelItem.getChannelSourceNames();
        ArrayList<LiveSettingItem> liveSettingItemList = new ArrayList<>();
        for (int j = 0; j < currentSourceNames.size(); j++) {
            LiveSettingItem liveSettingItem = new LiveSettingItem();
            liveSettingItem.setItemIndex(j);
            liveSettingItem.setItemName(currentSourceNames.get(j));
            liveSettingItemList.add(liveSettingItem);
        }
        liveSettingGroupList.get(0).setLiveSettingItems(liveSettingItemList);
    }

    void showTime() {
        if (Hawk.get(HawkConfig.LIVE_SHOW_TIME, false)) {
            mHandler.post(mUpdateTimeRun);
            tvTime.setVisibility(View.VISIBLE);
        } else {
            mHandler.removeCallbacks(mUpdateTimeRun);
            tvTime.setVisibility(View.GONE);
        }
    }

    private Runnable mUpdateTimeRun = new Runnable() {
        @Override
        public void run() {
            Date day=new Date();
            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
            tvTime.setText(df.format(day));
            mHandler.postDelayed(this, 1000);
        }
    };

    private void showNetSpeed() {
        if (Hawk.get(HawkConfig.LIVE_SHOW_NET_SPEED, false)) {
            mHandler.post(mUpdateNetSpeedRun);
            tvNetSpeed.setVisibility(View.VISIBLE);
        } else {
            mHandler.removeCallbacks(mUpdateNetSpeedRun);
            tvNetSpeed.setVisibility(View.GONE);
        }
    }

    private Runnable mUpdateNetSpeedRun = new Runnable() {
        @Override
        public void run() {
            if (mVideoView == null) return;
            tvNetSpeed.setText(String.format("%.2fMB/s", (float)mVideoView.getTcpSpeed() / 1024.0 / 1024.0));
            mHandler.postDelayed(this, 1000);
        }
    };

    private void showPasswordDialog(int groupIndex, int liveChannelIndex) {
        if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE)
            mHandler.removeCallbacks(mHideChannelListRun);

        LivePasswordDialog dialog = new LivePasswordDialog(this);
        dialog.setOnListener(new LivePasswordDialog.OnListener() {
            @Override
            public void onChange(String password) {
                if (password.equals(liveChannelGroupList.get(groupIndex).getGroupPassword())) {
                    channelGroupPasswordConfirmed.add(groupIndex);
                    loadChannelGroupDataAndPlay(groupIndex, liveChannelIndex);
                } else {
                    Toast.makeText(App.getInstance(), "密码错误", Toast.LENGTH_SHORT).show();
                }

                if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE)
                    mHandler.postDelayed(mHideChannelListRun, 5000);
            }

            @Override
            public void onCancel() {
                if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE) {
                    int groupIndex = liveChannelGroupAdapter.getSelectedGroupIndex();
                    liveChannelItemAdapter.setNewData(getLiveChannels(groupIndex));
                }
            }
        });
        dialog.show();
    }

    private void loadChannelGroupDataAndPlay(int groupIndex, int liveChannelIndex) {
        liveChannelItemAdapter.setNewData(getLiveChannels(groupIndex));
        if (groupIndex == currentChannelGroupIndex) {
            if (currentLiveChannelIndex > -1)
                mLiveChannelView.scrollToPosition(currentLiveChannelIndex);
            liveChannelItemAdapter.setSelectedChannelIndex(currentLiveChannelIndex);
        }
        else {
            mLiveChannelView.scrollToPosition(0);
            liveChannelItemAdapter.setSelectedChannelIndex(-1);
        }

        if (liveChannelIndex > -1) {
            clickLiveChannel(liveChannelIndex);
            mChannelGroupView.scrollToPosition(groupIndex);
            mLiveChannelView.scrollToPosition(liveChannelIndex);
            playChannel(groupIndex, liveChannelIndex, false);
        }
    }

    private boolean isNeedInputPassword(int groupIndex) {
        return !liveChannelGroupList.get(groupIndex).getGroupPassword().isEmpty()
                && !isPasswordConfirmed(groupIndex);
    }

    private boolean isPasswordConfirmed(int groupIndex) {
        for (Integer confirmedNum : channelGroupPasswordConfirmed) {
            if (confirmedNum == groupIndex)
                return true;
        }
        return false;
    }

    private ArrayList<LiveChannelItem> getLiveChannels(int groupIndex) {
        if (!isNeedInputPassword(groupIndex)) {
            return liveChannelGroupList.get(groupIndex).getLiveChannels();
        } else {
            return new ArrayList<>();
        }
    }

    private Integer[] getNextChannel(int direction) {
        int channelGroupIndex = currentChannelGroupIndex;
        int liveChannelIndex = currentLiveChannelIndex;

        //跨选分组模式下跳过加密频道分组（遥控器上下键换台/超时换源）
        if (direction > 0) {
            liveChannelIndex++;
            if (liveChannelIndex >= getLiveChannels(channelGroupIndex).size()) {
                liveChannelIndex = 0;
                if (Hawk.get(HawkConfig.LIVE_CROSS_GROUP, false)) {
                    do {
                        channelGroupIndex++;
                        if (channelGroupIndex >= liveChannelGroupList.size())
                            channelGroupIndex = 0;
                    } while (!liveChannelGroupList.get(channelGroupIndex).getGroupPassword().isEmpty() || channelGroupIndex == currentChannelGroupIndex);
                }
            }
        } else {
            liveChannelIndex--;
            if (liveChannelIndex < 0) {
                if (Hawk.get(HawkConfig.LIVE_CROSS_GROUP, false)) {
                    do {
                        channelGroupIndex--;
                        if (channelGroupIndex < 0)
                            channelGroupIndex = liveChannelGroupList.size() - 1;
                    } while (!liveChannelGroupList.get(channelGroupIndex).getGroupPassword().isEmpty() || channelGroupIndex == currentChannelGroupIndex);
                }
                liveChannelIndex = getLiveChannels(channelGroupIndex).size() - 1;
            }
        }

        Integer[] groupChannelIndex = new Integer[2];
        groupChannelIndex[0] = channelGroupIndex;
        groupChannelIndex[1] = liveChannelIndex;

        return groupChannelIndex;
    }

    private int getFirstNoPasswordChannelGroup() {
        for (LiveChannelGroup liveChannelGroup : liveChannelGroupList) {
            if (liveChannelGroup.getGroupPassword().isEmpty())
                return liveChannelGroup.getGroupIndex();
        }
        return -1;
    }

    private boolean isCurrentLiveChannelValid() {
        if (currentLiveChannelItem == null) {
            Toast.makeText(App.getInstance(), "请先选择频道", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}