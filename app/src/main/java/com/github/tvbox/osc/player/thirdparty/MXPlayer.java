package com.github.tvbox.osc.player.thirdparty;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;

import com.github.tvbox.osc.base.App;

import java.util.HashMap;

public class MXPlayer {
    public static final String TAG = "ThirdParty.MXPlayer";

    private static final String PACKAGE_NAME_PRO = "com.mxtech.videoplayer.pro";
    private static final String PACKAGE_NAME_AD = "com.mxtech.videoplayer.ad";
    private static final String PLAYBACK_ACTIVITY_PRO = "com.mxtech.videoplayer.ActivityScreen";
    private static final String PLAYBACK_ACTIVITY_AD = "com.mxtech.videoplayer.ad.ActivityScreen";

    private static class MXPackageInfo {
        final String packageName;
        final String activityName;

        MXPackageInfo(String packageName, String activityName) {
            this.packageName = packageName;
            this.activityName = activityName;
        }
    }

    private static final MXPackageInfo[] PACKAGES = {
            new MXPackageInfo(PACKAGE_NAME_PRO, PLAYBACK_ACTIVITY_PRO),
            new MXPackageInfo(PACKAGE_NAME_AD, PLAYBACK_ACTIVITY_AD),
    };

    /**
     * @return null if any MX Player packages not exist.
     */
    public static MXPackageInfo getPackageInfo() {
        for (MXPackageInfo pkg : PACKAGES) {
            try {
                ApplicationInfo info = App.getInstance().getPackageManager().getApplicationInfo(pkg.packageName, 0);
                if (info.enabled)
                    return pkg;
                else
                    Log.v(TAG, "MX Player package `" + pkg.packageName + "` is disabled.");
            } catch (PackageManager.NameNotFoundException ex) {
                Log.v(TAG, "MX Player package `" + pkg.packageName + "` does not exist.");
            }
        }
        return null;
    }

    private static class Subtitle {
        final Uri uri;
        String name;
        String filename;

        Subtitle(Uri uri) {
            if (uri.getScheme() == null)
                throw new IllegalStateException("Scheme is missed for subtitle URI " + uri);

            this.uri = uri;
        }

        Subtitle(String uriStr) {
            this(Uri.parse(uriStr));
        }
    }


    public static boolean run(Activity activity, String url, String title, String subtitle, HashMap<String, String> headers) {
        MXPackageInfo packageInfo = getPackageInfo();
        if (packageInfo == null)
            return false;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setPackage(packageInfo.packageName);
        intent.setClassName(packageInfo.packageName, packageInfo.activityName);

        intent.setData(Uri.parse(url));
        intent.putExtra("title", title);
        if (headers != null && headers.size() > 0) {
            String[] hds = new String[headers.size() * 2];
            int idx = 0;
            for (String hk : headers.keySet()) {
                hds[idx] = hk;
                hds[idx + 1] = headers.get(hk).trim();
                idx += 2;
            }
            intent.putExtra("headers", headers);
        }

        if (subtitle != null && !subtitle.isEmpty()) {
            Parcelable[] parcels = new Parcelable[1];
            parcels[0] = Uri.parse(subtitle);
            intent.putExtra("subs", parcels);
            intent.putExtra("subs.enable", parcels);
        }

        try {
            activity.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException ex) {
            Log.e(TAG, "Can't run MX Player(Pro)", ex);
            return false;
        }
    }
}
