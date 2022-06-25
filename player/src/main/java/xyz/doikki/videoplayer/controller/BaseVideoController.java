package xyz.doikki.videoplayer.controller;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import xyz.doikki.videoplayer.player.VideoView;
import xyz.doikki.videoplayer.player.VideoViewManager;
import xyz.doikki.videoplayer.util.CutoutUtil;
import xyz.doikki.videoplayer.util.L;
import xyz.doikki.videoplayer.util.PlayerUtils;

/**
 * 控制器基类
 * 此类集成各种事件的处理逻辑，包括
 * 1.播放器状态改变: {@link #handlePlayerStateChanged(int)}
 * 2.播放状态改变: {@link #handlePlayStateChanged(int)}
 * 3.控制视图的显示和隐藏: {@link #handleVisibilityChanged(boolean, Animation)}
 * 4.播放进度改变: {@link #handleSetProgress(int, int)}
 * 5.锁定状态改变: {@link #handleLockStateChanged(boolean)}
 * 6.设备方向监听: {@link #onOrientationChanged(int)}
 * Created by Doikki on 2017/4/12.
 */
public abstract class BaseVideoController extends FrameLayout
        implements IVideoController,
        OrientationHelper.OnOrientationChangeListener {

    //播放器包装类，集合了MediaPlayerControl的api和IVideoController的api
    protected ControlWrapper mControlWrapper;

    @Nullable
    protected Activity mActivity;

    //控制器是否处于显示状态
    protected boolean mShowing;

    //是否处于锁定状态
    protected boolean mIsLocked;

    //播放视图隐藏超时
    protected int mDefaultTimeout = 4000;

    //是否开启根据屏幕方向进入/退出全屏
    private boolean mEnableOrientation;
    //屏幕方向监听辅助类
    protected OrientationHelper mOrientationHelper;

    //用户设置是否适配刘海屏
    private boolean mAdaptCutout;
    //是否有刘海
    private Boolean mHasCutout;
    //刘海的高度
    private int mCutoutHeight;

    //是否开始刷新进度
    private boolean mIsStartProgress;

    //保存了所有的控制组件
    protected LinkedHashMap<IControlComponent, Boolean> mControlComponents = new LinkedHashMap<>();

    private Animation mShowAnim;
    private Animation mHideAnim;

    public BaseVideoController(@NonNull Context context) {
        this(context, null);
    }

    public BaseVideoController(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public BaseVideoController(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    protected void initView() {
        if (getLayoutId() != 0) {
            LayoutInflater.from(getContext()).inflate(getLayoutId(), this, true);
        }
        mOrientationHelper = new OrientationHelper(getContext().getApplicationContext());
        mEnableOrientation = VideoViewManager.getConfig().mEnableOrientation;
        mAdaptCutout = VideoViewManager.getConfig().mAdaptCutout;

        mShowAnim = new AlphaAnimation(0f, 1f);
        mShowAnim.setDuration(300);
        mHideAnim = new AlphaAnimation(1f, 0f);
        mHideAnim.setDuration(300);

        mActivity = PlayerUtils.scanForActivity(getContext());
    }

    /**
     * 设置控制器布局文件，子类必须实现
     */
    protected abstract int getLayoutId();

    /**
     * 重要：此方法用于将{@link VideoView} 和控制器绑定
     */
    @CallSuper
    public void setMediaPlayer(MediaPlayerControl mediaPlayer) {
        mControlWrapper = new ControlWrapper(mediaPlayer, this);
        //绑定ControlComponent和Controller
        for (Map.Entry<IControlComponent, Boolean> next : mControlComponents.entrySet()) {
            IControlComponent component = next.getKey();
            component.attach(mControlWrapper);
        }
        //开始监听设备方向
        mOrientationHelper.setOnOrientationChangeListener(this);
    }

    /**
     * 添加控制组件，最后面添加的在最下面，合理组织添加顺序，可让ControlComponent位于不同的层级
     */
    public void addControlComponent(IControlComponent... component) {
        for (IControlComponent item : component) {
            addControlComponent(item, false);
        }
    }

    /**
     * 添加控制组件，最后面添加的在最下面，合理组织添加顺序，可让ControlComponent位于不同的层级
     *
     * @param isDissociate 是否为游离的控制组件，
     *                     如果为 true ControlComponent 不会添加到控制器中，ControlComponent 将独立于控制器而存在，
     *                     如果为 false ControlComponent 将会被添加到控制器中，并显示出来。
     *                     为什么要让 ControlComponent 将独立于控制器而存在，假设有如下几种情况：
     *                     情况一：
     *                     如果在一个列表中控制器是复用的，但是控制器的某些部分是不能复用的，比如封面图，
     *                     此时你就可以将封面图拆分成一个游离的 ControlComponent，并把这个 ControlComponent
     *                     放在 item 的布局中，就可以实现每个item的封面图都是不一样，并且封面图可以随着播放器的状态显示和隐藏。
     *                     demo中演示的就是这种情况。
     *                     情况二：
     *                     假设有这样一种需求，播放器控制区域在显示区域的下面，此时你就可以通过自定义 ControlComponent
     *                     并将 isDissociate 设置为 true 来实现这种效果。
     */
    public void addControlComponent(IControlComponent component, boolean isDissociate) {
        mControlComponents.put(component, isDissociate);
        if (mControlWrapper != null) {
            component.attach(mControlWrapper);
        }
        View view = component.getView();
        if (view != null && !isDissociate) {
            addView(view, 0);
        }
    }

    /**
     * 移除某个控制组件
     */
    public void removeControlComponent(IControlComponent component) {
        removeView(component.getView());
        mControlComponents.remove(component);
    }

    /**
     * 移除所有控制组件
     */
    public void removeAllControlComponent() {
        for (Map.Entry<IControlComponent, Boolean> next : mControlComponents.entrySet()) {
            removeView(next.getKey().getView());
        }
        mControlComponents.clear();
    }

    /**
     * 移除所有的游离控制组件
     * 关于游离控制组件的定义请看 {@link #addControlComponent(IControlComponent, boolean)} 关于 isDissociate 的解释
     */
    public void removeAllDissociateComponents() {
        Iterator<Map.Entry<IControlComponent, Boolean>> it = mControlComponents.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<IControlComponent, Boolean> next = it.next();
            if (next.getValue()) {
                it.remove();
            }
        }
    }

    /**
     * {@link VideoView}调用此方法向控制器设置播放状态
     */
    @CallSuper
    public void setPlayState(int playState) {
        handlePlayStateChanged(playState);
    }

    /**
     * {@link VideoView}调用此方法向控制器设置播放器状态
     */
    @CallSuper
    public void setPlayerState(final int playerState) {
        handlePlayerStateChanged(playerState);
    }

    /**
     * 设置播放视图自动隐藏超时
     */
    public void setDismissTimeout(int timeout) {
        if (timeout > 0) {
            mDefaultTimeout = timeout;
        }
    }

    /**
     * 隐藏播放视图
     */
    @Override
    public void hide() {
        if (mShowing) {
            stopFadeOut();
            handleVisibilityChanged(false, mHideAnim);
            mShowing = false;
        }
    }

    /**
     * 显示播放视图
     */
    @Override
    public void show() {
        if (!mShowing) {
            handleVisibilityChanged(true, mShowAnim);
            startFadeOut();
            mShowing = true;
        }
    }

    @Override
    public boolean isShowing() {
        return mShowing;
    }

    /**
     * 开始计时
     */
    @Override
    public void startFadeOut() {
        //重新开始计时
        stopFadeOut();
        postDelayed(mFadeOut, mDefaultTimeout);
    }

    /**
     * 取消计时
     */
    @Override
    public void stopFadeOut() {
        removeCallbacks(mFadeOut);
    }

    /**
     * 隐藏播放视图Runnable
     */
    protected final Runnable mFadeOut = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    @Override
    public void setLocked(boolean locked) {
        mIsLocked = locked;
        handleLockStateChanged(locked);
    }

    @Override
    public boolean isLocked() {
        return mIsLocked;
    }

    /**
     * 开始刷新进度，注意：需在STATE_PLAYING时调用才会开始刷新进度
     */
    @Override
    public void startProgress() {
        if (mIsStartProgress) return;
        post(mShowProgress);
        mIsStartProgress = true;
    }

    /**
     * 停止刷新进度
     */
    @Override
    public void stopProgress() {
        if (!mIsStartProgress) return;
        removeCallbacks(mShowProgress);
        mIsStartProgress = false;
    }

    /**
     * 刷新进度Runnable
     */
    protected Runnable mShowProgress = new Runnable() {
        @Override
        public void run() {
            int pos = setProgress();
            if (mControlWrapper.isPlaying()) {
                postDelayed(this, (long) ((1000 - pos % 1000) / mControlWrapper.getSpeed()));
            } else {
                mIsStartProgress = false;
            }
        }
    };

    private int setProgress() {
        int position = (int) mControlWrapper.getCurrentPosition();
        int duration = (int) mControlWrapper.getDuration();
        handleSetProgress(duration, position);
        return position;
    }

    /**
     * 设置是否适配刘海屏
     */
    public void setAdaptCutout(boolean adaptCutout) {
        mAdaptCutout = adaptCutout;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        checkCutout();
    }

    /**
     * 检查是否需要适配刘海
     */
    private void checkCutout() {
        if (!mAdaptCutout) return;
        if (mActivity != null && mHasCutout == null) {
            mHasCutout = CutoutUtil.allowDisplayToCutout(mActivity);
            if (mHasCutout) {
                //竖屏下的状态栏高度可认为是刘海的高度
                mCutoutHeight = (int) PlayerUtils.getStatusBarHeightPortrait(mActivity);
            }
        }
        L.d("hasCutout: " + mHasCutout + " cutout height: " + mCutoutHeight);
    }

    /**
     * 是否有刘海屏
     */
    @Override
    public boolean hasCutout() {
        return mHasCutout != null && mHasCutout;
    }

    /**
     * 刘海的高度
     */
    @Override
    public int getCutoutHeight() {
        return mCutoutHeight;
    }

    /**
     * 显示移动网络播放提示
     *
     * @return 返回显示移动网络播放提示的条件，false:不显示, true显示
     * 此处默认根据手机网络类型来决定是否显示，开发者可以重写相关逻辑
     */
    public boolean showNetWarning() {
        return PlayerUtils.getNetworkType(getContext()) == PlayerUtils.NETWORK_MOBILE
                && !VideoViewManager.instance().playOnMobileNetwork();
    }

    /**
     * 播放和暂停
     */
    protected void togglePlay() {
        mControlWrapper.togglePlay();
    }

    /**
     * 横竖屏切换
     */
    protected void toggleFullScreen() {
        mControlWrapper.toggleFullScreen(mActivity);
    }

    /**
     * 子类中请使用此方法来进入全屏
     *
     * @return 是否成功进入全屏
     */
    protected boolean startFullScreen() {
        if (mActivity == null || mActivity.isFinishing()) return false;
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mControlWrapper.startFullScreen();
        return true;
    }

    /**
     * 子类中请使用此方法来退出全屏
     *
     * @return 是否成功退出全屏
     */
    protected boolean stopFullScreen() {
        if (mActivity == null || mActivity.isFinishing()) return false;
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mControlWrapper.stopFullScreen();
        return true;
    }

    /**
     * 改变返回键逻辑，用于activity
     */
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (mControlWrapper.isPlaying()
                && (mEnableOrientation || mControlWrapper.isFullScreen())) {
            if (hasWindowFocus) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mOrientationHelper.enable();
                    }
                }, 800);
            } else {
                mOrientationHelper.disable();
            }
        }
    }

    /**
     * 是否自动旋转， 默认不自动旋转
     */
    public void setEnableOrientation(boolean enableOrientation) {
        mEnableOrientation = enableOrientation;
    }

    private int mOrientation = 0;

    @CallSuper
    @Override
    public void onOrientationChanged(int orientation) {
        if (mActivity == null || mActivity.isFinishing()) return;

        //记录用户手机上一次放置的位置
        int lastOrientation = mOrientation;

        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
            //手机平放时，检测不到有效的角度
            //重置为原始位置 -1
            mOrientation = -1;
            return;
        }

        if (orientation > 350 || orientation < 10) {
            int o = mActivity.getRequestedOrientation();
            //手动切换横竖屏
            if (o == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE && lastOrientation == 0) return;
            if (mOrientation == 0) return;
            //0度，用户竖直拿着手机
            mOrientation = 0;
            onOrientationPortrait(mActivity);
        } else if (orientation > 80 && orientation < 100) {

            int o = mActivity.getRequestedOrientation();
            //手动切换横竖屏
            if (o == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT && lastOrientation == 90) return;
            if (mOrientation == 90) return;
            //90度，用户右侧横屏拿着手机
            mOrientation = 90;
            onOrientationReverseLandscape(mActivity);
        } else if (orientation > 260 && orientation < 280) {
            int o = mActivity.getRequestedOrientation();
            //手动切换横竖屏
            if (o == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT && lastOrientation == 270) return;
            if (mOrientation == 270) return;
            //270度，用户左侧横屏拿着手机
            mOrientation = 270;
            onOrientationLandscape(mActivity);
        }
    }

    /**
     * 竖屏
     */
    protected void onOrientationPortrait(Activity activity) {
        //屏幕锁定的情况
        if (mIsLocked) return;
        //没有开启设备方向监听的情况
        if (!mEnableOrientation) return;

        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mControlWrapper.stopFullScreen();
    }

    /**
     * 横屏
     */
    protected void onOrientationLandscape(Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        if (mControlWrapper.isFullScreen()) {
            handlePlayerStateChanged(VideoView.PLAYER_FULL_SCREEN);
        } else {
            mControlWrapper.startFullScreen();
        }
    }

    /**
     * 反向横屏
     */
    protected void onOrientationReverseLandscape(Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        if (mControlWrapper.isFullScreen()) {
            handlePlayerStateChanged(VideoView.PLAYER_FULL_SCREEN);
        } else {
            mControlWrapper.startFullScreen();
        }
    }

    //------------------------ start handle event change ------------------------//

    private void handleVisibilityChanged(boolean isVisible, Animation anim) {
        if (!mIsLocked) { //没锁住时才向ControlComponent下发此事件
            for (Map.Entry<IControlComponent, Boolean> next
                    : mControlComponents.entrySet()) {
                IControlComponent component = next.getKey();
                component.onVisibilityChanged(isVisible, anim);
            }
        }
        onVisibilityChanged(isVisible, anim);
    }

    /**
     * 子类重写此方法监听控制的显示和隐藏
     *
     * @param isVisible 是否可见
     * @param anim      显示/隐藏动画
     */
    protected void onVisibilityChanged(boolean isVisible, Animation anim) {

    }

    private void handlePlayStateChanged(int playState) {
        for (Map.Entry<IControlComponent, Boolean> next
                : mControlComponents.entrySet()) {
            IControlComponent component = next.getKey();
            component.onPlayStateChanged(playState);
        }
        onPlayStateChanged(playState);
    }

    /**
     * 子类重写此方法并在其中更新控制器在不同播放状态下的ui
     */
    @CallSuper
    protected void onPlayStateChanged(int playState) {
        switch (playState) {
            case VideoView.STATE_IDLE:
                mOrientationHelper.disable();
                mOrientation = 0;
                mIsLocked = false;
                mShowing = false;
                //由于游离组件是独立于控制器存在的，
                //所以在播放器release的时候需要移除
                removeAllDissociateComponents();
                break;
            case VideoView.STATE_PLAYBACK_COMPLETED:
                mIsLocked = false;
                mShowing = false;
                break;
            case VideoView.STATE_ERROR:
                mShowing = false;
                break;
        }
    }

    private void handlePlayerStateChanged(int playerState) {
        for (Map.Entry<IControlComponent, Boolean> next
                : mControlComponents.entrySet()) {
            IControlComponent component = next.getKey();
            component.onPlayerStateChanged(playerState);
        }
        onPlayerStateChanged(playerState);
    }

    /**
     * 子类重写此方法并在其中更新控制器在不同播放器状态下的ui
     */
    @CallSuper
    protected void onPlayerStateChanged(int playerState) {
        switch (playerState) {
            case VideoView.PLAYER_NORMAL:
                if (mEnableOrientation) {
                    mOrientationHelper.enable();
                } else {
                    mOrientationHelper.disable();
                }
                if (hasCutout()) {
                    CutoutUtil.adaptCutoutAboveAndroidP(getContext(), false);
                }
                break;
            case VideoView.PLAYER_FULL_SCREEN:
                //在全屏时强制监听设备方向
                mOrientationHelper.enable();
                if (hasCutout()) {
                    CutoutUtil.adaptCutoutAboveAndroidP(getContext(), true);
                }
                break;
            case VideoView.PLAYER_TINY_SCREEN:
                mOrientationHelper.disable();
                break;
        }
    }

    private void handleSetProgress(int duration, int position) {
        for (Map.Entry<IControlComponent, Boolean> next
                : mControlComponents.entrySet()) {
            IControlComponent component = next.getKey();
            component.setProgress(duration, position);
        }
        setProgress(duration, position);
    }

    /**
     * 刷新进度回调，子类可在此方法监听进度刷新，然后更新ui
     *
     * @param duration 视频总时长
     * @param position 视频当前时长
     */
    protected void setProgress(int duration, int position) {

    }

    private void handleLockStateChanged(boolean isLocked) {
        for (Map.Entry<IControlComponent, Boolean> next
                : mControlComponents.entrySet()) {
            IControlComponent component = next.getKey();
            component.onLockStateChanged(isLocked);
        }
        onLockStateChanged(isLocked);
    }

    /**
     * 子类可重写此方法监听锁定状态发生改变，然后更新ui
     */
    protected void onLockStateChanged(boolean isLocked) {

    }

    //------------------------ end handle event change ------------------------//
}
