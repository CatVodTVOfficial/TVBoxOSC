package com.github.tvbox.osc.util;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import org.xwalk.core.XWalkInitializer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class XWalkUtils {

    private static XWalkInitializer xWalkInitializer = null;

    public interface XWalkState {
        void success();

        void fail();

        void ignore();
    }

    public static String downUrl() {
        return String.format("https://download.01.org/crosswalk/releases/crosswalk/android/stable/23.53.589.4/%s/crosswalk-apks-23.53.589.4-%s.zip", getRuntimeAbi(), getRuntimeAbi());
    }

    public static String saveZipFile() {
        return String.format("crosswalk-apks-23.53.589.4-%s.zip", getRuntimeAbi());
    }

    public static boolean xWalkLibExist(Context context) {
        String[] libFiles = new String[]{
                "classes.dex",
                "icudtl.dat",
                "libxwalkcore.so",
                "xwalk.pak",
                "xwalk_100_percent.pak"
        };
        String dir = libExtractPath(context);
        for (String lib : libFiles) {
            if (!new File(dir + "/" + lib).exists())
                return false;
        }
        return true;
    }

    public static void tryUseXWalk(Context context, XWalkState state) {
        if (!xWalkLibExist(context)) {
            state.ignore();
            return;
        }
        if (xWalkInitializer == null) {
            xWalkInitializer = new XWalkInitializer(new XWalkInitializer.XWalkInitListener() {
                @Override
                public void onXWalkInitStarted() {

                }

                @Override
                public void onXWalkInitCancelled() {

                }

                @Override
                public void onXWalkInitFailed() {
                    if (state != null)
                        state.fail();
                }

                @Override
                public void onXWalkInitCompleted() {
                    if (state != null)
                        state.success();
                }
            }, context);
        }
        if (xWalkInitializer.isXWalkReady()) {
            state.success();
        } else {
            xWalkInitializer.initAsync();
        }
    }

    private static boolean checkEmbedded(Activity activity) {
        try {
            Class clazz = Class.forName("org.xwalk.core.XWalkCoreWrapper");
            if (clazz != null) {
                Constructor constructor = clazz.getDeclaredConstructor(new Class[]{Context.class, int.class});
                constructor.setAccessible(true);
                Object obj = constructor.newInstance(activity, -1);
                Method fe = clazz.getDeclaredMethod("findEmbeddedCore", new Class[]{});
                fe.setAccessible(true);
                if (!(boolean) fe.invoke(obj)) {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean apkHadDown(Context context) {
        return new File(apkPath(context)).exists();
    }

    public static void unzipXWalkZip(Context context, String archive) throws Throwable {
        BufferedInputStream bi;
        ZipFile zf = new ZipFile(archive);
        Enumeration e = zf.entries();
        while (e.hasMoreElements()) {
            ZipEntry ze2 = (ZipEntry) e.nextElement();
            String entryName = ze2.getName();
            if (ze2.isDirectory()) {
                continue;
            } else {
                String fileName = entryName.substring(entryName.lastIndexOf("/") + 1, entryName.length());
                if (fileName.equals("XWalkRuntimeLib.apk")) {
                    String tempFile = apkPath(context) + ".tmp";
                    String finalApk = apkPath(context);
                    File f = new File(tempFile);
                    if (f.exists())
                        f.delete();
                    f = new File(finalApk);
                    if (f.exists())
                        f.delete();
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tempFile));
                    bi = new BufferedInputStream(zf.getInputStream(ze2));
                    byte[] readContent = new byte[1024];
                    int readCount = bi.read(readContent);
                    while (readCount != -1) {
                        bos.write(readContent, 0, readCount);
                        readCount = bi.read(readContent);
                    }
                    bos.flush();
                    bos.close();
                    if (new File(tempFile).renameTo(new File(finalApk))) {
                        LOG.i(finalApk);
                    }
                }
            }
        }
        zf.close();
        File zipFile = new File(archive);
        if (zipFile.exists() && zipFile.getName().endsWith(".zip")) {
            zipFile.delete();
        }
    }

    public static String getRuntimeAbi() {
        String result = "arm";
        try {
            Class cls = Class.forName("org.xwalk.core.XWalkEnvironment");
            Method method = cls.getMethod("getRuntimeAbi");
            String obj = (String) method.invoke(null);
            switch (obj) {
                case "arm64-v8a": {
                    result = "arm64";
                    break;
                }
                case "x86": {
                    result = "x86";
                    break;
                }
                case "x86_64": {
                    result = "x86_64";
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean extractXWalkLib(Context context) throws Throwable {
        Class cls = Class.forName("org.xwalk.core.XWalkDecompressor");
        Method method = cls.getMethod("extractResource", String.class, String.class);
        boolean obj = (boolean) method.invoke(null, apkPath(context), libExtractPath(context));
        if (obj) {
            Toast.makeText(context, "解压XWalkView运行组件完成!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "解压XWalkView运行组件失败!", Toast.LENGTH_LONG).show();
        }
        return obj;
    }

    static String apkPath(Context context) {
        return context.getCacheDir().getAbsolutePath() + "/XWalkRuntimeLib.apk";
    }

    static String libExtractPath(Context context) {
        // XWalkEnvironment.getExtractedCoreDir
        return context.getDir(/*XWALK_CORE_EXTRACTED_DIR*/"extracted_xwalkcore", Context.MODE_PRIVATE).getAbsolutePath();
    }

}
