package xyz.doikki.videoplayer.util;

import android.util.Log;

import xyz.doikki.videoplayer.player.VideoViewManager;

/**
 * 日志类
 * Created by Doikki on 2017/6/5.
 */

public final class L {

    private L() {
    }

    private static final String TAG = "DKPlayer";

    private static boolean isDebug = VideoViewManager.getConfig().mIsEnableLog;


    public static void d(String msg) {
        if (isDebug) {
            Log.d(TAG, msg);
        }
    }

    public static void e(String msg) {
        if (isDebug) {
            Log.e(TAG, msg);
        }
    }

    public static void i(String msg) {
        if (isDebug) {
            Log.i(TAG, msg);
        }
    }

    public static void w(String msg) {
        if (isDebug) {
            Log.w(TAG, msg);
        }
    }

    public static void setDebug(boolean isDebug) {
        L.isDebug = isDebug;
    }
}
