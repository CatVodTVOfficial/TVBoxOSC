package com.github.tvbox.osc.util.thunder;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;

import com.xunlei.downloadlib.XLDownloadManager;
import com.xunlei.downloadlib.XLTaskHelper;
import com.xunlei.downloadlib.android.XLUtil;
import com.xunlei.downloadlib.parameter.TorrentFileInfo;
import com.xunlei.downloadlib.parameter.TorrentInfo;
import com.xunlei.downloadlib.parameter.XLTaskInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Thunder {

    private static String cacheRoot = "";
    private static long currentTask = 0L;
    private static ArrayList<TorrentFileInfo> torrentFileInfoArrayList = null;
    private static ExecutorService threadPool = null;


    private static void init(Context context) {
        // fake deviceId and Mac
        SharedPreferences sharedPreferences = context.getSharedPreferences("rand_thunder_id", Context.MODE_PRIVATE);
        String imei = sharedPreferences.getString("imei", null);
        String mac = sharedPreferences.getString("mac", null);
        if (imei == null) {
            imei = randomImei();
            sharedPreferences.edit().putString("imei", imei).commit();
        }
        if (mac == null) {
            mac = randomMac();
            sharedPreferences.edit().putString("mac", mac).commit();
        }

        XLUtil.mIMEI = imei;
        XLUtil.isGetIMEI = true;
        XLUtil.mMAC = mac;
        XLUtil.isGetMAC = true;
        String cd3 = "cee25055f125a2fde0";
        String base64Decode = "axzNjAwMQ^^yb==0^852^083dbcff^";
        String substring = base64Decode.substring(1);
        String substring2 = cd3.substring(0, cd3.length() - 1);
        String cd = substring + substring2;
        XLTaskHelper.init(context, cd, "21.01.07.800002");
        cacheRoot = context.getCacheDir().getAbsolutePath() + File.separator + "thunder";
    }

    public static void stop() {
        if (currentTask > 0) {
            XLTaskHelper.instance().stopTask(currentTask);
            currentTask = 0L;
        }
        torrentFileInfoArrayList = null;
        // del cache file
        File cache = new File(cacheRoot);
        recursiveDelete(cache);
        if (!cache.exists())
            cache.mkdirs();
        if (threadPool != null) {
            try {
                threadPool.shutdownNow();
                threadPool = null;
            } catch (Throwable th) {

            }
        }
    }

    public interface ThunderCallback {

        void status(int code, String info);

        void list(String playList);

        void play(String url);
    }

    public static void parse(Context context, String url, ThunderCallback callback) {
        init(context);
        stop();
        threadPool = Executors.newSingleThreadExecutor();
        if (isMagnet(url) || isThunder(url)) {
            String link = isThunder(url) ? XLDownloadManager.getInstance().parserThunderUrl(url) : url;
            Uri p = Uri.parse(link);
            if (p == null) {
                callback.status(-1, "链接错误");
                return;
            }
            String fileName = XLTaskHelper.instance().getFileName(link);
            File cache = new File(cacheRoot + File.separator + fileName);
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        currentTask = isMagnet(url) ?
                                XLTaskHelper.instance().addMagentTask(url, cacheRoot, fileName) :
                                XLTaskHelper.instance().addThunderTask(url, cacheRoot, fileName);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        currentTask = 0;
                    }
                    if (currentTask <= 0) {
                        callback.status(-1, "链接错误");
                        return;
                    }
                    int count = 30;
                    while (true) {
                        count--;
                        if (count <= 0) {
                            callback.status(-1, "解析超时");
                            break;
                        }
                        XLTaskInfo taskInfo = XLTaskHelper.instance().getTaskInfo(currentTask);
                        switch (taskInfo.mTaskStatus) {
                            case 2: {
                                callback.status(0, "正在获取文件列表...");
                                try {
                                    TorrentInfo torrentInfo = XLTaskHelper.instance().getTorrentInfo(cache.getAbsolutePath());
                                    if (torrentInfo == null || TextUtils.isEmpty(torrentInfo.mInfoHash)) {
                                        callback.status(-1, "解析失败");
                                    } else {
                                        TorrentFileInfo[] mSubFileInfo = torrentInfo.mSubFileInfo;
                                        ArrayList<String> playList = new ArrayList<>();
                                        ArrayList<TorrentFileInfo> list = new ArrayList<>();
                                        if (mSubFileInfo != null && mSubFileInfo.length >= 0) {
                                            for (TorrentFileInfo sub : mSubFileInfo) {
                                                if (isMedia(sub.mFileName)) {
                                                    sub.torrentPath = cache.getAbsolutePath();
                                                    playList.add(sub.mFileName + "$tvbox-torrent:" + list.size());
                                                    list.add(sub);
                                                }
                                            }
                                        }
                                        if (list.size() > 0) {
                                            torrentFileInfoArrayList = list;
                                            callback.list(TextUtils.join("#", playList));
                                        } else {
                                            callback.status(-1, "文件列表为空!");
                                        }
                                    }
                                } catch (Throwable throwable) {
                                    throwable.printStackTrace();
                                    callback.status(-1, "解析失败");
                                }
                                return;
                            }
                            case 3: {
                                callback.status(-1, "解析失败");
                                return;
                            }
                            default: {
                                callback.status(0, "解析中...");
                                break;
                            }
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }


    public static boolean play(String url, ThunderCallback callback) {
        if (url.startsWith("tvbox-torrent:")) {
            int idx = Integer.parseInt(url.substring(14));
            TorrentFileInfo info = torrentFileInfoArrayList.get(idx);
            if (currentTask > 0) {
                XLTaskHelper.instance().stopTask(currentTask);
                currentTask = 0L;
            }
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    String torrentName = new File(info.torrentPath).getName();
                    String cache = cacheRoot + File.separator + torrentName.substring(0, torrentName.lastIndexOf("."));
                    currentTask = XLTaskHelper.instance().addTorrentTask(info.torrentPath, cache, info.mFileIndex);
                    if (currentTask < 0)
                        callback.status(-1, "下载出错");
                    int count = 30;
                    while (true) {
                        count--;
                        if (count <= 0) {
                            callback.status(-1, "解析下载超时");
                            break;
                        }
                        XLTaskInfo taskInfo = XLTaskHelper.instance().getBtSubTaskInfo(currentTask, info.mFileIndex).mTaskInfo;
                        switch (taskInfo.mTaskStatus) {
                            case 3: {
                                callback.status(-1, errorInfo(taskInfo.mErrorCode));
                                return;
                            }
                            case 1:
                            case 4: // 下载中
                            case 2: { // 下载完成
                                String pUrl = XLTaskHelper.instance().getLoclUrl(cache + File.separator + info.mFileName);
                                callback.play(pUrl);
                                return;
                            }
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            return true;
        }
        return false;
    }

    private static String errorInfo(int code) {
        switch (code) {
            case 9125:
                return "文件名太长";
            case 111120:
                return "文件路径太长";
            case 111142:
                return "文件太小";
            case 111085:
                return "磁盘空间不足";
            case 111171:
                return "拒绝的网络连接";
            case 9301:
                return "缓冲区不足";
            case 114001:
            case 114004:
            case 114005:
            case 114006:
            case 114007:
            case 114011:
            case 9304:
            case 111154:
                return "版权限制：无权下载";
            case 114101:
                return "无效链接";
            default:
                return "ErrorCode=" + code;
        }
    }


    public static boolean isSupportUrl(String url) {
        return isMagnet(url) || isThunder(url)/* || isTorrent(url) || isEd2k(url)*/;
    }

    private static boolean isMagnet(String url) {
        return url.toLowerCase().startsWith("magnet:");
    }

    private static boolean isThunder(String url) {
        return url.toLowerCase().startsWith("thunder");
    }

    private static boolean isTorrent(String url) {
        return url.toLowerCase().split(";")[0].endsWith(".torrent");
    }

    private static boolean isEd2k(String url) {
        return url.toLowerCase().startsWith("ed2k:");
    }

    static void recursiveDelete(File file) {
        if (!file.exists())
            return;
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                recursiveDelete(f);
            }
        }
        file.delete();
    }

    static ArrayList<String> formats = new ArrayList<>();

    static boolean isMedia(String name) {
        if (formats.size() == 0) {
            formats.add(".rmvb");
            formats.add(".avi");
            formats.add(".mkv");
            formats.add(".flv");
            formats.add(".mp4");
            formats.add(".rm");
            formats.add(".vob");
            formats.add(".wmv");
            formats.add(".mov");
            formats.add(".3gp");
            formats.add(".asf");
            formats.add("mpg");
            formats.add("mpeg");
            formats.add("mpe");
        }
        for (String f : formats) {
            if (name.toLowerCase().endsWith(f))
                return true;

        }
        return false;
    }

    static String randomImei() {
        return randomString("0123456", 15);
    }

    static String randomMac() {
        return randomString("ABCDEF0123456", 12).toUpperCase();
    }

    static String randomString(String base, int length) {
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

}
