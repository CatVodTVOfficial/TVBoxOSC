package com.github.tvbox.osc.util;

import android.content.Context;

import xyz.doikki.videoplayer.exo.ExoMediaPlayerFactory;
import xyz.doikki.videoplayer.player.AndroidMediaPlayerFactory;
import xyz.doikki.videoplayer.player.PlayerFactory;
import xyz.doikki.videoplayer.player.VideoView;
import xyz.doikki.videoplayer.player.VideoViewConfig;
import xyz.doikki.videoplayer.player.VideoViewManager;
import xyz.doikki.videoplayer.render.RenderViewFactory;
import xyz.doikki.videoplayer.render.TextureRenderViewFactory;
import com.github.tvbox.osc.player.IjkMediaPlayer;
import com.github.tvbox.osc.player.render.SurfaceRenderViewFactory;
import com.orhanobut.hawk.Hawk;

import tv.danmaku.ijk.media.player.IjkLibLoader;

public class PlayerHelper {
    public static void updateCfg(VideoView videoView) {
        int playType = Hawk.get(HawkConfig.PLAY_TYPE, 0);
        PlayerFactory playerFactory;
        if (playType == 1) {
            playerFactory = new PlayerFactory<IjkMediaPlayer>() {
                @Override
                public IjkMediaPlayer createPlayer(Context context) {
                    return new IjkMediaPlayer(context);
                }
            };
            try {
                tv.danmaku.ijk.media.player.IjkMediaPlayer.loadLibrariesOnce(new IjkLibLoader() {
                    @Override
                    public void loadLibrary(String s) throws UnsatisfiedLinkError, SecurityException {
                        try {
                            System.loadLibrary(s);
                        } catch (Throwable th) {
                            th.printStackTrace();
                        }
                    }
                });
            } catch (Throwable th) {
                th.printStackTrace();
            }
        } else if (playType == 2) {
            playerFactory = ExoMediaPlayerFactory.create();
        } else {
            playerFactory = AndroidMediaPlayerFactory.create();
        }
        int renderType = Hawk.get(HawkConfig.PLAY_RENDER, 0);
        RenderViewFactory renderViewFactory = null;
        switch (renderType) {
            case 0:
            default:
                renderViewFactory = TextureRenderViewFactory.create();
                break;
            case 1:
                renderViewFactory = SurfaceRenderViewFactory.create();
                break;
        }
        videoView.setPlayerFactory(playerFactory);
        videoView.setRenderViewFactory(renderViewFactory);
    }


    public static void init() {
        int playType = Hawk.get(HawkConfig.PLAY_TYPE, 0);
        PlayerFactory playerFactory;
        if (playType == 1) {
            playerFactory = new PlayerFactory<IjkMediaPlayer>() {
                @Override
                public IjkMediaPlayer createPlayer(Context context) {
                    return new IjkMediaPlayer(context);
                }
            };
            try {
                tv.danmaku.ijk.media.player.IjkMediaPlayer.loadLibrariesOnce(new IjkLibLoader() {
                    @Override
                    public void loadLibrary(String s) throws UnsatisfiedLinkError, SecurityException {
                        try {
                            System.loadLibrary(s);
                        } catch (Throwable th) {
                            th.printStackTrace();
                        }
                    }
                });
            } catch (Throwable th) {
                th.printStackTrace();
            }
        } else if (playType == 2) {
            playerFactory = ExoMediaPlayerFactory.create();
        } else {
            playerFactory = AndroidMediaPlayerFactory.create();
        }
        int renderType = Hawk.get(HawkConfig.PLAY_RENDER, 0);
        RenderViewFactory renderViewFactory = null;
        switch (renderType) {
            case 0:
            default:
                renderViewFactory = TextureRenderViewFactory.create();
                break;
            case 1:
                renderViewFactory = SurfaceRenderViewFactory.create();
                break;
        }
        //播放器配置，注意：此为全局配置，按需开启
        VideoViewManager.setConfig(VideoViewConfig.newBuilder()
                .setLogEnabled(Hawk.get(HawkConfig.DEBUG_OPEN, false))
                .setScreenScaleType(VideoView.SCREEN_SCALE_DEFAULT)
                .setPlayerFactory(playerFactory)
                .setRenderViewFactory(renderViewFactory)
                .build());
    }
}
