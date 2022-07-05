package com.github.tvbox.osc.player.thirdparty;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;

import com.github.tvbox.osc.base.App;

public class MXPlayer {
    public static final String TAG = "ThirdParty.MXPlayer";

    public static final String RESULT_VIEW = "com.mxtech.intent.result.VIEW";
    public static final int RESULT_ERROR = Activity.RESULT_FIRST_USER + 0;

    public static final String EXTRA_DECODER = "decode_mode";    // (byte)
    public static final String EXTRA_VIDEO = "video";
    public static final String EXTRA_EXPLICIT_LIST = "video_list_is_explicit";
    public static final String EXTRA_DURATION = "duration";
    public static final String EXTRA_SUBTITLES = "subs";
    public static final String EXTRA_SUBTITLE_NAMES = "subs.name";
    public static final String EXTRA_SUBTITLE_FILENAMES = "subs.filename";
    public static final String EXTRA_ENABLED_SUBTITLES = "subs.enable";
    public static final String EXTRA_POSITION = "position";
    public static final String EXTRA_RETURN_RESULT = "return_result";
    public static final String EXTRA_HEADERS = "headers";
    public static final String EXTRA_END_BY = "end_by";
    public static final String EXTRA_VIDEO_ZOOM = "video_zoom";
    public static final String EXTRA_DAR_HORZ = "DAR_horz";
    public static final String EXTRA_DAR_VERT = "DAR_vert";
    public static final String EXTRA_STICKY = "sticky";
    public static final String EXTRA_ORIENTATION = "orientation";
    public static final String EXTRA_SUPPRESS_ERROR_MESSAGE = "suppress_error_message";
    public static final String EXTRA_SECURE_URI = "secure_uri";
    public static final String EXTRA_KEYS_DPAD_UPDOWN = "keys.dpad_up_down";

    public static final String EXTRA_LIST = "video_list";

    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_TITLES = "video_list.name";

    public static final String EXTRA_SIZE = "size";
    public static final String EXTRA_SIZES = "video_list.size";

    public static final String EXTRA_FILENAME = "filename";
    public static final String EXTRA_FILENAMES = "video_list.filename";

    public static final String EXTRA_HASH_OPENSUBTITLES = "hash.opensubtitles";
    public static final String EXTRA_HASHES_OPENSUBTITLES = "video_list.hash.opensubtitles";

    private static final int ORIENTATION_LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    private static final int ORIENTATION_REVERSE_LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
    private static final int ORIENTATION_AUTO_ROTATION_LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
    private static final int ORIENTATION_PORTRAIT = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    private static final int ORIENTATION_REVERSE_PORTRAIT = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
    private static final int ORIENTATION_AUTO_ROTATION_PORTRAIT = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
    private static final int ORIENTATION_AUTO_ROTATION = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;
    private static final int ORIENTATION_SYSTEM_DEFAULT = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    private static final int ORIENTATION_MATCH_VIDEO_ORIENTATION = 99999;


    void demo() {
        // Simple run.
//        run("file:///storage/emulated/0/Movies/a.Avi");

        // Simple run only with title information.
//        run("file:///storage/emulated/0/Movies/a.Avi", "Cafrica, Pilot");


        // Complex run.
        Media media0 = new Media("file:///storage/emulated/0/Movies/a.avi"); // new Media("https://s3.amazonaws.com/mediatest888/s1.mkv");
//        media0.filename = "a.avi";
        media0.size = 367085370;
        media0.opensubtitlesHash = "bfb2c1c721605c8f";
        media0.title = "Cafrica Episode #0, Pilot";

        Media media1 = new Media("file:///storage/emulated/0/Movies/s1.mkv");
        media1.filename = "s1.mkv";
        media1.size = 31762747;
        media1.opensubtitlesHash = "49e2530ea3bd0d18";
        media1.title = "Elephant's Dream";

        Subtitle subtitle0_0 = new Subtitle("https://s3.amazonaws.com/mediatest888/s1.smi");
        subtitle0_0.name = "SAMI subtitle";
        subtitle0_0.filename = "a.smi";

        Subtitle subtitle0_1 = new Subtitle("file:///storage/emulated/0/Movies/a.srt");
        Subtitle subtitle0_2 = new Subtitle("http://192.168.0.102/Videos/vobsub/VTS_01_0.idx");
        subtitle0_2.name = "VobSub";

        Subtitle subtitle0_3 = new Subtitle("http://192.168.0.102/Videos/a.srt");

        Media[] medias = {media0, media1};
        Subtitle[] subtitles = {subtitle0_0, subtitle0_1, subtitle0_2, subtitle0_3};
        Uri[] enabledSubs = {subtitle0_1.uri};

        Options options = new Options();

        options.headers = new String[]{
                "User-Agent", "Mozilla compatible/1.0",
                "Auth-Token", "wpeog124sdfw-ef0wje+fxjfwe1"};

        options.videoZoom = Options.ZOOM_ORIGINAL;
        options.decoder = Options.DECODER_SW;
//        options.video = false;
        options.explicitList = true;
        options.resumeAt = 5000;
        options.sticky = true;
        options.orientation = ORIENTATION_REVERSE_LANDSCAPE;
        options.UpDownAction = Options.kActionKeyUpdownNextPrev;

        run(null, medias, subtitles, enabledSubs, options, true);
    }


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
    public static MXPackageInfo getMXPackageInfo() {
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

    private static final int REQUEST_CODE = 0x8001; // This can be changed as you wish.

    public static class Subtitle {
        /**
         * Subtitle URI
         */
        public final Uri uri;

        /**
         * (Optional) Custom subtitle name.
         */
        String name;

        /**
         * (Optional) File name.
         */
        String filename;


        Subtitle(Uri uri) {
            if (uri.getScheme() == null)
                throw new IllegalStateException("Scheme is missed for subtitle URI " + uri);

            this.uri = uri;
        }

        public Subtitle(String uriStr) {
            this(Uri.parse(uriStr));
        }
    }

    public static class Media {
        /**
         * Video or Audio URI.
         */
        final Uri uri;

        /**
         * Title text to be displayed on the title area of the playback screen. If null, player will select title automatically.
         */
        public String title;

        /**
         * Optional size of the media file. Set it only if player can't directly access video file such as network/streaming video.
         */
        long size;

        /**
         * Optional file name of the media file. Set it only if player can't directly access video file such as network/streaming video.
         */
        String filename;

        /**
         * OpenSubtitles.org MovieHash. Used for better subtitle searching from OpenSubtitles.org. Not required for local files. It should come with `size` and `filename`.
         */
        String opensubtitlesHash;


        /**
         * Create object with all options filled with default value except URI.
         */
        Media(Uri uri) {
            if (uri.getScheme() == null)
                throw new IllegalStateException("Scheme is missed for media URI " + uri);

            this.uri = uri;

            this.title = null;
            this.size = 0;
            this.filename = null;
            this.opensubtitlesHash = null;
        }

        public Media(String uriStr) {
            this(Uri.parse(uriStr));
        }

        void putToIntent(Intent intent) {
            intent.setData(uri);

            if (title != null)
                intent.putExtra(EXTRA_TITLE, title);

            if (size > 0)
                intent.putExtra(EXTRA_SIZE, size);

            if (filename != null)
                intent.putExtra(EXTRA_FILENAME, filename);

            if (opensubtitlesHash != null) {
                if (size <= 0 || (filename == null && uri.getLastPathSegment() == null))
                    throw new IllegalStateException("OpenSubtitles Hash should come along with `size` and `filename`.");

                intent.putExtra(EXTRA_HASH_OPENSUBTITLES, opensubtitlesHash);
            }
        }
    }

    public static class Options {
        static final byte NO_DECODER = 0;
        static final byte DECODER_HW = 1;
        static final byte DECODER_SW = 2;

        static final int NO_ZOOM = -1;
        static final int ZOOM_FIT_TO_SCREEN = 1;
        static final int ZOOM_STRETCH = 0;
        static final int ZOOM_CROP = 3;
        static final int ZOOM_ORIGINAL = 2;

        static final int kActionKeyUpdownVolume = 0;
        static final int kActionKeyUpdownNextPrev = 1;

        /**
         * Resume position, in milliseconds. If null, player will reumse or start over automatically.
         */
        Integer resumeAt;

        /**
         * One of NO_DECODER, DECODER_HW or DECODER_SW. If NO_DECODER is given, player will automatically select decoder
         */
        byte decoder;

        /**
         * Override video visibility.
         */
        Boolean video;

        boolean explicitList;

        /**
         * Override video zoom setting. Can be one of ZOOM_FIT_TO_SCREEN, ZOOM_STRETCH, ZOOM_CROP, ZOOM_ORIGINAL, NO_ZOOM. Set NO_ZOOM to make player select zoom mode automatically.
         */
        int videoZoom;

        /**
         * Override default DAR(display aspect ratio). Both DAR_horz and DAR_vert should come together or both should be 0 to use default DAR.
         */
        float DAR_horz;
        float DAR_vert;

        /**
         * Set true or false to enable/disable background play mode. If null, player will automatically enable or disable it.
         */
        Boolean sticky;

        /**
         * One of ORIENTATION_xxx. if null, use user's setting.
         */
        Integer orientation;

        /**
         * If set to true and error occurs, activity will play next video or close itself not showing error message.
         */
        public boolean suppressErrorMessage;

        /**
         * If set to true, video uri will not be displayed in the property dialog box.
         */
        boolean secureUri;

        /**
         * Headers to be sent with to the media server while playing network/streaming media. If null, player's default header will be used which may vary version by version.
         */
        public String[] headers;

        /**
         * {@link #kActionKeyUpdownVolume}, {@link #kActionKeyUpdownNextPrev}, or null.
         */
        Integer UpDownAction;


        /**
         * Create object with all options filled with default value.
         */
        public Options() {
            this.decoder = NO_DECODER;
            this.videoZoom = NO_ZOOM;
        }

        void putToIntent(Intent intent) {
            if (resumeAt != null)
                intent.putExtra(EXTRA_POSITION, resumeAt);

            if (decoder != NO_DECODER)
                intent.putExtra(EXTRA_DECODER, decoder);

            if (video != null)
                intent.putExtra(EXTRA_VIDEO, video);

            if (explicitList)
                intent.putExtra(EXTRA_EXPLICIT_LIST, true);

            if (videoZoom != NO_ZOOM)
                intent.putExtra(EXTRA_VIDEO_ZOOM, videoZoom);

            if (DAR_horz > 0 && DAR_vert > 0) {
                intent.putExtra(EXTRA_DAR_HORZ, DAR_horz);
                intent.putExtra(EXTRA_DAR_VERT, DAR_vert);
            }

            if (sticky != null)
                intent.putExtra(EXTRA_STICKY, sticky);

            if (orientation != null)
                intent.putExtra(EXTRA_ORIENTATION, orientation);

            if (suppressErrorMessage)
                intent.putExtra(EXTRA_SUPPRESS_ERROR_MESSAGE, true);

            if (secureUri)
                intent.putExtra(EXTRA_SECURE_URI, true);

            if (headers != null)
                intent.putExtra(EXTRA_HEADERS, headers);

            if (UpDownAction != null)
                intent.putExtra(EXTRA_KEYS_DPAD_UPDOWN, UpDownAction);
        }
    }

    /**
     * Launch MX Player(Pro).
     *
     * @param medias              Video/audio URI.
     * @param subtitles           Subtitles to be played with the given video. Can be null.
     * @param enabledSubtitles    URIs of enabled subtitles. Can be null.
     * @param options             Optional options. Can be null to use all default values.
     * @param playOnlyGivenMedias This option is used only if medias size is 1. If set true, only given medias will be played, otherwise, player will play next video on the folder in which given media located, after finishing given media.
     */
    public static boolean run(Activity activity, Media[] medias, Subtitle[] subtitles, Uri[] enabledSubtitles, Options options, boolean playOnlyGivenMedias) {
        // At least 1 media should be provided.
        if (medias.length == 0)
            return false;

        // Find MX Player(Pro) package and activity.
        MXPackageInfo packageInfo = getMXPackageInfo();
        if (packageInfo == null)
            return false;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setPackage(packageInfo.packageName);
        intent.setClassName(packageInfo.packageName, packageInfo.activityName);

        // Put information for 1st video.
        medias[0].putToIntent(intent);

        // Put information for next videos.
        if (playOnlyGivenMedias || medias.length > 1) {
            Parcelable[] uris = new Parcelable[medias.length];
            String[] names = null, filenames = null, opensubtitlesHashes = null;
            long[] sizes = null;

            for (int i = 0; i < medias.length; ++i) {
                Media media = medias[i];

                uris[i] = media.uri;

                if (media.title != null) {
                    if (names == null)
                        names = new String[medias.length];

                    names[i] = media.title;
                }

                if (media.size > 0) {
                    if (sizes == null)
                        sizes = new long[medias.length];

                    sizes[i] = media.size;
                }

                if (media.filename != null) {
                    if (filenames == null)
                        filenames = new String[medias.length];

                    filenames[i] = media.filename;
                }

                if (media.opensubtitlesHash != null) {
                    if (opensubtitlesHashes == null)
                        opensubtitlesHashes = new String[medias.length];

                    opensubtitlesHashes[i] = media.opensubtitlesHash;
                }
            }

            intent.putExtra(EXTRA_LIST, uris);

            if (names != null)
                intent.putExtra(EXTRA_TITLES, names);

            if (sizes != null)
                intent.putExtra(EXTRA_SIZES, sizes);

            if (filenames != null)
                intent.putExtra(EXTRA_FILENAMES, filenames);

            if (opensubtitlesHashes != null)
                intent.putExtra(EXTRA_HASHES_OPENSUBTITLES, opensubtitlesHashes);
        }

        // Put options. (Note that some options may work only on 1st video.)
        if (options != null)
            options.putToIntent(intent);

        // Put subtitle extras. (Note that subtitle information will be used only for 1st video.)
        if (subtitles != null) {
            Parcelable[] parcels = new Parcelable[subtitles.length];
            String[] names = null, filenames = null;

            for (int i = 0; i < subtitles.length; ++i) {
                Subtitle sub = subtitles[i];

                parcels[i] = sub.uri;

                if (sub.name != null) {
                    if (names == null)
                        names = new String[subtitles.length];

                    names[i] = sub.name;
                }

                if (sub.filename != null) {
                    if (filenames == null)
                        filenames = new String[subtitles.length];

                    filenames[i] = sub.filename;
                }
            }

            intent.putExtra(EXTRA_SUBTITLES, parcels);

            if (names != null)
                intent.putExtra(EXTRA_SUBTITLE_NAMES, names);

            if (filenames != null)
                intent.putExtra(EXTRA_SUBTITLE_FILENAMES, filenames);
        }

        if (enabledSubtitles != null) {
            Parcelable[] parcels = new Parcelable[enabledSubtitles.length];

            for (int i = 0; i < enabledSubtitles.length; ++i)
                parcels[i] = enabledSubtitles[i];

            intent.putExtra(EXTRA_ENABLED_SUBTITLES, parcels);
        }

        // Launch MX Player(Pro)
        try {
            activity.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException ex) {
            Log.e(TAG, "Can't run MX Player(Pro)", ex);
            return false;
        }
    }

    public static boolean run(Activity activity, String uriStr) {
        return run(activity, new Media[]{new Media(uriStr)}, null, null, null, false);
    }

    public static boolean run(Activity activity, String uriStr, String title) {
        Media media = new Media(uriStr);
        media.title = title;
        return run(activity, new Media[]{media}, null, null, null, false);
    }
}
