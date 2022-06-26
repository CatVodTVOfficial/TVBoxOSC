package xyz.doikki.videoplayer.controller;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

/**
 * 此类的目的是为了在ControlComponent中既能调用VideoView的api又能调用BaseVideoController的api，
 * 并对部分api做了封装，方便使用
 */
public class ControlWrapper implements MediaPlayerControl, IVideoController {
    
    private MediaPlayerControl mPlayerControl;
    private IVideoController mController;
    
    public ControlWrapper(@NonNull MediaPlayerControl playerControl, @NonNull IVideoController controller) {
        mPlayerControl = playerControl;
        mController = controller;
    }
    
    @Override
    public void start() {
        mPlayerControl.start();
    }

    @Override
    public void pause() {
        mPlayerControl.pause();
    }

    @Override
    public long getDuration() {
        return mPlayerControl.getDuration();
    }

    @Override
    public long getCurrentPosition() {
        return mPlayerControl.getCurrentPosition();
    }

    @Override
    public void seekTo(long pos) {
        mPlayerControl.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return mPlayerControl.isPlaying();
    }

    @Override
    public int getBufferedPercentage() {
        return mPlayerControl.getBufferedPercentage();
    }

    @Override
    public void startFullScreen() {
        mPlayerControl.startFullScreen();
    }

    @Override
    public void stopFullScreen() {
        mPlayerControl.stopFullScreen();
    }

    @Override
    public boolean isFullScreen() {
        return mPlayerControl.isFullScreen();
    }

    @Override
    public void setMute(boolean isMute) {
        mPlayerControl.setMute(isMute);
    }

    @Override
    public boolean isMute() {
        return mPlayerControl.isMute();
    }

    @Override
    public void setScreenScaleType(int screenScaleType) {
        mPlayerControl.setScreenScaleType(screenScaleType);
    }

    @Override
    public void setSpeed(float speed) {
        mPlayerControl.setSpeed(speed);
    }

    @Override
    public float getSpeed() {
        return mPlayerControl.getSpeed();
    }

    @Override
    public long getTcpSpeed() {
        return mPlayerControl.getTcpSpeed();
    }

    @Override
    public void replay(boolean resetPosition) {
        mPlayerControl.replay(resetPosition);
    }

    @Override
    public void setMirrorRotation(boolean enable) {
        mPlayerControl.setMirrorRotation(enable);
    }

    @Override
    public Bitmap doScreenShot() {
        return mPlayerControl.doScreenShot();
    }

    @Override
    public int[] getVideoSize() {
        return mPlayerControl.getVideoSize();
    }

    @Override
    public void setRotation(float rotation) {
        mPlayerControl.setRotation(rotation);
    }

    @Override
    public void startTinyScreen() {
        mPlayerControl.startTinyScreen();
    }

    @Override
    public void stopTinyScreen() {
        mPlayerControl.stopTinyScreen();
    }

    @Override
    public boolean isTinyScreen() {
        return mPlayerControl.isTinyScreen();
    }

    /**
     * 播放和暂停
     */
    public void togglePlay() {
        if (isPlaying()) {
            pause();
        } else {
            start();
        }
    }

    /**
     * 横竖屏切换，会旋转屏幕
     */
    public void toggleFullScreen(Activity activity) {
        if (activity == null || activity.isFinishing())
            return;
        if (isFullScreen()) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            stopFullScreen();
        } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            startFullScreen();
        }
    }

    /**
     * 横竖屏切换，不会旋转屏幕
     */
    public void toggleFullScreen() {
        if (isFullScreen()) {
            stopFullScreen();
        } else {
            startFullScreen();
        }
    }

    /**
     * 横竖屏切换，根据适配宽高决定是否旋转屏幕
     */
    public void toggleFullScreenByVideoSize(Activity activity) {
        if (activity == null || activity.isFinishing())
            return;
        int[] size = getVideoSize();
        int width = size[0];
        int height = size[1];
        if (isFullScreen()) {
            stopFullScreen();
            if (width > height) {
               activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } else {
            startFullScreen();
            if (width > height) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
    }

    @Override
    public void startFadeOut() {
        mController.startFadeOut();
    }

    @Override
    public void stopFadeOut() {
        mController.stopFadeOut();
    }

    @Override
    public boolean isShowing() {
        return mController.isShowing();
    }

    @Override
    public void setLocked(boolean locked) {
        mController.setLocked(locked);
    }

    @Override
    public boolean isLocked() {
        return mController.isLocked();
    }

    @Override
    public void startProgress() {
        mController.startProgress();
    }

    @Override
    public void stopProgress() {
        mController.stopProgress();
    }

    @Override
    public void hide() {
        mController.hide();
    }

    @Override
    public void show() {
        mController.show();
    }

    @Override
    public boolean hasCutout() {
        return mController.hasCutout();
    }

    @Override
    public int getCutoutHeight() {
        return mController.getCutoutHeight();
    }

    /**
     * 切换锁定状态
     */
    public void toggleLockState() {
        setLocked(!isLocked());
    }


    /**
     * 切换显示/隐藏状态
     */
    public void toggleShowState() {
        if (isShowing()) {
            hide();
        } else {
            show();
        }
    }
}
