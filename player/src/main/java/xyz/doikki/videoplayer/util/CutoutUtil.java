package xyz.doikki.videoplayer.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.view.DisplayCutout;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 刘海屏工具
 */
public final class CutoutUtil {

    private CutoutUtil() {
    }

    /**
     * 是否为允许全屏界面显示内容到刘海区域的刘海屏机型（与AndroidManifest中配置对应）
     */
    public static boolean allowDisplayToCutout(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // 9.0系统全屏界面默认会保留黑边，不允许显示内容到刘海区域
            Window window = activity.getWindow();
            WindowInsets windowInsets = window.getDecorView().getRootWindowInsets();
            if (windowInsets == null) {
                return false;
            }
            DisplayCutout displayCutout = windowInsets.getDisplayCutout();
            if (displayCutout == null) {
                return false;
            }
            List<Rect> boundingRects = displayCutout.getBoundingRects();
            return boundingRects.size() > 0;
        } else {
            return hasCutoutHuawei(activity)
                    || hasCutoutOPPO(activity)
                    || hasCutoutVIVO(activity)
                    || hasCutoutXIAOMI(activity);
        }
    }

    /**
     * 是否是华为刘海屏机型
     */
    @SuppressWarnings("unchecked")
    private static boolean hasCutoutHuawei(Activity activity) {
        if (!Build.MANUFACTURER.equalsIgnoreCase("HUAWEI")) {
            return false;
        }
        try {
            ClassLoader cl = activity.getClassLoader();
            Class HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            if (HwNotchSizeUtil != null) {
                Method get = HwNotchSizeUtil.getMethod("hasNotchInScreen");
                return (boolean) get.invoke(HwNotchSizeUtil);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 是否是oppo刘海屏机型
     */
    private static boolean hasCutoutOPPO(Activity activity) {
        if (!Build.MANUFACTURER.equalsIgnoreCase("oppo")) {
            return false;
        }
        return activity.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
    }

    /**
     * 是否是vivo刘海屏机型
     */
    @SuppressWarnings("unchecked")
    @SuppressLint("PrivateApi")
    private static boolean hasCutoutVIVO(Activity activity) {
        if (!Build.MANUFACTURER.equalsIgnoreCase("vivo")) {
            return false;
        }
        try {
            ClassLoader cl = activity.getClassLoader();
            Class ftFeatureUtil = cl.loadClass("android.util.FtFeature");
            if (ftFeatureUtil != null) {
                Method get = ftFeatureUtil.getMethod("isFeatureSupport", int.class);
                return (boolean) get.invoke(ftFeatureUtil, 0x00000020);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 是否是小米刘海屏机型
     */
    @SuppressWarnings("unchecked")
    @SuppressLint("PrivateApi")
    private static boolean hasCutoutXIAOMI(Activity activity) {
        if (!Build.MANUFACTURER.equalsIgnoreCase("xiaomi")) {
            return false;
        }
        try {
            ClassLoader cl = activity.getClassLoader();
            Class SystemProperties = cl.loadClass("android.os.SystemProperties");
            Class[] paramTypes = new Class[2];
            paramTypes[0] = String.class;
            paramTypes[1] = int.class;
            Method getInt = SystemProperties.getMethod("getInt", paramTypes);
            //参数
            Object[] params = new Object[2];
            params[0] = "ro.miui.notch";
            params[1] = 0;
            int hasCutout = (int) getInt.invoke(SystemProperties, params);
            return hasCutout == 1;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 适配刘海屏，针对Android P以上系统
     */
    public static void adaptCutoutAboveAndroidP(Context context, boolean isAdapt) {
        Activity activity = PlayerUtils.scanForActivity(context);
        if (activity == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
            if (isAdapt) {
                lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            } else {
                lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
            }
            activity.getWindow().setAttributes(lp);
        }
    }

}
