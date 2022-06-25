package xyz.doikki.videoplayer.player;

import android.app.Application;

import xyz.doikki.videoplayer.util.L;

import java.util.LinkedHashMap;

/**
 * 视频播放器管理器，管理当前正在播放的VideoView，以及播放器配置
 * 你也可以用来保存常驻内存的VideoView，但是要注意通过Application Context创建，
 * 以免内存泄漏
 */
public class VideoViewManager {

    /**
     * 保存VideoView的容器
     */
    private LinkedHashMap<String, VideoView> mVideoViews = new LinkedHashMap<>();

    /**
     * 是否在移动网络下直接播放视频
     */
    private boolean mPlayOnMobileNetwork;

    /**
     * VideoViewManager实例
     */
    private static VideoViewManager sInstance;

    /**
     * VideoViewConfig实例
     */
    private static VideoViewConfig sConfig;

    private VideoViewManager() {
        mPlayOnMobileNetwork = getConfig().mPlayOnMobileNetwork;
    }

    /**
     * 设置VideoViewConfig
     */
    public static void setConfig(VideoViewConfig config) {
        if (sConfig == null) {
            synchronized (VideoViewConfig.class) {
                if (sConfig == null) {
                    sConfig = config == null ? VideoViewConfig.newBuilder().build() : config;
                }
            }
        }
    }

    /**
     * 获取VideoViewConfig
     */
    public static VideoViewConfig getConfig() {
        setConfig(null);
        return sConfig;
    }

    /**
     * 获取是否在移动网络下直接播放视频配置
     */
    public boolean playOnMobileNetwork() {
        return mPlayOnMobileNetwork;
    }

    /**
     * 设置是否在移动网络下直接播放视频
     */
    public void setPlayOnMobileNetwork(boolean playOnMobileNetwork) {
        mPlayOnMobileNetwork = playOnMobileNetwork;
    }

    public static VideoViewManager instance() {
        if (sInstance == null) {
            synchronized (VideoViewManager.class) {
                if (sInstance == null) {
                    sInstance = new VideoViewManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * 添加VideoView
     * @param tag 相同tag的VideoView只会保存一个，如果tag相同则会release并移除前一个
     */
    public void add(VideoView videoView, String tag) {
        if (!(videoView.getContext() instanceof Application)) {
            L.w("The Context of this VideoView is not an Application Context," +
                    "you must remove it after release,or it will lead to memory leek.");
        }
        VideoView old = get(tag);
        if (old != null) {
            old.release();
            remove(tag);
        }
        mVideoViews.put(tag, videoView);
    }

    public VideoView get(String tag) {
        return mVideoViews.get(tag);
    }

    public void remove(String tag) {
        mVideoViews.remove(tag);
    }

    public void removeAll() {
        mVideoViews.clear();
    }

    /**
     * 释放掉和tag关联的VideoView，并将其从VideoViewManager中移除
     */
    public void releaseByTag(String tag) {
        releaseByTag(tag, true);
    }

    public void releaseByTag(String tag, boolean isRemove) {
        VideoView videoView = get(tag);
        if (videoView != null) {
            videoView.release();
            if (isRemove) {
                remove(tag);
            }
        }
    }

    public boolean onBackPress(String tag) {
        VideoView videoView = get(tag);
        if (videoView == null) return false;
        return videoView.onBackPressed();
    }

}
