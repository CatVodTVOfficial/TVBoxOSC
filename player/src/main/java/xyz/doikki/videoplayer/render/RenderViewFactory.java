package xyz.doikki.videoplayer.render;

import android.content.Context;

/**
 * 此接口用于扩展自己的渲染View。使用方法如下：
 * 1.继承IRenderView实现自己的渲染View。
 * 2.重写createRenderView返回步骤1的渲染View。
 * 可参考{@link TextureRenderView}和{@link TextureRenderViewFactory}的实现。
 */
public abstract class RenderViewFactory {

    public abstract IRenderView createRenderView(Context context);

}
