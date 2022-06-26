package com.github.tvbox.osc.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.player.controller.BoxVideoController;
import com.github.tvbox.osc.player.controller.BoxVodControlView;
import com.github.tvbox.osc.ui.dialog.ParseDialog;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.PlayerHelper;
import com.orhanobut.hawk.Hawk;

import xyz.doikki.videocontroller.component.GestureView;
import xyz.doikki.videoplayer.player.VideoView;

/**
 * @author pj567
 * @date :2021/3/5
 * @description:
 */
public class ProjectionPlayActivity extends BaseActivity {
    private VideoView mVideoView;
    private BoxVideoController controller;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_projection_play;
    }

    @Override
    protected void init() {
        initView();
        initData();
    }

    private void initView() {
        mVideoView = findViewById(R.id.mVideoView);
        PlayerHelper.updateCfg(mVideoView);

//        ViewGroup.LayoutParams layoutParams = mVideoView.getLayoutParams();
//        layoutParams.width = 100;
//        layoutParams.height = 50;
//        mVideoView.setLayoutParams(layoutParams);

        mVideoView.addOnStateChangeListener(new VideoView.SimpleOnStateChangeListener() {
            @Override
            public void onPlayStateChanged(int state) {
                switch (state) {
                    case VideoView.STATE_IDLE:
                    case VideoView.STATE_PREPARED:
                    case VideoView.STATE_PLAYING:
                    case VideoView.STATE_BUFFERED:
                    case VideoView.STATE_PAUSED:
                    case VideoView.STATE_BUFFERING:
                    case VideoView.STATE_PREPARING:
                        break;
                    case VideoView.STATE_PLAYBACK_COMPLETED:
                        finish();
                        break;
                    case VideoView.STATE_ERROR:
                        finish();
                        tryDismissParse();
                        Toast.makeText(mContext, "播放错误", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        controller = new BoxVideoController(this);

        controller.addControlComponent(new GestureView(this));
        BoxVodControlView boxVodControlView = new BoxVodControlView(this);
        boxVodControlView.hideNextPre();
        controller.addControlComponent(boxVodControlView);
        controller.setCanChangePosition(true);
        controller.setEnableInNormal(true);
        controller.setGestureEnabled(true);
        mVideoView.setVideoController(controller);
    }

    ParseDialog parseDialog = null;

    void tryDismissParse() {
        if (parseDialog != null) {
            try {
                parseDialog.dismiss();
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
            String html = bundle.getString("html");

            tryDismissParse();

            parseDialog = new ParseDialog().build(this, new ParseDialog.BackPress() {
                @Override
                public void onBack() {
                    ProjectionPlayActivity.this.finish();
                    tryDismissParse();
                }
            });

            parseDialog.show();

/*            parseDialog.parse("", "", html, new ParseDialog.ParseCallback() {
                @Override
                public void success(String playUrl, Map<String, String> headers) {
                    controller.boxTVRefreshInfo(playUrl);
                    if (mVideoView != null) {
                        mVideoView.release();
                        mVideoView.setUrl(playUrl);
                        mVideoView.start();
                    }
                    tryDismissParse();
                }

                @Override
                public void fail() {
                    ProjectionPlayActivity.this.finish();
                    tryDismissParse();
                }
            });*/
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        if (action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                controller.boxTVSlideStart(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ? 1 : -1);
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || (Hawk.get(HawkConfig.DEBUG_OPEN, false) && keyCode == KeyEvent.KEYCODE_0)) {
                controller.boxTVTogglePlay();
            }
        } else if (action == KeyEvent.ACTION_UP) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                controller.boxTVSlideStop();
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
        tryDismissParse();
    }
}