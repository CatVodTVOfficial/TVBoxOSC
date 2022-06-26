package com.github.tvbox.osc.player.controller;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ProgressBar;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.tvbox.osc.R;

import java.util.Map;

import xyz.doikki.videoplayer.controller.GestureVideoController;
import xyz.doikki.videoplayer.controller.IControlComponent;
import xyz.doikki.videoplayer.player.VideoView;
import xyz.doikki.videoplayer.player.VideoViewManager;

/**
 * 直播/点播控制器
 * 注意：此控制器仅做一个参考，如果想定制ui，你可以直接继承GestureVideoController或者BaseVideoController实现
 * 你自己的控制器
 * Created by dueeeke on 2017/4/7.
 */

public class BoxVideoController extends GestureVideoController implements View.OnClickListener {

    protected ProgressBar mLoadingProgress;

    public BoxVideoController(@NonNull Context context) {
        this(context, null);
        VideoViewManager.instance().setPlayOnMobileNetwork(true);
    }

    public BoxVideoController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BoxVideoController(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.box_standard_controller;
    }

    @Override
    protected void initView() {
        super.initView();
        mLoadingProgress = findViewById(R.id.loading);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
    }


    @Override
    protected void onVisibilityChanged(boolean isVisible, Animation anim) {
        if (mControlWrapper.isFullScreen()) {
            if (isVisible) {
            } else {
            }
        }
    }

    @Override
    protected void onPlayStateChanged(int playState) {
        super.onPlayStateChanged(playState);
        switch (playState) {
            //调用release方法会回到此状态
            case VideoView.STATE_IDLE:
                mLoadingProgress.setVisibility(GONE);
                break;
            case VideoView.STATE_PLAYING:
            case VideoView.STATE_PAUSED:
            case VideoView.STATE_PREPARED:
            case VideoView.STATE_ERROR:
            case VideoView.STATE_BUFFERED:
                mLoadingProgress.setVisibility(GONE);
                break;
            case VideoView.STATE_PREPARING:
            case VideoView.STATE_BUFFERING:
                mLoadingProgress.setVisibility(VISIBLE);
                break;
            case VideoView.STATE_PLAYBACK_COMPLETED:
                mLoadingProgress.setVisibility(GONE);
                break;
        }
    }

    private OnScreenTapListener screenTapListener;

    public interface OnScreenTapListener {
        void tap();
    }

    public void setScreenTapListener(OnScreenTapListener screenTapListener) {
        this.screenTapListener = screenTapListener;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (screenTapListener != null)
            screenTapListener.tap();
        return super.onSingleTapConfirmed(e);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return super.onScroll(e1, e2, distanceX, distanceY);
    }

    private boolean simSlideStart = false;
    private int simSeekPosition = 0;
    private long simSlideOffset = 0;

    public boolean isBoxTVBottomShow() {
        for (Map.Entry<IControlComponent, Boolean> next : mControlComponents.entrySet()) {
            IControlComponent component = next.getKey();
            if (component instanceof BoxVodControlView) {
                return ((BoxVodControlView) component).isTVBottomShow();
            }
        }
        return false;
    }

    public void boxTVBottomToggle() {
        for (Map.Entry<IControlComponent, Boolean> next : mControlComponents.entrySet()) {
            IControlComponent component = next.getKey();
            if (component instanceof BoxVodControlView) {
                ((BoxVodControlView) component).onTVBottomToggle();
            }
        }
    }

    public void boxTVTogglePlay() {
        if (mControlWrapper.isPlaying()) {
            mControlWrapper.pause();
            for (Map.Entry<IControlComponent, Boolean> next : mControlComponents.entrySet()) {
                IControlComponent component = next.getKey();
                if (component instanceof BoxVodControlView) {
                    ((BoxVodControlView) component).onTVPause();
                }
            }
        } else {
            mControlWrapper.start();
            for (Map.Entry<IControlComponent, Boolean> next : mControlComponents.entrySet()) {
                IControlComponent component = next.getKey();
                if (component instanceof BoxVodControlView) {
                    ((BoxVodControlView) component).onTVPlay();
                }
            }
        }
    }

    public void boxTVRefreshInfo(String title) {
        for (Map.Entry<IControlComponent, Boolean> next : mControlComponents.entrySet()) {
            IControlComponent component = next.getKey();
            if (component instanceof BoxVodControlView) {
                ((BoxVodControlView) component).onTVInfo(title);
            }
        }
    }

    public void boxTVSlideStop() {
        if (!simSlideStart)
            return;
        for (Map.Entry<IControlComponent, Boolean> next : mControlComponents.entrySet()) {
            IControlComponent component = next.getKey();
            if (component instanceof BoxVodControlView) {
                ((BoxVodControlView) component).onTVStopSlide();
            }
        }
        mControlWrapper.seekTo(simSeekPosition);
        if (!mControlWrapper.isPlaying())
            mControlWrapper.start();
        simSlideStart = false;
        simSeekPosition = 0;
        simSlideOffset = 0;
    }

    public void boxTVSlideStart(int dir) {
        int duration = (int) mControlWrapper.getDuration();
        if (duration <= 0)
            return;
        if (!simSlideStart) {
            for (Map.Entry<IControlComponent, Boolean> next : mControlComponents.entrySet()) {
                IControlComponent component = next.getKey();
                if (component instanceof BoxVodControlView) {
                    ((BoxVodControlView) component).onTVStartSlide();
                }
            }
            simSlideStart = true;
        }
        // 每次10秒
        simSlideOffset += (10000.0f * dir);
        int currentPosition = (int) mControlWrapper.getCurrentPosition();
        int position = (int) (simSlideOffset + currentPosition);
        if (position > duration) position = duration;
        if (position < 0) position = 0;
        for (Map.Entry<IControlComponent, Boolean> next : mControlComponents.entrySet()) {
            IControlComponent component = next.getKey();
            if (component instanceof BoxVodControlView) {
                ((BoxVodControlView) component).onTVPositionChange(position, currentPosition, duration);
            }
        }
        simSeekPosition = position;
    }
}
