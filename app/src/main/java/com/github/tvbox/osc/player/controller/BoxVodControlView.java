package com.github.tvbox.osc.player.controller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import xyz.doikki.videoplayer.controller.ControlWrapper;
import xyz.doikki.videoplayer.controller.IControlComponent;
import xyz.doikki.videoplayer.player.VideoView;
import com.github.tvbox.osc.R;

import static xyz.doikki.videoplayer.util.PlayerUtils.stringForTime;

/**
 * 点播底部控制栏
 */
public class BoxVodControlView extends FrameLayout implements IControlComponent, View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    protected ControlWrapper mControlWrapper;

    private TextView mTotalTime, mCurrTime;
    private LinearLayout mBottomContainer;
    private LinearLayout mCenterContainer;
    private SeekBar mVideoProgress;
    private ProgressBar mBottomProgress;
    private ImageView mPlayButton;
    private TextView mPlayNext;
    private TextView mPlayPre;
    private TextView mPlaySize;
    private TextView mPlaySpeed;
    private boolean mIsDragging;

    private boolean mIsShowBottomProgress = false;

    // tv 相关控制
    private ImageView mTVProgressIcon;
    private TextView mTVProgressText;
    private TextView mTVName;
    private TextView mTVHint;
    private TextView mTVPauseProgressText;

    private LinearLayout mTVProgressContainer;
    private LinearLayout mTVPauseContainer;
    private LinearLayout mTVBottomContainer;
    private FrameLayout mTVInfoContainer;
    private TextView mTvPlayNext;
    private TextView mTvPlayPre;
    private TextView mTvPlaySize;
    private TextView mTvPlaySpeed;

    public BoxVodControlView(@NonNull Context context) {
        super(context);
    }

    public BoxVodControlView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BoxVodControlView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    {
        LayoutInflater.from(getContext()).inflate(getLayoutId(), this, true);
        mBottomContainer = findViewById(R.id.bottom_container);
        mCenterContainer = findViewById(R.id.center_container);
        mVideoProgress = findViewById(R.id.seekBar);
        mVideoProgress.setOnSeekBarChangeListener(this);
        mTotalTime = findViewById(R.id.total_time);
        mCurrTime = findViewById(R.id.curr_time);
        mPlayButton = findViewById(R.id.iv_play);
        mPlayButton.setOnClickListener(this);
        mBottomProgress = findViewById(R.id.bottom_progress);
        mPlayNext = findViewById(R.id.play_next);
        mPlayPre = findViewById(R.id.play_pre);
        mPlaySize = findViewById(R.id.video_size);
        mPlaySpeed = findViewById(R.id.video_speed);
        mPlayNext.setOnClickListener(this);
        mPlayPre.setOnClickListener(this);
        mPlaySize.setOnClickListener(this);
        mPlaySpeed.setOnClickListener(this);

        //5.1以下系统SeekBar高度需要设置成WRAP_CONTENT
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            mVideoProgress.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        mBottomContainer.setVisibility(GONE);
        mCenterContainer.setVisibility(GONE);
        mBottomProgress.setVisibility(GONE);

        // tv 相关控制
        mTVProgressContainer = findViewById(R.id.tv_progress_container);
        mTVProgressIcon = findViewById(R.id.tv_progress_icon);
        mTVProgressText = findViewById(R.id.tv_progress_text);
        mTVPauseContainer = findViewById(R.id.tv_pause_container);
        mTVPauseProgressText = findViewById(R.id.tv_pause_progress_text);
        mTVBottomContainer = findViewById(R.id.tv_bottom_container);
        mTVInfoContainer = findViewById(R.id.tv_info_container);
        mTVName = findViewById(R.id.tv_info_name);
        mTVHint = findViewById(R.id.tv_info_hint);
        mTvPlayNext = findViewById(R.id.tv_play_next);
        mTvPlayPre = findViewById(R.id.tv_play_pre);
        mTvPlayNext.setOnClickListener(this);
        mTvPlayPre.setOnClickListener(this);
        mTvPlaySize = findViewById(R.id.tv_video_size);
        mTvPlaySpeed = findViewById(R.id.tv_video_speed);
        mTvPlaySize.setOnClickListener(this);
        mTvPlaySpeed.setOnClickListener(this);
        mTVInfoContainer.setVisibility(GONE);
    }

    protected int getLayoutId() {
        return R.layout.box_vod_control_view;
    }

    /**
     * 是否显示底部进度条，默认显示
     */
    public void showBottomProgress(boolean isShow) {
        mIsShowBottomProgress = isShow;
    }

    @Override
    public void attach(@NonNull ControlWrapper controlWrapper) {
        mControlWrapper = controlWrapper;
        refreshScaleAndSpeed();
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onVisibilityChanged(boolean isVisible, Animation anim) {
        if (isVisible) {
            mBottomContainer.setVisibility(VISIBLE);
            mCenterContainer.setVisibility(VISIBLE);
            mTVInfoContainer.setVisibility(VISIBLE);
            if (anim != null) {
                mBottomContainer.startAnimation(anim);
            }
            if (mIsShowBottomProgress) {
                mBottomProgress.setVisibility(GONE);
            }
        } else {
            mBottomContainer.setVisibility(GONE);
            mCenterContainer.setVisibility(GONE);
            mTVInfoContainer.setVisibility(GONE);
            if (anim != null) {
                mBottomContainer.startAnimation(anim);
            }
            if (mIsShowBottomProgress) {
                mBottomProgress.setVisibility(VISIBLE);
                AlphaAnimation animation = new AlphaAnimation(0f, 1f);
                animation.setDuration(300);
                mBottomProgress.startAnimation(animation);
            }
        }
    }

    @Override
    public void onPlayStateChanged(int playState) {
        switch (playState) {
            case VideoView.STATE_IDLE:
            case VideoView.STATE_PLAYBACK_COMPLETED:
                mBottomContainer.setVisibility(GONE);
                mCenterContainer.setVisibility(GONE);
                mTVInfoContainer.setVisibility(GONE);
                mBottomProgress.setVisibility(GONE);
                mBottomProgress.setProgress(0);
                mBottomProgress.setSecondaryProgress(0);
                mVideoProgress.setProgress(0);
                mVideoProgress.setSecondaryProgress(0);
                break;
            case VideoView.STATE_START_ABORT:
            case VideoView.STATE_PREPARING:
            case VideoView.STATE_PREPARED:
            case VideoView.STATE_ERROR:
                mBottomContainer.setVisibility(GONE);
                mCenterContainer.setVisibility(GONE);
                mTVInfoContainer.setVisibility(GONE);
                mBottomProgress.setVisibility(GONE);
                break;
            case VideoView.STATE_PLAYING:
                mPlayButton.setSelected(true);
                if (mIsShowBottomProgress) {
                    if (mControlWrapper.isShowing()) {
                        mBottomProgress.setVisibility(GONE);
                        mBottomContainer.setVisibility(VISIBLE);
                        mCenterContainer.setVisibility(VISIBLE);
                        mTVInfoContainer.setVisibility(VISIBLE);
                    } else {
                        mBottomContainer.setVisibility(GONE);
                        mCenterContainer.setVisibility(GONE);
                        mTVInfoContainer.setVisibility(GONE);
                        mBottomProgress.setVisibility(VISIBLE);
                    }
                } else {
                    mBottomContainer.setVisibility(GONE);
                    mCenterContainer.setVisibility(GONE);
                    mTVInfoContainer.setVisibility(GONE);
                }
                //开始刷新进度
                mControlWrapper.startProgress();
                break;
            case VideoView.STATE_PAUSED:
                mPlayButton.setSelected(false);
                break;
            case VideoView.STATE_BUFFERING:
            case VideoView.STATE_BUFFERED:
                mPlayButton.setSelected(mControlWrapper.isPlaying());
                break;
        }
    }

    @Override
    public void onPlayerStateChanged(int playerState) {
    }

    @Override
    public void setProgress(int duration, int position) {
        if (mIsDragging) {
            return;
        }

        if (mVideoProgress != null) {
            if (duration > 0) {
                mVideoProgress.setEnabled(true);
                int pos = (int) (position * 1.0 / duration * mVideoProgress.getMax());
                mVideoProgress.setProgress(pos);
                mBottomProgress.setProgress(pos);
            } else {
                mVideoProgress.setEnabled(false);
            }
            int percent = mControlWrapper.getBufferedPercentage();
            if (percent >= 95) { //解决缓冲进度不能100%问题
                mVideoProgress.setSecondaryProgress(mVideoProgress.getMax());
                mBottomProgress.setSecondaryProgress(mBottomProgress.getMax());
            } else {
                mVideoProgress.setSecondaryProgress(percent * 10);
                mBottomProgress.setSecondaryProgress(percent * 10);
            }
        }

        if (mTotalTime != null)
            mTotalTime.setText(stringForTime(duration));
        if (mCurrTime != null)
            mCurrTime.setText(stringForTime(position));
    }

    @Override
    public void onLockStateChanged(boolean isLocked) {
        onVisibilityChanged(!isLocked, null);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_play) {
            mControlWrapper.togglePlay();
        } else if (id == R.id.play_next) {
            hideTvPause();
            vodControlListener.playNext();
        } else if (id == R.id.play_pre) {
            hideTvPause();
            vodControlListener.playPre();
        } else if (id == R.id.video_size) {
            changeScale();
        } else if (id == R.id.video_speed) {
            changeSpeed();
        } else if (id == R.id.tv_play_next) {
            onTVBottomToggle();
            hideTvPause();
            vodControlListener.playNext();
        } else if (id == R.id.tv_play_pre) {
            onTVBottomToggle();
            hideTvPause();
            vodControlListener.playPre();
        } else if (id == R.id.tv_video_size) {
            changeScale();
        } else if (id == R.id.tv_video_speed) {
            changeSpeed();
        }
    }

    private int screenScaleType = VideoView.SCREEN_SCALE_DEFAULT;
    private float videoSpeed = 1.0f;

    private void changeScale() {
        screenScaleType += 1;
        if (screenScaleType > VideoView.SCREEN_SCALE_CENTER_CROP) {
            screenScaleType = 0;
        }
        mControlWrapper.setScreenScaleType(screenScaleType);
        refreshScaleAndSpeed();
    }

    private void changeSpeed() {
        videoSpeed += 0.25f;
        if (videoSpeed > 2.1) {
            videoSpeed = 0.5f;
        }
        mControlWrapper.setSpeed(videoSpeed);
        refreshScaleAndSpeed();
    }

    private void refreshScaleAndSpeed() {
        String scaleText = "默认";
        switch (screenScaleType) {
            case VideoView.SCREEN_SCALE_DEFAULT:
                scaleText = "默认";
                break;
            case VideoView.SCREEN_SCALE_16_9:
                scaleText = "16:9";
                break;
            case VideoView.SCREEN_SCALE_4_3:
                scaleText = "4:3";
                break;
            case VideoView.SCREEN_SCALE_MATCH_PARENT:
                scaleText = "填充";
                break;
            case VideoView.SCREEN_SCALE_ORIGINAL:
                scaleText = "原始";
                break;
            case VideoView.SCREEN_SCALE_CENTER_CROP:
                scaleText = "裁剪";
                break;
        }
        String speedText = "x" + videoSpeed;
        mPlaySize.setText(scaleText);
        mPlaySpeed.setText(speedText);
        mTvPlaySize.setText(scaleText);
        mTvPlaySpeed.setText(speedText);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mIsDragging = true;
        mControlWrapper.stopProgress();
        mControlWrapper.stopFadeOut();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        long duration = mControlWrapper.getDuration();
        long newPosition = (duration * seekBar.getProgress()) / mVideoProgress.getMax();
        mControlWrapper.seekTo((int) newPosition);
        mIsDragging = false;
        mControlWrapper.startProgress();
        mControlWrapper.startFadeOut();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser) {
            return;
        }

        long duration = mControlWrapper.getDuration();
        long newPosition = (duration * progress) / mVideoProgress.getMax();
        if (mCurrTime != null)
            mCurrTime.setText(stringForTime((int) newPosition));
    }

    // tv 相关控制
    public void onTVStartSlide() {
        mControlWrapper.hide();
        mTVProgressContainer.animate()
                .alpha(1f)
                .setDuration(0)
                .setListener(null);
        mTVProgressContainer.setVisibility(VISIBLE);
        mTVInfoContainer.setVisibility(VISIBLE);
        // 快进快退 隐藏暂停 因为结束 会 结束自动开始播放
        hideTvPause();
    }


    public void onTVStopSlide() {
        mTVProgressContainer.animate()
                .alpha(0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mTVProgressContainer.setVisibility(GONE);
                        mTVInfoContainer.setVisibility(GONE);
                    }
                })
                .start();
    }

    public void onTVPositionChange(int slidePosition, int currentPosition, int duration) {
        if (slidePosition > currentPosition) {
            mTVProgressIcon.setImageResource(R.drawable.ic_pre);
        } else {
            mTVProgressIcon.setImageResource(R.drawable.ic_back);
        }
        mTVProgressText.setText(String.format("%s/%s", stringForTime(slidePosition), stringForTime(duration)));
    }

    private void hideTvPause() {
        mTVPauseContainer.animate()
                .alpha(0f)
                .setDuration(0)
                .setListener(null);
    }

    public void onTVPause() {
        int duration = (int) mControlWrapper.getDuration();
        int currentPosition = (int) mControlWrapper.getCurrentPosition();
        mTVPauseProgressText.setText(String.format("%s/%s", stringForTime(currentPosition), stringForTime(duration)));
        mTVPauseContainer.setVisibility(VISIBLE);
        mTVInfoContainer.setVisibility(VISIBLE);
        mTVPauseContainer.setAlpha(1f);
    }

    public void onTVBottomToggle() {
        boolean show = mTVBottomContainer.getVisibility() == GONE;
        // mTVPauseContainer 显示时 不显示底部控制菜单
        if (show && mTVPauseContainer.getVisibility() == VISIBLE)
            return;
        mTVBottomContainer.setVisibility(show ? VISIBLE : GONE);
    }

    public boolean isTVBottomShow() {
        return mTVBottomContainer.getVisibility() == VISIBLE;
    }

    public void onTVPlay() {
        mTVPauseContainer.animate()
                .alpha(0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mTVPauseContainer.setVisibility(GONE);
                        mTVInfoContainer.setVisibility(GONE);
                    }
                })
                .start();
    }

    public void onTVInfo(String title) {
        mTVName.setText(title);
    }

    public void hideNextPre() {
        mPlayNext.setVisibility(GONE);
        mPlayPre.setVisibility(GONE);
        mTVHint.setVisibility(GONE);
        mTvPlayNext.setVisibility(GONE);
        mTvPlayPre.setVisibility(GONE);
        ViewGroup.LayoutParams layoutParams = mTVName.getLayoutParams();
        if (layoutParams instanceof FrameLayout.LayoutParams) {
            ((LayoutParams) layoutParams).gravity = Gravity.CENTER;
        }
    }

    public interface BoxVodControlListener {
        void playNext();

        void playPre();
    }

    private BoxVodControlListener vodControlListener;

    public void setVodControlListener(BoxVodControlListener vodControlListener) {
        this.vodControlListener = vodControlListener;
    }
}
