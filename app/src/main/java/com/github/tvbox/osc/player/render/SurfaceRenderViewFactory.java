package com.github.tvbox.osc.player.render;

import android.content.Context;

import xyz.doikki.videoplayer.render.IRenderView;
import xyz.doikki.videoplayer.render.RenderViewFactory;

public class SurfaceRenderViewFactory extends RenderViewFactory {

    public static SurfaceRenderViewFactory create() {
        return new SurfaceRenderViewFactory();
    }

    @Override
    public IRenderView createRenderView(Context context) {
        return new SurfaceRenderView(context);
    }
}